/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.connection;

import com.nextian.ipmi.coding.commands.ResponseData;

/**
 * Interface for the {@link Connection} listener.
 */
public interface ConnectionListener {

    /**
     * Notifies the {@link ConnectionListener}s of the one of the following events:
     * <li>response to the request tagged with id arrived
     * <li>request tagged with id timed out
     *
     * @param responseData {@link ResponseData} specific for the request if it was completed successfully, null if it
     *                     timed out or an error occurred during decoding.
     * @param handle       the id of the connection that received the message
     * @param id           tag of the request-response pair
     * @param exception    null if everything went correctly or timeout occurred, contains exception that occurred
     *                     during decoding if it failed.
     */
    void notify(ResponseData responseData, int handle, int id, Exception exception);
}
