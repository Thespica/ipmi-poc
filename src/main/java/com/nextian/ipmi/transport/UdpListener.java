/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.transport;

/**
 * An interface for {@link UdpMessenger} listener.
 */
public interface UdpListener {
    /**
     * Notifies listener of the UDP message that was received.
     *
     * @param message message received
     */
    void notifyMessage(UdpMessage message) throws InterruptedException;
}
