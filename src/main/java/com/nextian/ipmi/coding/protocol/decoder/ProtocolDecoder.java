/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.coding.protocol.decoder;

import com.nextian.ipmi.coding.payload.IpmiPayload;
import com.nextian.ipmi.coding.payload.lan.IpmiLanResponse;
import com.nextian.ipmi.coding.protocol.AuthenticationType;
import com.nextian.ipmi.coding.protocol.IpmiMessage;
import com.nextian.ipmi.coding.rmcp.RmcpClassOfMessage;
import com.nextian.ipmi.coding.rmcp.RmcpMessage;
import com.nextian.ipmi.coding.security.ConfidentialityAlgorithm;
import com.nextian.ipmi.common.TypeConverter;

/**
 * Decodes IPMI session header and retrieves encrypted payload. Payload must be IPMI LAN format message.
 */
public abstract class ProtocolDecoder implements IpmiDecoder {

    public ProtocolDecoder() {

    }

    /**
     * Decodes IPMI message version independent fields.
     *
     * @param rmcpMessage          RMCP message to decode.
     * @param message              a reference to message being decoded.
     * @param sequenceNumberOffset protocol version specific offset to Session Sequence Number field in the header
     *                             of the IPMI message.
     * @param payloadLengthOffset  protocol version specific offset to IPMI Payload Length field in the header
     *                             of the IPMI message.
     * @param payloadLengthLength  length of the payload length field.
     * @return Offset to the session trailer.
     * @throws IllegalArgumentException when delivered RMCP message does not contain encapsulated IPMI message.
     * @see IpmiMessage
     */
    @Deprecated
    protected int decode(RmcpMessage rmcpMessage, IpmiMessage message,
                         int sequenceNumberOffset, int payloadLengthOffset,
                         int payloadLengthLength) throws IllegalArgumentException {

        if (rmcpMessage.getClassOfMessage() != RmcpClassOfMessage.Ipmi) {
            throw new IllegalArgumentException("This is not an IPMI message");
        }

        byte[] raw = rmcpMessage.getData();

        message.setAuthenticationType(decodeAuthenticationType(raw[0]));

        message.setSessionSequenceNumber(decodeSessionSequenceNumber(raw, sequenceNumberOffset));

        message.setPayloadLength(decodePayloadLength(raw, payloadLengthOffset));

        message.setPayload(decodePayload(raw, payloadLengthOffset + payloadLengthLength, message.getPayloadLength(),
                message.getConfidentialityAlgorithm()));

        return payloadLengthOffset + payloadLengthLength + message.getPayloadLength();
    }

    protected static AuthenticationType decodeAuthenticationType(byte authenticationType)
            throws IllegalArgumentException {
        authenticationType &= TypeConverter.intToByte(0x0f);
        return AuthenticationType.parseInt(TypeConverter.byteToInt(authenticationType));
    }

    /**
     * Decodes {@link AuthenticationType} of the message so that the version of the IPMI protocol could be determined.
     *
     * @param message RMCP message to decode.
     * @return {@link AuthenticationType} of the message.
     */
    public static AuthenticationType decodeAuthenticationType(RmcpMessage message) {
        return decodeAuthenticationType(message.getData()[0]);
    }

    /**
     * Decodes int in a little endian convention from raw message at given offset
     *
     * @param rawMessage raw message to be decoded
     * @param offset     offset to integer value in buffer
     * @return decoded integer
     */
    protected static int decodeInt(byte[] rawMessage, int offset) {
        byte[] result = new byte[4];

        System.arraycopy(rawMessage, offset, result, 0, 4);

        return TypeConverter.littleEndianByteArrayToInt(result);
    }

    /**
     * Decodes session sequence number.
     *
     * @param rawMessage byte array holding whole message data.
     * @param offset     offset to session sequence number in header
     * @return Session Sequence number.
     */
    protected int decodeSessionSequenceNumber(byte[] rawMessage, int offset) {
        return decodeInt(rawMessage, offset);
    }

    /**
     * Decodes session ID.
     *
     * @param rawMessage byte array holding whole message data.
     * @param offset     offset to session ID in header.
     * @return Session ID.
     */
    protected static int decodeSessionID(byte[] rawMessage, int offset) {
        return decodeInt(rawMessage, offset);
    }

    /**
     * Decodes payload length.
     *
     * @param rawData byte array holding whole message data.
     * @param offset  offset to payload length in header.
     * @return payload length.
     */
    protected abstract int decodePayloadLength(byte[] rawData, int offset);

    /**
     * Decodes payload.
     *
     * @param rawData                  byte array holding whole message data.
     * @param offset                   offset to payload.
     * @param length                   length of the payload.
     * @param confidentialityAlgorithm {@link ConfidentialityAlgorithm} required to decrypt payload.
     * @return Payload decoded into {@link IpmiLanResponse}.
     */
    protected IpmiPayload decodePayload(byte[] rawData, int offset, int length,
                                        ConfidentialityAlgorithm confidentialityAlgorithm) {
        byte[] payload = null;
        if (length > 0) {
            payload = new byte[length];

            System.arraycopy(rawData, offset, payload, 0, length);

            payload = confidentialityAlgorithm.decrypt(payload);
        }
        return new IpmiLanResponse(payload);
    }
}
