/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */

package com.nextian.ipmi.api.sync;

import com.nextian.ipmi.api.async.ConnectionHandle;
import com.nextian.ipmi.api.async.IpmiAsyncConnector;
import com.nextian.ipmi.api.async.IpmiListener;
import com.nextian.ipmi.api.async.messages.IpmiError;
import com.nextian.ipmi.api.async.messages.IpmiResponse;
import com.nextian.ipmi.api.async.messages.IpmiResponseData;
import com.nextian.ipmi.coding.commands.ResponseData;
import com.nextian.ipmi.coding.payload.lan.IPMIException;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Listens to {@link IpmiAsyncConnector} waiting for concrete message to
 * arrive. Must be registered via
 * {@link IpmiAsyncConnector#registerListener(IpmiListener)} to receive
 * messages.
 */
public class MessageListener implements IpmiListener {
    /**
     * Constructor-initialized connection handle.
     */
    private final ConnectionHandle handle;

    /**
	 * Received messages that have proper connection handle.
	 */
    private final BlockingQueue<IpmiResponse> messagesQueue;


	/**
     * Initializing constructor.
     *
     * @param handle connection handle for incoming messages
     */
	public MessageListener(ConnectionHandle handle) {
		messagesQueue = new LinkedBlockingDeque<IpmiResponse>();
		this.handle = handle;
	}

	/**
     * Blocks invoking thread until message with expected tag arrives.
     *
     * @param tag requested message tag
     * @param timeout time to wait for response in milliseconds
     * @return {@link ResponseData} for received message
     * @throws InterruptedException if thread is interrupted
     * @throws IPMIException if response was received but contains request error code
     * @throws IOException if exception occurred when receiving response message
     */
    public ResponseData waitForAnswer(int tag, int timeout)
            throws InterruptedException, IOException, IPMIException {
        ResponseData data;
		IpmiResponse response;

		// Validate tag parameter
        if (tag >= 0 && tag <= 63) {
            // Get response from queue - wait if necessary
            do {
                response = messagesQueue.poll(timeout, TimeUnit.MILLISECONDS);
            } while (response.getTag() != tag);

            // Remove other messages
            messagesQueue.clear();

            // Get data from response if possible
            if (response instanceof IpmiResponseData) {
                data = ((IpmiResponseData) response).getResponseData();
            } else {
                // IPMIException requires special handling because it carries response result code
                if (((IpmiError) response).getException() instanceof IPMIException) {
                    throw (IPMIException) ((IpmiError) response).getException();
                } else {
                    throw new IOException("Response error", ((IpmiError) response).getException());
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid message tag (should be in 0-63 range");
        }
		return data;
	}

    /**
     * Notify listener about received messages. If message handle matches listener handle response is queued for later
     * analysis
     * @param response response passed to the listener
     */
	@Override
	public synchronized void notify(IpmiResponse response) {
		if (response.getHandle().getHandle() == handle.getHandle()) {
            messagesQueue.add(response);
		}
	}
}
