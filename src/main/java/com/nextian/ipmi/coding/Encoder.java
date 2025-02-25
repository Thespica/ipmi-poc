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
import com.nextian.ipmi.coding.protocol.IpmiMessage;
import com.nextian.ipmi.coding.protocol.decoder.Protocolv20Decoder;
import com.nextian.ipmi.coding.protocol.encoder.IpmiEncoder;
import com.nextian.ipmi.coding.rmcp.RmcpEncoder;
import com.nextian.ipmi.coding.rmcp.RmcpIpmiMessage;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Creates RMCP packet containing encrypted IPMI command from IPMICommand wrapper class or raw byte data.
 */
public final class Encoder {

    /**
     * Encodes IPMI command specified by commandCoder into byte array raw data.
     *
     * @param protocolEncoder instance of {@link IpmiEncoder} class for encoding of the IPMI session header.
     *                        {@link Protocolv15Decoder} or {@link Protocolv20Decoder} should be used (depending on
     *                        IPMI protocol version used).
     * @param commandCoder    instance of {@link IpmiCommandCoder} class used for building IPMI message payload
     * @param sequenceNumber  a generated sequence number used for matching request and response. If IPMI message is sent
     *                        in a session, it is used as a Session Sequence Number. For all IPMI messages,
     *                        sequenceNumber % 256 is used as a IPMI LAN Message sequence number and as an IPMI
     *                        payload message tag.
     * @param sessionId       ID of the managed system's session message is being sent in. For sessionless commands
     *                        should be set to 0.
     * @return encoded IPMI command
     * @throws NoSuchAlgorithmException when authentication, confidentiality or integrity algorithm fails.
     * @throws InvalidKeyException      when creating of the algorithm key fails
     */
    public static byte[] encode(IpmiEncoder protocolEncoder, IpmiCommandCoder commandCoder, int sequenceNumber,
                                int sessionId) throws NoSuchAlgorithmException, InvalidKeyException {
        IpmiMessage cmdMessage = commandCoder.encodeCommand(sequenceNumber, sessionId);
        // LOGGER.debug("Encode message: {}", cmdMessage.toString());
        RmcpIpmiMessage message = new RmcpIpmiMessage(protocolEncoder.encode(cmdMessage));
        return RmcpEncoder.encode(message);
    }

    private Encoder() {
    }
}
