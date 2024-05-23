/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.states;

import com.nextian.ipmi.coding.rmcp.RmcpMessage;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.actions.ErrorAction;
import com.nextian.ipmi.sm.events.DefaultAck;
import com.nextian.ipmi.sm.events.StartSession;
import com.nextian.ipmi.sm.events.StateMachineEvent;

/**
 * Empty state inserted to keep the convention of Waiting-Complete states. At this point Session Challenge is over
 * and {@link StateMachine} can transit to {@link SessionValid} on {@link DefaultAck}
 */
public class Rakp3Complete extends State {

    @Override
    public void doTransition(StateMachine stateMachine, StateMachineEvent machineEvent) throws InterruptedException {
        if (machineEvent instanceof StartSession) {
            StartSession event = (StartSession) machineEvent;
            stateMachine.setCurrent(new SessionValid(event.getCipherSuite(), event.getSessionId()));
        } else {
            stateMachine.doExternalAction(new ErrorAction(new IllegalArgumentException("Invalid transition")));
        }
    }

    @Override
    public void doAction(StateMachine stateMachine, RmcpMessage message) {
        // No action is performed in this state
    }

}
