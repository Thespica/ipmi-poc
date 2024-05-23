/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.coding.protocol.encoder;

import com.nextian.ipmi.coding.protocol.AuthenticationType;
import com.nextian.ipmi.common.TypeConverter;

/**
 * Adds IPMI session header to encrypted payload. Should be used to encode regular RMCP+ messages (excluding Open
 * Session and RAKP Messages)
 */
public abstract class ProtocolEncoder implements IpmiEncoder {

    protected byte encodeAuthenticationType(AuthenticationType authenticationType) {
        return TypeConverter.intToByte(authenticationType.getCode());
    }

    /**
     * Converts integer value into byte array using little endian convention and inserts it into message at given offset.
     *
     * @param value integer value to be encoded
     * @param message IPMI message being created
     * @param offset message offset to place integer value
     * @throws IndexOutOfBoundsException when message is too short to hold value at given offset
     */
    protected void encodeInt(int value, byte[] message, int offset) throws IndexOutOfBoundsException {
        byte[] array = TypeConverter.intToLittleEndianByteArray(value);

        if (array.length + offset > message.length) {
            throw new IndexOutOfBoundsException("Message is too short");
        }

        System.arraycopy(array, 0, message, offset, array.length);
    }

    /**
     * Encodes session sequence number and inserts it into message at given offset.
     *
     * @param value   session sequence number
     * @param message IPMI message being created
     * @param offset  message offset to place sequence id
     * @throws IndexOutOfBoundsException when message is too short to hold value at given offset
     */
    protected void encodeSessionSequenceNumber(int value, byte[] message, int offset) throws IndexOutOfBoundsException {
        encodeInt(value, message, offset);
    }

    /**
     * Encodes session id and inserts it into message at given offset.
     *
     * @param value   session session id
     * @param message IPMI message being created
     * @param offset  message offset to place sequence id
     * @throws IndexOutOfBoundsException when message is too short to hold value at given offset
     */
    protected void encodeSessionId(int value, byte[] message, int offset) throws IndexOutOfBoundsException {
        encodeInt(value, message, offset);
    }

    /**
     * Encodes payload length and inserts it into message at given offset.
     *
     * @param value   payload message length
     * @param message IPMI message being created
     * @param offset  message offset to place sequence id
     * @throws IndexOutOfBoundsException when message is too short to hold value at given offset
     */
    protected abstract void encodePayloadLength(int value, byte[] message, int offset) throws IndexOutOfBoundsException;

    /**
     * Encodes payload and inserts it into message at given offset. <br>
     * When payload == null it will not be inserted.
     *
     * @param payload IPMI payload to encode.
     * @param message IPMI message being created.
     * @param offset  message offset to place sequence id
     * @return offset + encoded message length
     * @throws IndexOutOfBoundsException when message is too short to hold value at given offset
     */
    protected int encodePayload(byte[] payload, byte[] message, int offset) throws IndexOutOfBoundsException {
        if (payload == null) {
            return offset;
        }
        if (payload.length + offset > message.length) {
            throw new IndexOutOfBoundsException("Message is too short");
        }
        System.arraycopy(payload, 0, message, offset, payload.length);
        return offset + payload.length;
    }
}
