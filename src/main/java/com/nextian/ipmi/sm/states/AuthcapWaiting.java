/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.states;

import com.nextian.ipmi.coding.commands.session.GetChannelAuthenticationCapabilities;
import com.nextian.ipmi.coding.payload.lan.IpmiLanResponse;
import com.nextian.ipmi.coding.protocol.AuthenticationType;
import com.nextian.ipmi.coding.protocol.IpmiMessage;
import com.nextian.ipmi.coding.protocol.decoder.ProtocolDecoder;
import com.nextian.ipmi.coding.protocol.decoder.Protocolv15Decoder;
import com.nextian.ipmi.coding.rmcp.RmcpMessage;
import com.nextian.ipmi.common.TypeConverter;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.actions.ErrorAction;
import com.nextian.ipmi.sm.actions.ResponseAction;
import com.nextian.ipmi.sm.events.AuthenticationCapabilitiesReceived;
import com.nextian.ipmi.sm.events.StateMachineEvent;
import com.nextian.ipmi.sm.events.Timeout;

/**
 * Waiting for the {@link GetChannelAuthenticationCapabilities} response. <br>
 * Transition to: <li>{@link Ciphers} on {@link Timeout} <li>{@link Authcap} on
 * {@link AuthenticationCapabilitiesReceived}</li>.
 */
public class AuthcapWaiting extends State {

    private final int tag;

    public AuthcapWaiting(int tag) {
        this.tag = tag;
    }

    @Override
    public void doTransition(StateMachine stateMachine, StateMachineEvent machineEvent) throws InterruptedException {
        if (machineEvent instanceof Timeout) {
            stateMachine.setCurrent(new Ciphers());
        } else if (machineEvent instanceof AuthenticationCapabilitiesReceived) {
            stateMachine.setCurrent(new Authcap());
        } else {
            stateMachine.doExternalAction(new ErrorAction(new IllegalArgumentException("Invalid transition")));
        }
    }

    @Override
    public void doAction(StateMachine stateMachine, RmcpMessage message) throws InterruptedException {
        if (ProtocolDecoder.decodeAuthenticationType(message) == AuthenticationType.RMCPPlus) {
            return; // this isn't IPMI v1.5 message so we ignore it
        }
        Protocolv15Decoder decoder = new Protocolv15Decoder();
        IpmiMessage ipmiMessage = null;
        try {
            ipmiMessage = decoder.decode(message);
            GetChannelAuthenticationCapabilities capabilities = new GetChannelAuthenticationCapabilities();
            if (capabilities.isCommandResponse(ipmiMessage)) {
                IpmiLanResponse ipmiLanResponse = (IpmiLanResponse) ipmiMessage.getPayload();
                if (TypeConverter.byteToInt(ipmiLanResponse.getSequenceNumber()) == tag) {
                    stateMachine.doExternalAction(new ResponseAction(ipmiLanResponse.getCompletionCode(),
                            capabilities.getResponseData(ipmiMessage)));
                }
            }
        } catch (Exception e) {
            stateMachine.doExternalAction(new ErrorAction(e));
        }
    }

}
