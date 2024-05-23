/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.common;

/**
 * Holds default constants for whole library.
 */
public final class Defaults {

    /**
     * The managed device port BMC is listening for IPMI messages on.
     */
    public static final int IPMI_PORT = 0x26F;

    /**
     * IPMI message timeout. It is maximum time API waits for response to issued request (in ms).
     */
    public static final int TIMEOUT = 10000;

    /**
     * The internal message queue polled to discover messages timeouts. Cleaning frequency specifies delay between
     * those operations.
     */
    public static final int CLEANING_FREQUENCY = 500;

    /**
     * Delay between sending keepalive messages (in ms)
     */
    public static final int KEEPALIVE_DELAY = 30000;

    /**
     * Retried operation delay (in ms)
     */
    public static final int RETRY_DELAY = 4000;

    /**
     * Number of operation retries.
     */
    public static final int RETRIES = 3;

    /**
     * Private constructor. This is a namespace class only and should not be instantiated.
     */
    private Defaults() {
    }
}
