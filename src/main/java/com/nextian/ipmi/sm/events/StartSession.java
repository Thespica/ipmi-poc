/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.events;

import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.states.Rakp3Complete;
import com.nextian.ipmi.sm.states.SessionValid;

/**
 * Acknowledges starting the session after receiving RAKP Message 4 ({@link StateMachine} transits from
 * {@link Rakp3Complete} to {@link SessionValid})
 *
 * @see StateMachine
 */
public class StartSession extends StateMachineEvent {
    private CipherSuite cipherSuite;
    private int sessionId;

    public StartSession(CipherSuite cipherSuite, int sessionId) {
        this.cipherSuite = cipherSuite;
        this.sessionId = sessionId;
    }

    public CipherSuite getCipherSuite() {
        return cipherSuite;
    }

    public int getSessionId() {
        return sessionId;
    }
}
