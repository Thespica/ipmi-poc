/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.states;

import com.nextian.ipmi.coding.Encoder;
import com.nextian.ipmi.coding.commands.session.OpenSession;
import com.nextian.ipmi.coding.commands.session.Rakp1;
import com.nextian.ipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.nextian.ipmi.coding.rmcp.RmcpMessage;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.actions.ErrorAction;
import com.nextian.ipmi.sm.events.OpenSessionAck;
import com.nextian.ipmi.sm.events.StateMachineEvent;

/**
 * Indicates that {@link OpenSession} response was received. Transition to {@link Rakp1Waiting} on
 * {@link OpenSessionAck}
 */
public class OpenSessionComplete extends State {

    @Override
    public void doTransition(StateMachine stateMachine, StateMachineEvent machineEvent) throws InterruptedException {
        if (machineEvent instanceof OpenSessionAck) {
            OpenSessionAck event = (OpenSessionAck) machineEvent;

            Rakp1 rakp1 = new Rakp1(event.getManagedSystemSessionId(),
                    event.getPrivilegeLevel(), event.getUsername(),
                    event.getPassword(), event.getBmcKey(),
                    event.getCipherSuite());

            try {
                stateMachine.setCurrent(new Rakp1Waiting(event.getSequenceNumber(), rakp1));
                stateMachine.sendMessage(Encoder.encode(new Protocolv20Encoder(), rakp1, event.getSequenceNumber(), 0));
            } catch (Exception e) {
                stateMachine.setCurrent(this);
                stateMachine.doExternalAction(new ErrorAction(e));
            }
        } else {
            stateMachine.doExternalAction(new ErrorAction(new IllegalArgumentException("Invalid transition")));
        }

    }

    @Override
    public void doAction(StateMachine stateMachine, RmcpMessage message) {
        // No action is assigned to this state
    }

}
