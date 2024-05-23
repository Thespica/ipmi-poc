/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */

package com.nextian.ipmi.sm.events;

import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.states.Authcap;
import com.nextian.ipmi.sm.states.SessionValid;
import com.nextian.ipmi.sm.states.State;

/**
 * {@link StateMachineEvent} that will make {@link StateMachine} in the {@link SessionValid} {@link State} to send
 * {@link com.nextian.ipmi.coding.commands.session.CloseSession} and transit to {@link Authcap} {@link State}
 * the session.
 */
public class CloseSession extends StateMachineEvent {
    private final int sessionId;
    private final int sequenceNumber;

    /**
     * Prepares {@link CloseSession}.
     *
     * @param sessionId      managed system session ID
     * @param sequenceNumber generated sequence number for the message to send
     */
    public CloseSession(int sessionId, int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        this.sessionId = sessionId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
