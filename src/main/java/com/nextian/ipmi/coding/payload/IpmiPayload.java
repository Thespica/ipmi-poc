/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */

package com.nextian.ipmi.coding.payload;

import com.nextian.ipmi.coding.security.ConfidentialityAlgorithm;

import java.security.InvalidKeyException;

/**
 * Payload for IPMI messages. It handles both encoded data message as well as decoded Java message.
 */
public abstract class IpmiPayload {

    private byte[] data;

    private byte[] encryptedPayload;

    /**
     * Set payload message data in raw format
     *
     * @param data byte array of payload data
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Get payload message data in raw format
     *
     * @return bytes array oy payload message data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns encrypted payload encoded in byte array.
     * Might be null if payload was not yet encrypted.
     *
     * @see #encryptPayload(ConfidentialityAlgorithm)
     */
    public byte[] getEncryptedPayload() {
        return encryptedPayload;
    }

    /**
     * Returns unencrypted payload encoded in byte array (owner is responsible
     * for encryption).
     *
     * @return payload data
     */
    public abstract byte[] getPayloadData();

    /**
     * Get payload data length
     *
     * @return encoded but but not encrypted payload length.
     */
    public abstract int getPayloadLength();

    /**
     * Get payload command data
     *
     * @return IPMI command encapsulated in IPMI Payload.
     */
    public abstract byte[] getIpmiCommandData();

    /**
     * Encrypts {@link #getPayloadData()}.
     *
     * @param confidentialityAlgorithm {@link ConfidentialityAlgorithm} to be used to encrypt payload data.
     * @throws InvalidKeyException when confidentiality algorithm fails.
     * @see IpmiPayload#getEncryptedPayload()
     */
    public void encryptPayload(ConfidentialityAlgorithm confidentialityAlgorithm) throws InvalidKeyException {
        encryptedPayload = confidentialityAlgorithm.encrypt(getPayloadData());
    }

    @Override
    public String toString() {
        String str;

        str = String.format("length: :  %1$s%n", getPayloadLength());
        return str;
    }

}
