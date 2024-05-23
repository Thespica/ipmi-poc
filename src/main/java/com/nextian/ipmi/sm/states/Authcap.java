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
import com.nextian.ipmi.coding.commands.session.GetChannelAuthenticationCapabilities;
import com.nextian.ipmi.coding.commands.session.OpenSession;
import com.nextian.ipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.nextian.ipmi.coding.rmcp.RmcpMessage;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.actions.ErrorAction;
import com.nextian.ipmi.sm.events.Authorize;
import com.nextian.ipmi.sm.events.StateMachineEvent;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * {@link GetChannelAuthenticationCapabilities} response was received. At this point the Session Challenge is going
 * to start. Transits to {@link OpenSessionWaiting} on {@link Authorize}.
 */
public class Authcap extends State {

    @Override
    public void doTransition(StateMachine stateMachine, StateMachineEvent machineEvent) throws InterruptedException {
        if (machineEvent instanceof Authorize) {
            Authorize event = (Authorize) machineEvent;

            OpenSession openSession = new OpenSession(event.getSessionId(), event.getPrivilegeLevel(),
                    event.getCipherSuite());

            try {
                stateMachine.setCurrent(new OpenSessionWaiting(event.getSequenceNumber()));
                stateMachine.sendMessage(Encoder.encode(new Protocolv20Encoder(), openSession,
                        event.getSequenceNumber(), 0));
            } catch (IOException e) {
                stateMachine.setCurrent(this);
                stateMachine.doExternalAction(new ErrorAction(e));
            } catch (InvalidKeyException e) {
                stateMachine.setCurrent(this);
                stateMachine.doExternalAction(new ErrorAction(e));
            } catch (NoSuchAlgorithmException e) {
                stateMachine.setCurrent(this);
                stateMachine.doExternalAction(new ErrorAction(e));
            }
        } else {
            stateMachine.doExternalAction(new ErrorAction(new IllegalArgumentException("Invalid transition: "
                    + machineEvent.getClass().getSimpleName())));
        }

    }

    @Override
    public void doAction(StateMachine stateMachine, RmcpMessage message) {
        // No action is assigned to this state
    }

}
