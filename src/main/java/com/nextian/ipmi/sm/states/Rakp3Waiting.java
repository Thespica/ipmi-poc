/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.states;

import com.nextian.ipmi.coding.commands.session.Rakp1;
import com.nextian.ipmi.coding.commands.session.Rakp1ResponseData;
import com.nextian.ipmi.coding.commands.session.Rakp3;
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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * At this point of session challenge, RAKP Message 3 was sent,
 * {@link StateMachine} is waiting for RAKP Message 4.<br>
 * Transition to: <li> {@link Rakp3Complete} on {@link DefaultAck} <li>
 * {@link Authcap} on {@link Timeout}
 */
public class Rakp3Waiting extends State {

    private Rakp1 rakp1;
    private Rakp1ResponseData rakp1ResponseData;
    private CipherSuite cipherSuite;
    private int tag;

    /**
     * Initiates state.
     *
     * @param rakp1             the {@link Rakp1} message that was sent earlier in the authentication process.
     * @param rakp1ResponseData the {@link Rakp1ResponseData} that was received earlier in the authentification process.
     * @param cipherSuite       the {@link CipherSuite} used during this session
     */
    public Rakp3Waiting(int tag, Rakp1 rakp1,
                        Rakp1ResponseData rakp1ResponseData, CipherSuite cipherSuite) {
        this.rakp1 = rakp1;
        this.rakp1ResponseData = rakp1ResponseData;
        this.cipherSuite = cipherSuite;
        this.tag = tag;
    }

    @Override
    public void doTransition(StateMachine stateMachine, StateMachineEvent machineEvent) throws InterruptedException {
        if (machineEvent instanceof DefaultAck) {
            stateMachine.setCurrent(new Rakp3Complete());
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
        PlainCommandv20Decoder decoder = new PlainCommandv20Decoder(CipherSuite.getEmpty());
        if (Protocolv20Decoder.decodePayloadType(message.getData()[1]) != PayloadType.Rakp4) {
            return;
        }

        IpmiMessage ipmiMessage = null;
        Rakp3 rakp3 = new Rakp3(cipherSuite, rakp1, rakp1ResponseData);
        try {
            ipmiMessage = decoder.decode(message);
            if (rakp3.isCommandResponse(ipmiMessage)
                    && TypeConverter.byteToInt((ipmiMessage.getPayload()).getPayloadData()[0]) == tag) {
                stateMachine.doExternalAction(new ResponseAction(CompletionCode.Ok, rakp3.getResponseData(ipmiMessage)));
            }
        } catch (NoSuchAlgorithmException e) {
            stateMachine.doExternalAction(new ErrorAction(e));
        } catch (InvalidKeyException e) {
            stateMachine.doExternalAction(new ErrorAction(e));
        } catch (IPMIException e) {
            stateMachine.doExternalAction(new ErrorAction(e));
        }
    }
}
