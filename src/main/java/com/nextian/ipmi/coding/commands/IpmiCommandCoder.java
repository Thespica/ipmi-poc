/*
 * Copyright 2022 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nextian.ipmi.coding.commands;

import com.nextian.ipmi.coding.payload.IpmiPayload;
import com.nextian.ipmi.coding.payload.PlainMessage;
import com.nextian.ipmi.coding.payload.lan.IPMIException;
import com.nextian.ipmi.coding.payload.lan.IpmiLanResponse;
import com.nextian.ipmi.coding.payload.lan.NetworkFunction;
import com.nextian.ipmi.coding.protocol.AuthenticationType;
import com.nextian.ipmi.coding.protocol.IpmiMessage;
import com.nextian.ipmi.coding.protocol.Ipmiv15Message;
import com.nextian.ipmi.coding.protocol.Ipmiv20Message;
import com.nextian.ipmi.coding.protocol.PayloadType;
import com.nextian.ipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.coding.security.SecurityConstants;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * A wrapper for IPMI command.
 * <p>
 * Parameterless constructors in classes derived from IpmiCommandCoder are meant
 * to be used for decoding. For requests encoding the appropriate constructors with parameters are used.
 */
public abstract class IpmiCommandCoder {

    /**
     * Version of IPMI protocol in use.
     */
    private IpmiVersion ipmiVersion;

    /**
     * Authentication type used. Default == None;
     */
    private AuthenticationType authenticationType;

    /**
     * Cipher Suite used for IPMI command requests
     */
    private CipherSuite cipherSuite;

    /**
     * Set IMPI protocol version to be used.
     *
     * @param ipmiVersion IPMI version
     */
    public void setIpmiVersion(IpmiVersion ipmiVersion) {
        this.ipmiVersion = ipmiVersion;
    }

    /**
     * Get IMPI protocol version defined for the command.
     *
     * @return IPMI protocol version
     */
    public IpmiVersion getIpmiVersion() {
        return ipmiVersion;
    }

    /**
     * Set authentication type used for command request.
     *
     * @param authenticationType authentication type
     */
    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    /**
     * GSet authentication type used for command request.
     *
     * @return authentication type
     */
    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    /**
     * Set Cipher Suite to be used for command.
     *
     * @param cipherSuite Cipher Suite definition
     */
    public void setCipherSuite(CipherSuite cipherSuite) {
        this.cipherSuite = cipherSuite;
    }

    /**
     * Get Cipher Suite to be used for command.
     *
     * @return Cipher Suite definition
     */
    public CipherSuite getCipherSuite() {
        return cipherSuite;
    }

    /**
     * Initialize command default parameters. This constructor is supposed to be used for responses.
     */
    public IpmiCommandCoder() {
        setSessionParameters(IpmiVersion.V20, CipherSuite.getEmpty(), AuthenticationType.RMCPPlus);
    }

    /**
     * Initialize command with minimum set of required parameters. This constructor is supposed to be used for request
     * creation.
     *
     * @param version            IMPI version
     * @param cipherSuite        Cipher Suite to be used in IPMI request
     * @param authenticationType type of authentication to be used in IPMI request
     */
    public IpmiCommandCoder(IpmiVersion version, CipherSuite cipherSuite, AuthenticationType authenticationType) {
        setSessionParameters(version, cipherSuite, authenticationType);
    }

    /**
     * Sets session parameters.
     *
     * @param version            IPMI version of the command.
     * @param cipherSuite        {@link CipherSuite} containing authentication, confidentiality and integrity algorithms
     *                           for this session.
     * @param authenticationType type of authentication used. Must be RMCPPlus for IPMI v2.0
     */
    public void setSessionParameters(IpmiVersion version, CipherSuite cipherSuite,
                                     AuthenticationType authenticationType) {

        if (version == IpmiVersion.V20 && authenticationType != AuthenticationType.RMCPPlus) {
            throw new IllegalArgumentException("Authentication Type must be RMCPPlus for IPMI v2.0 messages");
        }

        setIpmiVersion(version);
        setAuthenticationType(authenticationType);
        setCipherSuite(cipherSuite);
    }

    /**
     * Prepares an IPMI request message containing class-specific command
     *
     * @param sequenceNumber generated sequence number used for matching request and response. If IPMI message is sent
     *                       in a session, it is used as a Session Sequence Number. For all IPMI messages,
     *                       sequenceNumber % 256 is used as a IPMI LAN Message sequence number and as an IPMI
     *                       payload message tag
     * @param sessionId      ID of the managed system's session message is being sent in. For sessionless commands
     *                       should be set to 0.
     * @return IPMI message
     * @throws NoSuchAlgorithmException when authentication, confidentiality or integrity algorithm fails
     * @throws InvalidKeyException      when creating of the algorithm key fails
     */
    public IpmiMessage encodeCommand(int sequenceNumber, int sessionId)
            throws NoSuchAlgorithmException, InvalidKeyException {
        if (getIpmiVersion() == IpmiVersion.V15) {
            Ipmiv15Message message = new Ipmiv15Message();

            message.setAuthenticationType(getAuthenticationType());

            message.setSessionID(sessionId);

            message.setSessionSequenceNumber(sequenceNumber);

            message.setPayload(preparePayload(sequenceNumber));

            return message;
        } else /* IPMI version 2.0 */ {
            Ipmiv20Message message = new Ipmiv20Message(getCipherSuite().getConfidentialityAlgorithm());

            message.setAuthenticationType(getAuthenticationType());

            message.setSessionID(sessionId);

            message.setSessionSequenceNumber(sequenceNumber);

            message.setPayloadType(PayloadType.Ipmi);

            message.setPayloadAuthenticated(getCipherSuite().getIntegrityAlgorithm().getCode() !=
                    SecurityConstants.IA_NONE);

            message.setPayloadEncrypted(getCipherSuite().getConfidentialityAlgorithm().getCode() !=
                    SecurityConstants.CA_NONE);

            message.setPayload(preparePayload(sequenceNumber));

            message.setAuthCode(getCipherSuite().getIntegrityAlgorithm().generateAuthCode(
                    message.getIntegrityAlgorithmBase(new Protocolv20Encoder())));

            return message;
        }
    }

    /**
     * Checks if given message contains response command specific for this class.
     *
     * @param message to be checked
     * @return True if message contains response command specific for this class, false otherwise
     */
    public boolean isCommandResponse(IpmiMessage message) {
        if (message.getPayload() instanceof IpmiLanResponse) {
            return ((IpmiLanResponse) message.getPayload()).getCommand() == getCommandCode();
        } else {
            return message.getPayload() instanceof PlainMessage;
        }
    }

    /**
     * Retrieves command code specific for command represented by this class.
     *
     * @return command code
     */
    public abstract byte getCommandCode();

    /**
     * Retrieves network function specific for command represented by this class.
     *
     * @return network function
     * @see NetworkFunction
     */
    public abstract NetworkFunction getNetworkFunction();

    /**
     * Prepares {@link IpmiPayload} to be encoded. Called from {@link #encodeCommand(int, int)}.
     *
     * @param sequenceNumber sequenceNumber % 256 is used as an IPMI payload message tag
     * @return IPMI payload
     * @throws NoSuchAlgorithmException when authentication, confidentiality or integrity algorithm fails.
     * @throws InvalidKeyException      when creating of the algorithm key fails
     */
    protected abstract IpmiPayload preparePayload(int sequenceNumber)
            throws NoSuchAlgorithmException, InvalidKeyException;

    /**
     * Retrieves command-specific response data from IPMI message.
     *
     * @param message IPMI message
     * @return response data
     * @throws IllegalArgumentException when message is not a response for class-specific command or response has
     *                                  invalid length
     * @throws IPMIException            when response completion code isn't OK
     * @throws NoSuchAlgorithmException when authentication, confidentiality or integrity algorithm fails
     * @throws InvalidKeyException      when creating of the authentication algorithm key fails
     */
    public abstract ResponseData getResponseData(IpmiMessage message)
            throws IllegalArgumentException, IPMIException, NoSuchAlgorithmException, InvalidKeyException;

    /**
     * Used in several derived classes - converts {@link PrivilegeLevel} to byte.
     *
     * @param privilegeLevel privilege level to be encoded
     * @return privilegeLevel encoded as a byte due to {@link CommandsConstants}
     */
    protected byte encodePrivilegeLevel(PrivilegeLevel privilegeLevel) {
        switch (privilegeLevel) {
            case MaximumAvailable:
                return CommandsConstants.AL_HIGHEST_AVAILABLE;
            case Callback:
                return CommandsConstants.AL_CALLBACK;
            case User:
                return CommandsConstants.AL_USER;
            case Operator:
                return CommandsConstants.AL_OPERATOR;
            case Administrator:
                return CommandsConstants.AL_ADMINISTRATOR;
            default:
                throw new IllegalArgumentException("Invalid privilege level");
        }
    }
}
