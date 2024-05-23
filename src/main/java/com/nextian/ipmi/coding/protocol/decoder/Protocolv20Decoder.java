/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.coding.protocol.decoder;

import com.nextian.ipmi.coding.protocol.AuthenticationType;
import com.nextian.ipmi.coding.protocol.IpmiMessage;
import com.nextian.ipmi.coding.protocol.Ipmiv20Message;
import com.nextian.ipmi.coding.protocol.PayloadType;
import com.nextian.ipmi.coding.rmcp.RmcpMessage;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.coding.security.ConfidentialityNone;
import com.nextian.ipmi.common.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.security.InvalidKeyException;
import java.util.Arrays;

/**
 * Decodes IPMI v2.0 session header and retrieves encrypted payload.
 */
public class Protocolv20Decoder extends ProtocolDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Protocolv20Decoder.class);

    private CipherSuite cipherSuite;

    /**
     * Initiates IPMI v2.0 packet decoder.
     *
     * @param cipherSuite {@link CipherSuite} that will be used to decode the message
     */
    public Protocolv20Decoder(CipherSuite cipherSuite) {
        super();
        this.cipherSuite = cipherSuite;
    }

    public static PayloadType decodePayloadType(byte payloadType) throws IllegalArgumentException {
        return PayloadType.parseInt(TypeConverter.intToByte(payloadType & TypeConverter.intToByte(0x3f)));
    }

    /**
     * Decodes session ID.
     *
     * @param message message to get session ID from
     * @return Session ID.
     */
    public static int decodeSessionID(RmcpMessage message) {
        int offset = 2;
        if (decodePayloadType(message.getData()[1]) == PayloadType.Oem) {
            offset += 6;
        }
        return decodeSessionID(message.getData(), offset);
    }

    /**
     * Decodes IPMI v2.0 message fields.
     *
     * @param rmcpMessage RMCP message to decode.
     * @return decoded message
     * @throws IllegalArgumentException when delivered RMCP message does not contain encapsulated IPMI message
     *                                  or when AuthCode field is incorrect (integrity check fails)
     * @throws InvalidKeyException      when initiation of the integrity algorithm fails
     * @see Ipmiv20Message
     */
    @Override
    public IpmiMessage decode(RmcpMessage rmcpMessage) throws IllegalArgumentException, InvalidKeyException {
        Ipmiv20Message message = new Ipmiv20Message(cipherSuite.getConfidentialityAlgorithm());

        byte[] raw = rmcpMessage.getData();

        message.setAuthenticationType(decodeAuthenticationType(raw[0]));

        message.setPayloadEncrypted(decodeEncryption(raw[1]));

        message.setPayloadAuthenticated(decodeAuthentication(raw[1]));

        message.setPayloadType(decodePayloadType(raw[1]));

        int offset = 2;

        if (message.getPayloadType() == PayloadType.Oem) {
            message.setOemIANA(decodeOEMIANA(raw));
            offset += 4;

            message.setOemPayloadID(decodeOEMPayloadId(raw, offset));
            offset += 2;
        }

        message.setSessionID(decodeSessionID(raw, offset));
        offset += 4;

        message.setSessionSequenceNumber(decodeSessionSequenceNumber(raw,
                offset));
        offset += 4;

        int payloadLength = decodePayloadLength(raw, offset);
        offset += 2;

        if (message.isPayloadEncrypted()) {
            message.setPayload(decodePayload(raw, offset, payloadLength, message.getConfidentialityAlgorithm()));
        } else {
            message.setPayload(decodePayload(raw, offset, payloadLength, new ConfidentialityNone()));
        }

        offset += payloadLength;

        if (message.getAuthenticationType() != AuthenticationType.None
                && !(message.getAuthenticationType() == AuthenticationType.RMCPPlus &&
                !message.isPayloadAuthenticated()) && message.getSessionID() != 0) {
            offset = skipIntegrityPAD(raw, offset);
            message.setAuthCode(decodeAuthCode(raw, offset));
            if (!validateAuthCode(raw, offset)) {
                LOGGER.warn("Integrity check failed");
            }
        }

        return message;
    }

    /**
     * Decodes first bit of Payload Type.
     *
     * @param payloadType raw message data of payload type
     * @return True if payload is encrypted, false otherwise.
     */
    private boolean decodeEncryption(byte payloadType) {
        return (payloadType & TypeConverter.intToByte(0x80)) != 0;
    }

    /**
     * Decodes second bit of Payload Type.
     *
     * @param payloadType raw message data of payload type
     * @return True if payload is authenticated, false otherwise.
     */
    public boolean decodeAuthentication(byte payloadType) {
        return (payloadType & TypeConverter.intToByte(0x40)) != 0;
    }

    /**
     * Decodes OEM IANA.
     *
     * @param rawMessage byte array holding whole message data.
     * @return OEM IANA number.
     */
    private int decodeOEMIANA(byte[] rawMessage) {
        byte[] oemIANA = new byte[4];

        System.arraycopy(rawMessage, 3, oemIANA, 0, 3);
        oemIANA[3] = 0;

        return TypeConverter.littleEndianByteArrayToInt(oemIANA);
    }

    /**
     * Decodes OEM payload ID. To implement manufacturer-specific OEM Payload ID decoding, override this function.
     *
     * @param rawMessage byte array holding whole message data.
     * @param offset     offset to OEM payload ID in header.
     * @return Decoded OEM payload ID.
     */
    protected Object decodeOEMPayloadId(byte[] rawMessage, int offset) {
        byte[] oemPayload = new byte[2];

        System.arraycopy(rawMessage, offset, oemPayload, 0, 2);

        return oemPayload;
    }

    @Override
    protected int decodePayloadLength(byte[] rawData, int offset) {
        byte[] payloadLength = new byte[4];
        System.arraycopy(rawData, offset, payloadLength, 0, 2);
        payloadLength[2] = 0;
        payloadLength[3] = 0;

        return TypeConverter.littleEndianByteArrayToInt(payloadLength);
    }

    /**
     * Skips the integrity pad and pad length fields.
     *
     * @param rawMessage byte array holding whole message data.
     * @param offset     offset to integrity pad.
     * @return Offset to Auth Code
     * @throws IndexOutOfBoundsException when message is corrupted and pad length does not appear after integrity
     *                                   pad or length is incorrect.
     */
    private int skipIntegrityPAD(byte[] rawMessage, int offset) throws IndexOutOfBoundsException {
        int skip = 0;
        int offsetPos = offset;

        while (TypeConverter.byteToInt(rawMessage[offsetPos + skip]) == 0xff) {
            ++skip;
        }
        int length = TypeConverter.byteToInt(rawMessage[offsetPos + skip]);
        if (length != skip) {
            throw new IndexOutOfBoundsException("Message is corrupted.");
        }
        offsetPos += skip + 2; // skip pad length and next header fields
        if (offsetPos >= rawMessage.length) {
            throw new IndexOutOfBoundsException("Message is corrupted.");
        }
        return offsetPos;
    }

    /**
     * Decodes the Auth Code.
     *
     * @param rawMessage byte array holding whole message data
     * @param offset     offset to auth code.
     * @return Auth Code
     * @throws IndexOutOfBoundsException when message is corrupted and pad length does not appear after integrity
     *                                   pad or length is incorrect.
     */
    private byte[] decodeAuthCode(byte[] rawMessage, int offset) {
        byte[] authCode = new byte[rawMessage.length - offset];
        System.arraycopy(rawMessage, offset, authCode, 0, authCode.length);
        return authCode;
    }

    /**
     * Checks if Auth Code of the received message is valid.
     *
     * @param rawMessage received message
     * @param offset     offset to the AuthCode field in the message
     * @return True if AuthCode is correct, false otherwise
     */
    private boolean validateAuthCode(byte[] rawMessage, int offset) {
        boolean isValid;
        byte[] base = new byte[offset];

        System.arraycopy(rawMessage, 0, base, 0, offset);

        byte[] authCode = null;

        if (rawMessage.length > offset) {
            authCode = new byte[rawMessage.length - offset];
            System.arraycopy(rawMessage, offset, authCode, 0, authCode.length);
        }

        byte[] expectedAuthCode = cipherSuite.getIntegrityAlgorithm().generateAuthCode(base);
        isValid = Arrays.equals(authCode, expectedAuthCode);
        DumpAuthCode("Auth received", authCode);
        DumpAuthCode("Auth expected", expectedAuthCode);
        if (!isValid)
            LOGGER.warn("Received invalid authentication code");
        return isValid;
    }

    private void DumpAuthCode(String prefix, byte[] buffer) {
        String msg;

        msg = DatatypeConverter.printHexBinary(buffer);
        LOGGER.debug("{} {}", prefix, msg);
    }
}
