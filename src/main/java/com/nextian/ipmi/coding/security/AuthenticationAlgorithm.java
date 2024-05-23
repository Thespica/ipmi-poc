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

import com.nextian.ipmi.coding.commands.session.Rakp1;
import com.nextian.ipmi.coding.commands.session.Rakp1ResponseData;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Interface for authentication algorithms. All classes extending this one must  have a parameterless constructor.
 */
public abstract class AuthenticationAlgorithm {

    /**
     * @return algorithm-specific code
     */
    public abstract byte getCode();

    /**
     * Get name of algorithm used to generate secret key
     * @return name of algorithm ({@link javax.crypto.spec.SecretKeySpec})
     */
    public abstract String getSecretKeyAlgorithmName();

    /**
     * @return length of the key for the RAKP2 message
     */
    public abstract int getKeyLength();

    /**
     * @return length of the integrity check base for RAKP4 message
     */
    public abstract int getIntegrityCheckBaseLength();

    /**
     * Checks value of the Key Exchange Authentication Code in RAKP messages
     *
     * @param data     the base for authentication algorithm. Depends on RAKP Message.
     * @param key      the Key Exchange Authentication Code to check.
     * @param password password of the user establishing a session
     * @return True if authentication check was successful, false otherwise.
     * @throws NoSuchAlgorithmException when initiation of the algorithm fails
     * @throws InvalidKeyException      when creating of the algorithm key failsS
     */
    public abstract boolean checkKeyExchangeAuthenticationCode(byte[] data, byte[] key, String password)
            throws NoSuchAlgorithmException, InvalidKeyException;

    /**
     * Calculates value of the Key Exchange Authentication Code in RAKP messages
     *
     * @param data     the base for authentication algorithm. Depends on RAKP Message.
     * @param password password of the user establishing a session
     * @throws NoSuchAlgorithmException when initiation of the algorithm fails
     * @throws InvalidKeyException      when creating of the algorithm key fails
     */
    public abstract byte[] getKeyExchangeAuthenticationCode(byte[] data, String password)
            throws NoSuchAlgorithmException, InvalidKeyException;

    /**
     * Validates Integrity Check Value in RAKP Message 4.
     *
     * @param data      the base for authentication algorithm
     * @param reference the Integrity Check Value to validate
     * @param sik       the Session Integrity Key generated on base of RAKP Messages 1 and 2
     * @return True if integrity check was successful, false otherwise
     * @throws NoSuchAlgorithmException when initiation of the algorithm fails
     * @throws InvalidKeyException      when creating of the algorithm key fails
     * @see Rakp1#calculateSik(Rakp1ResponseData)
     */
    public abstract boolean doIntegrityCheck(byte[] data, byte[] reference,
                                             byte[] sik) throws InvalidKeyException, NoSuchAlgorithmException;
}
