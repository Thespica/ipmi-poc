/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.states;

import com.nextian.ipmi.coding.commands.session.OpenSession;
import com.nextian.ipmi.coding.payload.CompletionCode;
import com.nextian.ipmi.coding.payload.lan.IPMIException;
import com.nextian.ipmi.coding.protocol.AuthenticationType;
import com.nextian.ipmi.coding.protocol.IpmiMessage;
import com.nextian.ipmi.coding.protocol.PayloadType;
import com.nextian.ipmi.coding.protocol.decoder.PlainCommandv20Decoder;
import com.nextian.ipmi.coding.protocol.decoder.ProtocolDecoder;
import com.nextian.ipmi.coding.protocol.decoder.Protocolv20Decoder;
import com.nextian.ipmi.coding.rmcp.RmcpMessage;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.common.TypeConverter;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.actions.ErrorAction;
import com.nextian.ipmi.sm.actions.ResponseAction;
import com.nextian.ipmi.sm.events.DefaultAck;
import com.nextian.ipmi.sm.events.StateMachineEvent;
import com.nextian.ipmi.sm.events.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidKeyException;

/**
 * Waiting for the {@link OpenSession} response.<br>
 * <li>Transition to {@link OpenSessionComplete} on {@link DefaultAck} <li>
 * Transition to {@link Authcap} on {@link Timeout}
 */
public class OpenSessionWaiting extends State {

    private static Logger LOGGER = LoggerFactory.getLogger(OpenSessionWaiting.class);
    private int tag;

    public OpenSessionWaiting(int tag) {
        this.tag = tag;
    }

    @Override
    public void doTransition(StateMachine stateMachine, StateMachineEvent machineEvent) throws InterruptedException {
        if (machineEvent instanceof DefaultAck) {
            stateMachine.setCurrent(new OpenSessionComplete());
        } else if (machineEvent instanceof Timeout) {
            stateMachine.setCurrent(new Authcap());
        } else {
            stateMachine.doExternalAction(new ErrorAction(new IllegalArgumentException("Invalid transition")));
        }
    }

    @Override
    public void doAction(StateMachine stateMachine, RmcpMessage message) throws InterruptedException {
        if (ProtocolDecoder.decodeAuthenticationType(message) != AuthenticationType.RMCPPlus) {
            return; // this isn't IPMI v2.0 message so we ignore it
        }
        PlainCommandv20Decoder decoder = new PlainCommandv20Decoder(
                CipherSuite.getEmpty());
        if (Protocolv20Decoder.decodePayloadType(message.getData()[1]) != PayloadType.RmcpOpenSessionResponse) {
            return;
        }
        IpmiMessage ipmiMessage = null;
        try {
            ipmiMessage = decoder.decode(message);
            LOGGER.info("doAction: decode: {}", ipmiMessage.toString());
            OpenSession openSession = new OpenSession(CipherSuite.getEmpty());
            if (openSession.isCommandResponse(ipmiMessage)
                    && TypeConverter.byteToInt((ipmiMessage.getPayload()).getPayloadData()[0]) == tag) {
                stateMachine.doExternalAction(new ResponseAction(CompletionCode.Ok,
                        openSession.getResponseData(ipmiMessage)));
            }
        } catch (IPMIException e) {
            stateMachine.doExternalAction(new ErrorAction(e));
        } catch (InvalidKeyException e) {
            stateMachine.doExternalAction(new ErrorAction(e));
        }
    }
}
