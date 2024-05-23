/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.transport;

import com.google.gson.Gson;
import com.nextian.ipmi.common.GsonFactory;

import java.net.InetAddress;

/**
 * Container for UDP message.
 */
public class UdpMessage {
    /**
     * Target port when sending message. Sender port when receiving message.
     */
    private int port;

    /**
     * Target address when sending message. Sender address when receiving message.
     */
    private InetAddress address;

    /**
     * Raw message data
     */
    private byte[] message;

    /**
     * Target port when sending message. Sender port when receiving message.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Target port when sending message. Sender port when receiving message.
     */
    public int getPort() {
        return port;
    }

    /**
     * Target address when sending message. Sender address when receiving message.
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Target address when sending message. Sender address when receiving message.
     */
    public void setAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * Get raw message data
     *
     * @return byte array of message data
     */
    public byte[] getMessage() {
        return message;
    }

    /**
     * Assign raw message data
     *
     * @param message byte array of message data
     */
    public void setMessage(byte[] message) {
        this.message = message;
    }

    /**
     * Convert IPMI message to more user friendly representation.
     *
     * @return string representing the object
     */
    @Override
    public String toString() {
        String msg;
        Gson gson = GsonFactory.getGson();

        msg = gson.toJson(this);
        return msg;
    }
}
