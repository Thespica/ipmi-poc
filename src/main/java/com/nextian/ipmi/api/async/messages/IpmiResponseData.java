/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.api.async.messages;

import com.nextian.ipmi.api.async.ConnectionHandle;
import com.nextian.ipmi.coding.commands.ResponseData;

/**
 * Class that wraps {@link ResponseData} that was received successfully.
 */
public class IpmiResponseData extends IpmiResponse {
    private ResponseData responseData;

    /**
     * @return {@link ResponseData} received successfully.
     */
    public ResponseData getResponseData() {
        return responseData;
    }

    public IpmiResponseData(ResponseData data, int tag, ConnectionHandle handle) {
        super(tag, handle);
        responseData = data;
    }
}
