/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */

package com.nextian.ipmi.coding.commands.chassis;

import com.nextian.ipmi.coding.commands.CommandCodes;
import com.nextian.ipmi.coding.commands.IpmiCommandCoder;
import com.nextian.ipmi.coding.commands.IpmiVersion;
import com.nextian.ipmi.coding.commands.ResponseData;
import com.nextian.ipmi.coding.payload.CompletionCode;
import com.nextian.ipmi.coding.payload.lan.*;
import com.nextian.ipmi.coding.protocol.AuthenticationType;
import com.nextian.ipmi.coding.protocol.IpmiMessage;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.common.TypeConverter;

/**
 * Get Chassis Status request.
 */
public class GetChassisStatus extends IpmiCommandCoder {

    /**
     * Initiates GetChassisStatus for encoding and decoding.
     *
     * @param version            IPMI version of the command
     * @param cipherSuite        {@link CipherSuite} containing authentication, confidentiality and integrity algorithms for this session.
     * @param authenticationType type of authentication used (must be RMCPPlus for IPMI v2.0)
     */
    public GetChassisStatus(IpmiVersion version, CipherSuite cipherSuite, AuthenticationType authenticationType) {
        super(version, cipherSuite, authenticationType);

        if (version == IpmiVersion.V20 && authenticationType != AuthenticationType.RMCPPlus) {
            throw new IllegalArgumentException("Authentication Type must be RMCPPlus for IPMI v2.0 messages");
        }
    }

    @Override
    protected IpmiLanMessage preparePayload(int sequenceNumber) {
        return new IpmiLanRequest(getNetworkFunction(), getCommandCode(), null,
                TypeConverter.intToByte(sequenceNumber % 64));
    }

    @Override
    public byte getCommandCode() {
        return CommandCodes.GET_CHASSIS_STATUS;
    }

    @Override
    public NetworkFunction getNetworkFunction() {
        return NetworkFunction.ChassisRequest;
    }

    @Override
    public ResponseData getResponseData(IpmiMessage message)
            throws IllegalArgumentException, IPMIException {
        if (!isCommandResponse(message)) {
            throw new IllegalArgumentException("Invalid response for Get Chassis Status command");
        }
        if (!(message.getPayload() instanceof IpmiLanResponse)) {
            throw new IllegalArgumentException("Invalid response payload");
        }
        if (((IpmiLanResponse) message.getPayload()).getCompletionCode() != CompletionCode.Ok) {
            throw new IPMIException(((IpmiLanResponse) message.getPayload()).getCompletionCode());
        }

        byte[] raw = message.getPayload().getIpmiCommandData();

        // CValidate payload length. It must contain at least 3 bytes (or optionally 4 when
        // Front Panel Button Capabilities option is supported)
        if (raw == null || (raw.length != 3 && raw.length != 4)) {
            throw new IllegalArgumentException("Invalid response payload length");
        }

        GetChassisStatusResponseData responseData = new GetChassisStatusResponseData();

        responseData.setCurrentPowerState(raw[0]);
        responseData.setLastPowerEvent(raw[1]);
        responseData.setMiscChassisState(raw[2]);

        // Check if optional data is present
        if (raw.length == 4) {
            responseData.setFrontPanelButtonCapabilities(raw[3]);
        }

        return responseData;
    }
}
