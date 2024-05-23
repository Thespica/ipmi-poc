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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * RAKP-HMAC-SHA1 authentication algorithm.
 */
public class AuthenticationRakpHmacSha1 extends AuthenticationAlgorithm {

    private static final String HMACSHA1_TAG = "HmacSHA1";
    private Mac mac;

    /**
     * Initiates RAKP-HMAC-SHA1 authentication algorithm.
     *
     * @throws NoSuchAlgorithmException when initiation of the algorithm fails
     */
    public AuthenticationRakpHmacSha1() throws NoSuchAlgorithmException {
        mac = Mac.getInstance(HMACSHA1_TAG);
    }

    @Override
    public byte getCode() {
        return SecurityConstants.AA_RAKP_HMAC_SHA1;
    }

    @Override
    public String getSecretKeyAlgorithmName() {
        return HMACSHA1_TAG;
    }

    @Override
    public boolean checkKeyExchangeAuthenticationCode(byte[] data, byte[] key, String password)
            throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] check = getKeyExchangeAuthenticationCode(data, password);
        return Arrays.equals(check, key);
    }

    @Override
    public byte[] getKeyExchangeAuthenticationCode(byte[] data, String password)
            throws NoSuchAlgorithmException, InvalidKeyException {

        byte[] key = password.getBytes();

        SecretKeySpec sKey = new SecretKeySpec(key, HMACSHA1_TAG);
        mac.init(sKey);

        return mac.doFinal(data);
    }

    @Override
    public boolean doIntegrityCheck(byte[] data, byte[] reference, byte[] sik)
            throws InvalidKeyException, NoSuchAlgorithmException {

        SecretKeySpec sKey = new SecretKeySpec(sik, HMACSHA1_TAG);
        mac.init(sKey);

        byte[] result = new byte[getIntegrityCheckBaseLength()];

        System.arraycopy(mac.doFinal(data), 0, result, 0, getIntegrityCheckBaseLength());

        return Arrays.equals(result, reference);
    }

    @Override
    public int getKeyLength() {
        return 20;
    }

    @Override
    public int getIntegrityCheckBaseLength() {
        return 12;
    }

}
