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

/**
 * HMAC-MD5-128 integrity algorithm.
 */
public class IntegrityHmacMD5_128 extends IntegrityAlgorithm {

    private Mac mac;

    private static final byte[] CONST1 = new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    private static final String HMACMD5_TAG = "HmacMD5";

    /**
     * Initiates HMAC-SHA1-96 integrity algorithm.
     *
     * @throws NoSuchAlgorithmException when initiation of the algorithm fails
     */
    public IntegrityHmacMD5_128() throws NoSuchAlgorithmException {
        mac = Mac.getInstance(HMACMD5_TAG);
    }

    @Override
    public void initialize(byte[] sik) throws InvalidKeyException {
        super.initialize(sik);

        SecretKeySpec k1 = new SecretKeySpec(sik, HMACMD5_TAG);

        mac.init(k1);

        k1 = new SecretKeySpec(mac.doFinal(CONST1), HMACMD5_TAG);

        mac.init(k1);
    }

    @Override
    public byte getCode() {
        return SecurityConstants.IA_HMAC_MD5_128;
    }

    @Override
    public byte[] generateAuthCode(byte[] base) {

        if (sik == null) {
            throw new NullPointerException("Algorithm not initialized.");
        }

        byte[] baseArray;
        if (base[base.length - 2] == 0 /*there are no integrity pad bytes*/) {
            baseArray = injectIntegrityPad(base, 16);
        }  else {
            baseArray = base;
        }

        byte[] result = new byte[16];
        System.arraycopy(mac.doFinal(baseArray), 0, result, 0, 16);

        return result;
    }

}
