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
import com.nextian.ipmi.coding.payload.lan.IPMIException;
import com.nextian.ipmi.coding.payload.lan.IpmiLanResponse;
import com.nextian.ipmi.coding.protocol.AuthenticationType;
import com.nextian.ipmi.coding.protocol.IpmiMessage;
import com.nextian.ipmi.coding.protocol.PayloadType;
import com.nextian.ipmi.coding.protocol.decoder.ProtocolDecoder;
import com.nextian.ipmi.coding.protocol.decoder.Protocolv20Decoder;
import com.nextian.ipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.nextian.ipmi.coding.rmcp.RmcpMessage;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.common.TypeConverter;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.actions.ErrorAction;
import com.nextian.ipmi.sm.actions.ResponseAction;
import com.nextian.ipmi.sm.events.DefaultAck;
import com.nextian.ipmi.sm.events.GetChannelCipherSuitesPending;
import com.nextian.ipmi.sm.events.StateMachineEvent;
import com.nextian.ipmi.sm.events.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * State at which getting Channel Cipher Suites is in progress. Transits back to {@link Uninitialized} on
 * {@link Timeout}, further proceeds with getting
 * Cipher Suites on {@link GetChannelCipherSuitesPending} and moves on to
 * {@link Ciphers} on {@link DefaultAck}
 */
public class CiphersWaiting extends State {

    private static final Logger LOGGER = LoggerFactory.getLogger(CiphersWaiting.class);

    private int index;

    private int tag;

    /**
     * Initializes state.
     *
     * @param index index of the channel cipher suite package to get
     * @param tag   tag of the message
     */
    public CiphersWaiting(int index, int tag) {
        this.index = index;
        this.tag = tag;
    }

    @Override
    public void doTransition(StateMachine stateMachine, StateMachineEvent machineEvent) throws InterruptedException {
        if (machineEvent instanceof Timeout) {
            stateMachine.setCurrent(new Uninitialized());
        } else if (machineEvent instanceof GetChannelCipherSuitesPending) {
            GetChannelCipherSuitesPending event = (GetChannelCipherSuitesPending) machineEvent;
            GetChannelCipherSuites cipherSuites = new GetChannelCipherSuites(
                    TypeConverter.intToByte(0xE),
                    TypeConverter.intToByte(index + 1));
            try {
                tag = event.getSequenceNumber();
                stateMachine.sendMessage(Encoder.encode(new Protocolv20Encoder(), cipherSuites,
                        event.getSequenceNumber(), 0));
                ++index;
            } catch (IOException e) {
                stateMachine.doExternalAction(new ErrorAction(e));
            } catch (NoSuchAlgorithmException e) {
                stateMachine.doExternalAction(new ErrorAction(e));
            } catch (InvalidKeyException e) {
                stateMachine.doExternalAction(new ErrorAction(e));
            }
        } else if (machineEvent instanceof DefaultAck) {
            stateMachine.setCurrent(new Ciphers());
        } else {
            stateMachine.doExternalAction(new ErrorAction(new IllegalArgumentException("Invalid transition")));
        }
    }

    @Override
    public void doAction(StateMachine stateMachine, RmcpMessage message) throws InterruptedException {
        if (ProtocolDecoder.decodeAuthenticationType(message) != AuthenticationType.RMCPPlus) {
            return;    //this isn't IPMI v2.0 message so we ignore it
        }
        if (Protocolv20Decoder.decodeSessionID(message) != 0) {
            return;    //this isn't sessionless message so we drop it
        }
        if (Protocolv20Decoder.decodePayloadType(message.getData()[1]) != PayloadType.Ipmi) {
            return;
        }
        Protocolv20Decoder decoder = new Protocolv20Decoder(CipherSuite.getEmpty());
        if (decoder.decodeAuthentication(message.getData()[1])) {
            return;    //message is authenticated so it does belong to the other session
        }
        IpmiMessage ipmiMessage = null;
        try {
            ipmiMessage = decoder.decode(message);
            LOGGER.debug("doAction: decode: {}", ipmiMessage.toString());
            GetChannelCipherSuites suites = new GetChannelCipherSuites();
            if (suites.isCommandResponse(ipmiMessage)) {
                IpmiLanResponse ipmiLanResponse = (IpmiLanResponse) ipmiMessage.getPayload();
                if (TypeConverter.byteToInt(ipmiLanResponse.getSequenceNumber()) == tag) {
                    stateMachine.doExternalAction(new ResponseAction(ipmiLanResponse.getCompletionCode(),
                            suites.getResponseData(ipmiMessage)));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            stateMachine.doExternalAction(new ErrorAction(e));
        } catch (IPMIException e) {
            stateMachine.doExternalAction(new ErrorAction(e));
        } catch (InvalidKeyException e) {
            stateMachine.doExternalAction(new ErrorAction(e));
        }
    }

}
