/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.coding;

import com.nextian.ipmi.coding.commands.IpmiCommandCoder;
import com.nextian.ipmi.coding.commands.ResponseData;
import com.nextian.ipmi.coding.payload.CompletionCode;
import com.nextian.ipmi.coding.payload.lan.IPMIException;
import com.nextian.ipmi.coding.protocol.decoder.IpmiDecoder;
import com.nextian.ipmi.coding.protocol.decoder.PlainCommandv20Decoder;
import com.nextian.ipmi.coding.protocol.decoder.Protocolv20Decoder;
import com.nextian.ipmi.coding.rmcp.RmcpDecoder;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Decodes RMCP packet into {@link ResponseData}.
 */
public final class Decoder {

    /**
     * Decodes raw data into {@link ResponseData} - a wrapper class for
     * message-specific response data.
     *
     * @param data            aw RMCP packet to be decoded
     * @param protocolDecoder instance of {@link IpmiDecoder} class for decoding of the IPMI session header and
     *                        (if present) IPMI LAN packet. If IPMI LAN packet is present, {@link Protocolv15Decoder} or
     *                        {@link Protocolv20Decoder} should be used (depending on IPMI protocol version used).
     *                        Otherwise, {@link PlainCommandv20Decoder} should be used.
     * @param commandCoder    instance of {@link IpmiCommandCoder} class used for wrapping payload into
     *                        message-dependent {@link ResponseData} object.
     * @return {@link ResponseData}
     * @throws IPMIException            when request to the server fails.
     * @throws IllegalArgumentException when data is corrupted
     * @throws NoSuchAlgorithmException when authentication, confidentiality or integrity algorithm fails.
     * @throws InvalidKeyException      when creating of the authentication algorithm key fails
     * @see CompletionCode
     */
    public static ResponseData decode(byte[] data, IpmiDecoder protocolDecoder, IpmiCommandCoder commandCoder)
            throws IllegalArgumentException, IPMIException, NoSuchAlgorithmException, InvalidKeyException {
        return commandCoder.getResponseData(protocolDecoder.decode(RmcpDecoder.decode(data)));
    }

    private Decoder() {
    }
}
