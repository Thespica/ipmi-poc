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
import com.nextian.ipmi.coding.commands.ResponseData;
import com.nextian.ipmi.coding.payload.lan.IPMIException;
import com.nextian.ipmi.coding.payload.lan.IpmiLanMessage;
import com.nextian.ipmi.coding.payload.lan.IpmiLanRequest;
import com.nextian.ipmi.coding.payload.lan.NetworkFunction;
import com.nextian.ipmi.coding.protocol.AuthenticationType;
import com.nextian.ipmi.coding.protocol.IpmiMessage;
import com.nextian.ipmi.coding.protocol.Ipmiv20Message;
import com.nextian.ipmi.coding.protocol.PayloadType;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.coding.security.ConfidentialityNone;
import com.nextian.ipmi.common.TypeConverter;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Wrapper for RMCP+ Get Channel Cipher Suites command. This command can be
 * executed prior to establishing a session with the BMC.
 */
public class GetChannelCipherSuites extends IpmiCommandCoder {

    private byte channelNumber;

    private byte index;

    /**
     * Sets the channel number that will be put into IPMI command.
     *
     * @param channelNumber must be 0h-Bh or Eh-Fh <br>
     *                      Eh = retrieve information for channel this request was issued on
     * @throws IllegalArgumentException when illegal channel number is provided
     */
    public void setChannelNumber(int channelNumber) throws IllegalArgumentException {
        if (channelNumber < 0 || channelNumber > 0xF || channelNumber == 0xC || channelNumber == 0xD) {
            throw new IllegalArgumentException("Invalid channel number");
        }
        this.channelNumber = TypeConverter.intToByte(channelNumber);
    }

    public int getChannelNumber() {
        return TypeConverter.byteToInt(channelNumber);
    }

    public void setIndex(byte index) {
        if (index > 0x3F || index < 0) {
            throw new IllegalArgumentException("Index " + index + " invalid must be (00h-3Fh).");
        }
        this.index = index;
    }

    public byte getIndex() {
        return index;
    }

    /**
     * Initiates class for decoding.
     */
    public GetChannelCipherSuites() {
        super(IpmiVersion.V20, new CipherSuite((byte) 0, (byte) 0, (byte) 0, (byte) 0), AuthenticationType.RMCPPlus);
    }

    /**
     * Initiates class for both encoding and decoding.
     *
     * @param channelNumber must be 0h-Bh or Eh-Fh <br>
     *                      Eh = retrieve information for channel this request was issued
     *                      on
     * @param index         (00h-3Fh). 0h selects the first set of 16 cipher suites, 1h
     *                      selects the next set of 16, and so on
     */
    public GetChannelCipherSuites(byte channelNumber, byte index) {
        super(IpmiVersion.V20, new CipherSuite((byte) 0, (byte) 0, (byte) 0,
                (byte) 0), AuthenticationType.RMCPPlus);
        setChannelNumber(channelNumber);
        setIndex(index);
    }

    @Override
    public IpmiMessage encodeCommand(int sequenceNumber, int sessionId)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Ipmiv20Message message = new Ipmiv20Message(new ConfidentialityNone());

        message.setAuthenticationType(getAuthenticationType());

        message.setSessionID(0);

        message.setPayloadEncrypted(false);

        message.setPayloadAuthenticated(false);

        message.setSessionSequenceNumber(0);

        message.setPayloadType(PayloadType.Ipmi);

        message.setPayload(preparePayload(sequenceNumber));

        return message;
    }

    @Override
    protected IpmiLanMessage preparePayload(int sequenceNumber) {
        byte[] requestData = new byte[3];

        requestData[0] = channelNumber;

        requestData[1] = 0; // payload type = IPMI

        requestData[2] = TypeConverter.intToByte(0x80 | getIndex());

        return new IpmiLanRequest(getNetworkFunction(), getCommandCode(),
                requestData, TypeConverter.intToByte(sequenceNumber % 64));
    }

    @Override
    public byte getCommandCode() {
        return CommandCodes.GET_CHANNEL_CIPHER_SUITES;
    }

    @Override
    public NetworkFunction getNetworkFunction() {
        return NetworkFunction.ApplicationRequest;
    }

    @Override
    public ResponseData getResponseData(IpmiMessage message)
            throws IllegalArgumentException, IPMIException, NoSuchAlgorithmException, InvalidKeyException {

        GetChannelCipherSuitesResponseData data;

        byte[] raw = message.getPayload().getIpmiCommandData();
        if (raw != null && raw.length > 0) {
            data = new GetChannelCipherSuitesResponseData();

            data.setChannelNumber(raw[0]);
            if (raw.length > 1) {
                byte[] cssData = new byte[raw.length - 1];

                System.arraycopy(raw, 1, cssData, 0, cssData.length);

                data.setCipherSuiteData(cssData);
            } else if (raw.length == 1) {
                data.setCipherSuiteData(new byte[0]);
            }
        } else {
            data = null;
        }

        return data;
    }

}
