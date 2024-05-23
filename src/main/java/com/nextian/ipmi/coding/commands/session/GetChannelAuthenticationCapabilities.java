/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.coding.commands.session;

import com.nextian.ipmi.coding.commands.CommandCodes;
import com.nextian.ipmi.coding.commands.IpmiCommandCoder;
import com.nextian.ipmi.coding.commands.IpmiVersion;
import com.nextian.ipmi.coding.commands.PrivilegeLevel;
import com.nextian.ipmi.coding.commands.ResponseData;
import com.nextian.ipmi.coding.payload.CompletionCode;
import com.nextian.ipmi.coding.payload.IpmiPayload;
import com.nextian.ipmi.coding.payload.lan.IPMIException;
import com.nextian.ipmi.coding.payload.lan.IpmiLanRequest;
import com.nextian.ipmi.coding.payload.lan.IpmiLanResponse;
import com.nextian.ipmi.coding.payload.lan.NetworkFunction;
import com.nextian.ipmi.coding.protocol.AuthenticationType;
import com.nextian.ipmi.coding.protocol.IpmiMessage;
import com.nextian.ipmi.coding.protocol.Ipmiv15Message;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.common.TypeConverter;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Wrapper for Get Channel Authentication Capabilities request
 */
public class GetChannelAuthenticationCapabilities extends IpmiCommandCoder {

    private PrivilegeLevel requestedPrivilegeLevel;

    private byte channelNumber;

    private IpmiVersion requestVersion;

    /**
     * Set channel authentication privileged level
     * @param requestedPrivilegeLevel privilege level value
     */
    public void setRequestedPrivilegeLevel(PrivilegeLevel requestedPrivilegeLevel) {
        this.requestedPrivilegeLevel = requestedPrivilegeLevel;
    }


    /**
     * Get channel authentication privileged level
     * @return request privilege level value
     */
    public PrivilegeLevel getRequestedPrivilegeLevel() {
        return requestedPrivilegeLevel;
    }

    /**
     * Sets the channel number that will be put into IPMI command.
     *
     * @param channelNumber must be 0h-Bh or Eh-Fh <br>
     *                      Eh = retrieve information for channel this request was issued on
     * @throws IllegalArgumentException when wrong channel number is provided
     */
    public void setChannelNumber(int channelNumber) throws IllegalArgumentException {
        if (channelNumber < 0 || channelNumber > 0xF || channelNumber == 0xC || channelNumber == 0xD) {
            throw new IllegalArgumentException("Invalid channel number");
        }
        this.channelNumber = TypeConverter.intToByte(channelNumber);
    }

    /**
     * Get chanel number used in IPMI command.
     *
     * @return channel number
     */
    public int getChannelNumber() {
        return TypeConverter.byteToInt(channelNumber);
    }

    /**
     * Set IPMI version to be used during communication
     *
     * @param requestVersion IPMI version
     */
    protected void setRequestVersion(IpmiVersion requestVersion) {
        this.requestVersion = requestVersion;
    }

    /**
     * Get requested IPMI version to be used during communication
     *
     * @return requested IPMI version
     */
    protected IpmiVersion getRequestVersion() {
        return requestVersion;
    }

    /**
     * Initiates class for decoding in 1.5 version. Sets requested privilege
     * level to user. Sets channel number to 14 indicating that response will
     * contain information for channel this request was issued on. Sets session
     * parameters to default.
     *
     * @see IpmiCommandCoder#setSessionParameters(IpmiVersion, CipherSuite, AuthenticationType)
     * @see IpmiVersion
     */
    public GetChannelAuthenticationCapabilities() {
        super();
        setRequestedPrivilegeLevel(PrivilegeLevel.User);
        setChannelNumber(14);
    }

    /**
     * Initiates class. Sets IPMI version to version. Sets requested privilege level to user. Sets channel number
     * to 14 indicating that response will contain information for channel this request was issued on.
     *
     * @param version        version of IPMI protocol used
     * @param requestVersion if Get Channel Authentication Capabilities command is sent to BMC
     *                       with requestVersion = {@link IpmiVersion#V15} it will respond, that it does not
     *                       support IPMI v2.0 even if it does
     * @param cipherSuite    {@link CipherSuite} containing authentication, confidentiality and integrity algorithms for
     *                       this session
     * @see IpmiVersion
     */
    public GetChannelAuthenticationCapabilities(IpmiVersion version, IpmiVersion requestVersion,
                                                CipherSuite cipherSuite) {
        super(version, cipherSuite, AuthenticationType.None);
        this.setRequestVersion(requestVersion);
        setRequestedPrivilegeLevel(PrivilegeLevel.User);
        setChannelNumber(14);
    }

    /**
     * Initiates class. Sets IPMI version to version. Sets requested privilege
     * level privilegeLevel. Sets channel number to channelNumber.
     *
     * @param version        version of IPMI protocol used
     * @param requestVersion if Get Channel Authentication Capabilities command is sent to BMC
     *                       with requestVersion = {@link IpmiVersion#V15} it will respond, that it does not support
     *                       IPMI v2.0 even if it does
     * @param cipherSuite    {@link CipherSuite} containing authentication, confidentiality and integrity algorithms for
     *                       this session
     * @param privilegeLevel maximum requested privilege level. Can't be {@link PrivilegeLevel#MaximumAvailable}.
     * @param channelNumber  must be 0h-Bh or Eh-Fh <br>
     *                       Eh = retrieve information for channel this request was issued on
     * @see IpmiVersion
     * @see PrivilegeLevel
     */
    public GetChannelAuthenticationCapabilities(IpmiVersion version, IpmiVersion requestVersion,
                                                CipherSuite cipherSuite, PrivilegeLevel privilegeLevel,
                                                byte channelNumber) {
        super(version, cipherSuite, AuthenticationType.None);
        this.setRequestVersion(requestVersion);
        setRequestedPrivilegeLevel(privilegeLevel);
        setChannelNumber(channelNumber);
    }

    @Override
    public IpmiMessage encodeCommand(int sequenceNumber, int sessionId)
            throws InvalidKeyException, NoSuchAlgorithmException {
        if (getIpmiVersion() == IpmiVersion.V15) {
            if (sessionId != 0) {
                throw new IllegalArgumentException("Session ID must be 0");
            }

            Ipmiv15Message message = new Ipmiv15Message();

            message.setAuthenticationType(getAuthenticationType());

            message.setSessionSequenceNumber(0);

            message.setSessionID(0);

            message.setPayload(preparePayload(sequenceNumber));

            return message;
        } else {
            setAuthenticationType(AuthenticationType.RMCPPlus);

            return super.encodeCommand(sequenceNumber, sessionId);
        }
    }

    @Override
    protected IpmiPayload preparePayload(int sequenceNumber) {
        byte[] payload = new byte[2];

        payload[0] = 0;

        if (getRequestVersion() == IpmiVersion.V20) {
            payload[0] |= TypeConverter.intToByte(0x80);
        }

        payload[0] |= channelNumber;

        payload[1] = encodePrivilegeLevel(requestedPrivilegeLevel);
        return new IpmiLanRequest(getNetworkFunction(), getCommandCode(), payload,
                TypeConverter.intToByte(sequenceNumber % 64));
    }

    @Override
    public byte getCommandCode() {
        return CommandCodes.GET_CHANNEL_AUTHENTICATION_CAPABILITIES;
    }

    @Override
    public NetworkFunction getNetworkFunction() {
        return NetworkFunction.ApplicationRequest;
    }

    @Override
    public ResponseData getResponseData(IpmiMessage message) throws IllegalArgumentException, IPMIException {
        if (!isCommandResponse(message)) {
            throw new IllegalArgumentException(
                    "This is not a response for Get Channel Authentication Capabilities command");
        }
        if (!(message.getPayload() instanceof IpmiLanResponse)) {
            throw new IllegalArgumentException("Invalid response payload");
        }
        if (((IpmiLanResponse) message.getPayload()).getCompletionCode() != CompletionCode.Ok) {
            throw new IPMIException(((IpmiLanResponse) message.getPayload()).getCompletionCode());
        }
        GetChannelAuthenticationCapabilitiesResponseData responseData = new GetChannelAuthenticationCapabilitiesResponseData();

        byte[] raw = message.getPayload().getIpmiCommandData();

        if (raw.length != 8) {
            throw new IllegalArgumentException("Data has invalid length");
        }

        responseData.setChannelNumber(raw[0]);

        responseData.setIpmiv20Support((raw[1] & 0x80) != 0);

        responseData.setAuthenticationTypes(new ArrayList<AuthenticationType>());

        if ((raw[1] & 0x20) != 0) {
            responseData.getAuthenticationTypes().add(AuthenticationType.Oem);
        }

        if ((raw[1] & 0x10) != 0) {
            responseData.getAuthenticationTypes().add(AuthenticationType.Simple);
        }

        if ((raw[1] & 0x04) != 0) {
            responseData.getAuthenticationTypes().add(AuthenticationType.Md5);
        }

        if ((raw[1] & 0x02) != 0) {
            responseData.getAuthenticationTypes().add(AuthenticationType.Md2);
        }

        if ((raw[1] & 0x01) != 0) {
            responseData.getAuthenticationTypes().add(AuthenticationType.None);
        }

        responseData.setKgEnabled((raw[2] & 0x20) != 0);

        responseData.setPerMessageAuthenticationEnabled((raw[2] & 0x10) == 0);

        responseData.setUserLevelAuthenticationEnabled((raw[2] & 0x08) == 0);

        responseData.setNonNullUsernamesEnabled((raw[2] & 0x04) != 0);

        responseData.setNullUsernamesEnabled((raw[2] & 0x02) != 0);

        responseData.setAnonymousLoginEnabled((raw[2] & 0x01) != 0);

        byte[] oemId = new byte[4];

        System.arraycopy(raw, 4, oemId, 0, 3);

        oemId[3] = 0;

        responseData.setOemId(TypeConverter.littleEndianByteArrayToInt(oemId));

        responseData.setOemData(raw[7]);

        return responseData;
    }

    /**
     * Sets session parameters.
     *
     * @param version            IPMI version of the command
     * @param cipherSuite        {@link CipherSuite} containing authentication, confidentiality and integrity algorithms for
     *                           this session
     * @param authenticationType type of authentication used. Must be RMCPPlus for IPMI v2.0
     */
    @Override
    public void setSessionParameters(IpmiVersion version, CipherSuite cipherSuite,
                                     AuthenticationType authenticationType) {

        if (version == IpmiVersion.V20
                && authenticationType != AuthenticationType.RMCPPlus
                && authenticationType != AuthenticationType.None) {
            throw new IllegalArgumentException("Authentication Type must be RMCPPlus for IPMI v2.0 messages");
        }

        setIpmiVersion(version);
        setAuthenticationType(authenticationType);
        setCipherSuite(cipherSuite);
    }

}
