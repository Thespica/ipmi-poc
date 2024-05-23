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
import com.nextian.ipmi.coding.commands.PrivilegeLevel;
import com.nextian.ipmi.coding.commands.session.CloseSession;
import com.nextian.ipmi.coding.commands.session.GetChannelAuthenticationCapabilities;
import com.nextian.ipmi.coding.protocol.AuthenticationType;
import com.nextian.ipmi.coding.protocol.Ipmiv20Message;
import com.nextian.ipmi.coding.protocol.PayloadType;
import com.nextian.ipmi.coding.protocol.decoder.ProtocolDecoder;
import com.nextian.ipmi.coding.protocol.decoder.Protocolv20Decoder;
import com.nextian.ipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.nextian.ipmi.coding.rmcp.RmcpMessage;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.common.TypeConverter;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.actions.ErrorAction;
import com.nextian.ipmi.sm.actions.MessageAction;
import com.nextian.ipmi.sm.events.Sendv20Message;
import com.nextian.ipmi.sm.events.SessionUpkeep;
import com.nextian.ipmi.sm.events.StateMachineEvent;
import com.nextian.ipmi.sm.events.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * {@link State} in which the session is valid and sending IPMI commands to the remote machine is enabled.
 * <li>Sends an IPMI v2.0 message on {@link Sendv20Message}
 * <li>Sends {@link GetChannelAuthenticationCapabilities} message to keep the session form timing out on
 * {@link SessionUpkeep}
 * <li> Transits to {@link Authcap} on {@link Timeout}
 * <li>Sends {@link CloseSession} and transits to {@link Authcap} on {@link com.nextian.ipmi.sm.events.CloseSession}
 */
public class SessionValid extends State {

    private static Logger LOGGER = LoggerFactory.getLogger(SessionValid.class);
    private CipherSuite cipherSuite;

    private int sessionId;

    /**
     * Initiates the state.
     *
     * @param cipherSuite {@link CipherSuite} used during the session.
     */
    public SessionValid(CipherSuite cipherSuite, int sessionId) {
        this.cipherSuite = cipherSuite;
        this.sessionId = sessionId;
    }

    public CipherSuite getCipherSuite() {
        return cipherSuite;
    }

    @Override
    public void doTransition(StateMachine stateMachine, StateMachineEvent machineEvent) throws InterruptedException {
        if (machineEvent instanceof Sendv20Message) {
            Sendv20Message event = (Sendv20Message) machineEvent;
            try {
                stateMachine.sendMessage(Encoder.encode(new Protocolv20Encoder(), event.getCommandCoder(),
                        event.getSequenceNumber(), event.getSessionId()));
            } catch (InvalidKeyException e) {
                stateMachine.doExternalAction(new ErrorAction(e));
            } catch (NoSuchAlgorithmException e) {
                stateMachine.doExternalAction(new ErrorAction(e));
            } catch (IOException e) {
                stateMachine.doExternalAction(new ErrorAction(e));
            }
        } else if (machineEvent instanceof SessionUpkeep) {
            SessionUpkeep event = (SessionUpkeep) machineEvent;
            try {
                stateMachine.sendMessage(Encoder.encode(new Protocolv20Encoder(),
                        new GetChannelAuthenticationCapabilities(IpmiVersion.V20, IpmiVersion.V20, cipherSuite,
                                PrivilegeLevel.Callback, TypeConverter.intToByte(0xe)), event.getSequenceNumber(),
                        event.getSessionId()));
            } catch (InvalidKeyException e) {
                stateMachine.doExternalAction(new ErrorAction(e));
            } catch (NoSuchAlgorithmException e) {
                stateMachine.doExternalAction(new ErrorAction(e));
            } catch (IOException e) {
                stateMachine.doExternalAction(new ErrorAction(e));
            }
        } else if (machineEvent instanceof Timeout) {
            stateMachine.setCurrent(new Authcap());
        } else if (machineEvent instanceof com.nextian.ipmi.sm.events.CloseSession) {
            com.nextian.ipmi.sm.events.CloseSession event = (com.nextian.ipmi.sm.events.CloseSession) machineEvent;

            try {
                stateMachine.setCurrent(new Authcap());
                stateMachine.sendMessage(Encoder.encode(new Protocolv20Encoder(),
                        new CloseSession(IpmiVersion.V20, cipherSuite, AuthenticationType.RMCPPlus, event.getSessionId()),
                        event.getSequenceNumber(), event.getSessionId()));
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
    public void doAction(StateMachine stateMachine, RmcpMessage message) throws InterruptedException {
        if (ProtocolDecoder.decodeAuthenticationType(message) != AuthenticationType.RMCPPlus) {
            return; // this isn't IPMI v2.0 message so we ignore it
        }
        if (Protocolv20Decoder.decodeSessionID(message) == 0) {
            return; // this is a sessionless message so we drop it
        }
        Protocolv20Decoder decoder = new Protocolv20Decoder(cipherSuite);
        if (Protocolv20Decoder.decodePayloadType(message.getData()[1]) != PayloadType.Ipmi) {
            return;
        }
        if (Protocolv20Decoder.decodeSessionID(message) != sessionId) {
            return; // this message belongs to other session so we ignore it
        }
        try {
            Ipmiv20Message message20 = (Ipmiv20Message) decoder.decode(message);
            LOGGER.debug("doAction: decode: {}", message20.toString());
            if (message20.getSessionID() == sessionId) {
                stateMachine.doExternalAction(new MessageAction(message20));
            }
        } catch (InvalidKeyException e) {
            stateMachine.doExternalAction(new ErrorAction(e));
        }
    }

}
