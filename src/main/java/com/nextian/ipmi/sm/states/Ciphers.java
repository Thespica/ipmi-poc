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
import com.nextian.ipmi.coding.commands.IpmiVersion;
import com.nextian.ipmi.coding.commands.session.GetChannelAuthenticationCapabilities;
import com.nextian.ipmi.coding.protocol.encoder.Protocolv15Encoder;
import com.nextian.ipmi.coding.rmcp.RmcpMessage;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.common.TypeConverter;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.actions.ErrorAction;
import com.nextian.ipmi.sm.events.Default;
import com.nextian.ipmi.sm.events.StateMachineEvent;

/**
 * State at which {@link CipherSuite} that will be used during the session is already picked. Transition to
 * {@link AuthcapWaiting} on {@link Default}. On failure it is possible to retry by sending {@link Default} event again.
 */
public class Ciphers extends State {

    @Override
    public void doTransition(StateMachine stateMachine, StateMachineEvent machineEvent) throws InterruptedException {
        if (machineEvent instanceof Default) {
            Default event = (Default) machineEvent;
            GetChannelAuthenticationCapabilities authCap = new GetChannelAuthenticationCapabilities(
                    IpmiVersion.V15, IpmiVersion.V20, event.getCipherSuite(), event.getPrivilegeLevel(),
                    TypeConverter.intToByte(0xe));
            try {
                stateMachine.setCurrent(new AuthcapWaiting(event.getSequenceNumber()));
                stateMachine.sendMessage(Encoder.encode(
                        new Protocolv15Encoder(), authCap, event.getSequenceNumber(), 0));
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
        // There is no action related to this state
    }

}
