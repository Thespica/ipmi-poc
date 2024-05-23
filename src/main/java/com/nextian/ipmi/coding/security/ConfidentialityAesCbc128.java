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

import com.nextian.ipmi.common.TypeConverter;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * AES-CBC-128 confidentiality algorithm
 */
public class ConfidentialityAesCbc128 extends ConfidentialityAlgorithm {

    private static final byte[] CONST2 = new byte[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};

    private Cipher cipher;

    private SecretKeySpec cipherKey;

    @Override
    public byte getCode() {
        return SecurityConstants.CA_AES_CBC128;
    }

    @Override
    public void initialize(byte[] sik, AuthenticationAlgorithm authenticationAlgorithm) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        super.initialize(sik, authenticationAlgorithm);

        SecretKeySpec k2 = new SecretKeySpec(sik, authenticationAlgorithm.getSecretKeyAlgorithmName());

        Mac mac = Mac.getInstance(authenticationAlgorithm.getSecretKeyAlgorithmName());
        mac.init(k2);

        byte[] ckey = mac.doFinal(CONST2);

        byte[] ciphKey = new byte[16];

        System.arraycopy(ckey, 0, ciphKey, 0, 16);

        cipherKey = new SecretKeySpec(ciphKey, "AES");

        cipher = Cipher.getInstance("AES/CBC/NoPadding");
    }

    @Override
    public byte[] encrypt(byte[] data) throws InvalidKeyException {
        int length = data.length + 17;
        int pad = 0;
        if (length % 16 != 0) {
            pad = 16 - length % 16;
        }
        length += pad;

        byte[] result = new byte[length - 16];

        cipher.init(Cipher.ENCRYPT_MODE, cipherKey);

        System.arraycopy(data, 0, result, 0, data.length);

        for (int i = 0; i < pad; ++i) {
            result[i + data.length] = TypeConverter.intToByte(i + 1);
        }

        result[length - 17] = TypeConverter.intToByte(pad);

        try {
            byte[] encrypted = cipher.doFinal(result);

            result = new byte[encrypted.length + 16];

            System.arraycopy(encrypted, 0, result, 16, encrypted.length); // encrypted payload
            System.arraycopy(cipher.getIV(), 0, result, 0, 16); // Initialization  vector

            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public byte[] decrypt(byte[] data) throws IllegalArgumentException {

        byte[] decrypted = null;
        try {
            byte[] iv = new byte[16];

            System.arraycopy(data, 0, iv, 0, 16);

            byte[] encrypted = new byte[data.length - 16];

            System.arraycopy(data, 16, encrypted, 0, encrypted.length);

            cipher.init(Cipher.DECRYPT_MODE, cipherKey, new IvParameterSpec(iv));
            decrypted = cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new IllegalArgumentException("Decryption failed", e);
        }

        int pad = TypeConverter.byteToInt(decrypted[decrypted.length - 1]);

        byte[] result = new byte[decrypted.length - pad - 1];

        System.arraycopy(decrypted, 0, result, 0, result.length);

        return result;
    }

    @Override
    public int getConfidentialityOverheadSize(int payloadSize) {
        int size = 17;
        if ((size + payloadSize) % 16 != 0) {
            size += 16 - (size + payloadSize) % 16;
        }
        return size;
    }

}
