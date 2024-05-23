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
import com.nextian.ipmi.coding.commands.session.GetChannelCipherSuites;
import com.nextian.ipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.nextian.ipmi.coding.rmcp.RmcpMessage;
import com.nextian.ipmi.common.TypeConverter;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.actions.ErrorAction;
import com.nextian.ipmi.sm.events.Default;
import com.nextian.ipmi.sm.events.GetChannelCipherSuitesPending;
import com.nextian.ipmi.sm.events.StateMachineEvent;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * The initial state. Transits to {@link CiphersWaiting} on {@link GetChannelCipherSuitesPending}.
 */
public class Uninitialized extends State {

    @Override
    public void doTransition(StateMachine stateMachine, StateMachineEvent machineEvent) throws InterruptedException {
        if (machineEvent instanceof GetChannelCipherSuitesPending) {
            Default event = (GetChannelCipherSuitesPending) machineEvent;
            GetChannelCipherSuites cipherSuites = new GetChannelCipherSuites(TypeConverter.intToByte(0xE), (byte) 0);
            try {
                stateMachine.setCurrent(new CiphersWaiting(0, event.getSequenceNumber()));

                stateMachine.sendMessage(Encoder.encode(new Protocolv20Encoder(), cipherSuites,
                        event.getSequenceNumber(), 0));
            } catch (InvalidKeyException e) {
                stateMachine.setCurrent(this);
                stateMachine.doExternalAction(new ErrorAction(e));
            } catch (NoSuchAlgorithmException e) {
                stateMachine.setCurrent(this);
                stateMachine.doExternalAction(new ErrorAction(e));
            } catch (IOException e) {
                stateMachine.setCurrent(this);
                stateMachine.doExternalAction(new ErrorAction(e));
            }
        } else {
            stateMachine.doExternalAction(new ErrorAction(new IllegalArgumentException("Invalid transition")));
        }
    }

    @Override
    public void doAction(StateMachine stateMachine, RmcpMessage message) {
        // No action is related to this state
    }

}
