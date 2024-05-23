/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.actions;

import com.nextian.ipmi.coding.commands.ResponseData;
import com.nextian.ipmi.coding.payload.CompletionCode;

/**
 * {@link StateMachineAction} carrying {@link ResponseData} for the message in the process
 * of the session challenge.
 */
public class ResponseAction extends StateMachineAction {

    public ResponseAction(CompletionCode responseCode, ResponseData ipmiResponseData) {
        setIpmiResponseData(ipmiResponseData);
        setIpmiResponseCode(responseCode);
    }

    private CompletionCode responseCode;
    private ResponseData ipmiResponseData;

    public void setIpmiResponseData(ResponseData ipmiResponseData) {
        this.ipmiResponseData = ipmiResponseData;
    }

    public ResponseData getIpmiResponseData() {
        return ipmiResponseData;
    }

    public void setIpmiResponseCode(CompletionCode responseCode) {
        this.responseCode = responseCode;
    }

    public CompletionCode getIpmiResponseCode() {
        return responseCode;
    }
}
