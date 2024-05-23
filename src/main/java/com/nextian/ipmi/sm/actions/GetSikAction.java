/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.actions;

/**
 * Returns the Session Integrity Key calculated after receiving RAKP Message 2.
 */
public class GetSikAction extends StateMachineAction {
    private final byte[] sik;

    public GetSikAction(byte[] sik) {
        this.sik = sik;
    }

    public byte[] getSik() {
        return sik;
    }
}
