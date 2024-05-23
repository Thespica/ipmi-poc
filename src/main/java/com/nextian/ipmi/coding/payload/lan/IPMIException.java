/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.coding.payload.lan;

import com.nextian.ipmi.coding.payload.CompletionCode;

/**
 * IPMI exception class. It holds received IPMI completion code.
 */
public class IPMIException extends Exception {

    private static final long serialVersionUID = 1L;

    private final CompletionCode completionCode;

    public IPMIException(CompletionCode completionCode) {
        this.completionCode = completionCode;
    }

    public CompletionCode getCompletionCode() {
        return completionCode;
    }

    @Override
    public String getMessage() {
        return completionCode.getMessage();
    }
}
