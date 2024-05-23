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
package com.nextian.ipmi.coding.security;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Interface for Confidentiality Algorithms. All classes extending this one must implement constructor(byte[]).
 */
public abstract class ConfidentialityAlgorithm {
    protected byte[] sik;

    /**
     * Initializes Confidentiality Algorithm.
     *
     * @param sik session Integrity Key calculated during the opening of the session or user password if 'one-key'
     *            logins are enabled.
     * @throws InvalidKeyException      when initiation of the algorithm fails
     * @throws NoSuchAlgorithmException when initiation of the algorithm fails
     * @throws NoSuchPaddingException   when initiation of the algorithm fails
     */
    public void initialize(byte[] sik, AuthenticationAlgorithm authenticationAlgorithm) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        this.sik = sik;
    }

    /**
     * Returns the algorithm's ID.
     */
    public abstract byte getCode();

    /**
     * Encrypts the data.
     *
     * @param data payload to be encrypted
     * @return encrypted data encapsulated in Confidentiality Header and Trailer.
     * @throws InvalidKeyException when initiation of the algorithm fails
     */
    public abstract byte[] encrypt(byte[] data) throws InvalidKeyException;

    /**
     * Decrypts the data.
     *
     * @param data encrypted data encapsulated in Confidentiality Header and Trailer.
     * @return decrypted data.
     * @throws IllegalArgumentException when initiation of the algorithm fails
     */
    public abstract byte[] decrypt(byte[] data) throws IllegalArgumentException;

    /**
     * Calculates size of the confidentiality header and trailer specific for the algorithm.
     *
     * @param payloadSize size of the data that will be encrypted
     */
    public abstract int getConfidentialityOverheadSize(int payloadSize);
}
