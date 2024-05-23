/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */

package com.nextian.ipmi.transport;

import java.io.IOException;

/**
 * Low level connection handler interface.
 */
public interface Messenger {
    /**
     * Sends {@link UdpMessage}.
     *
     * @param message {@link UdpMessage} to send.
     * @throws IOException when sending of the message fails
     */
    void send(UdpMessage message) throws IOException;

    /**
     * Registers listener in the {@link Messenger} so it will be notified via
     * {@link UdpListener#notifyMessage(UdpMessage)} when new message arrives.
     *
     * @param listener {@link UdpListener} to register.
     */
    void register(UdpListener listener);

    /**
     * Unregisters listener from {@link Messenger} so it no longer will be notified.
     *
     * @param listener {@link UdpListener} to unregister
     */
    void unregister(UdpListener listener);

    /**
     * Closes the connection.
     */
    void closeConnection();
}
