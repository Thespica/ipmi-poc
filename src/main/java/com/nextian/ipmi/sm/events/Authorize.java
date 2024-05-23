/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.events;

import com.nextian.ipmi.coding.commands.PrivilegeLevel;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.states.Authcap;
import com.nextian.ipmi.sm.states.OpenSessionWaiting;

/**
 * Performs transition from {@link Authcap} to {@link OpenSessionWaiting}.
 *
 * @see StateMachine
 */
public class Authorize extends Default {

    private final int sessionId;

    public int getSessionId() {
        return sessionId;
    }

    public Authorize(CipherSuite cipherSuite, int sequenceNumber, PrivilegeLevel privilegeLevel, int sessionId) {
        super(cipherSuite, sequenceNumber, privilegeLevel);
        this.sessionId = sessionId;
    }

}
