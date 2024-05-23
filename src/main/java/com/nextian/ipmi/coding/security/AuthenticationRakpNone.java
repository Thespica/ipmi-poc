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

/**
 * RAKP-None authentication algorithm.
 */
public class AuthenticationRakpNone extends AuthenticationAlgorithm {

    @Override
    public byte getCode() {
        return SecurityConstants.AA_RAKP_NONE;
    }

    @Override
    public String getSecretKeyAlgorithmName() {
        return "";
    }

    /**
     * Checks value of the Key Exchange Authentication Code in RAKP messages using the RAKP-None algorithm.
     */
    @Override
    public boolean checkKeyExchangeAuthenticationCode(byte[] data, byte[] key, String password) {
        return true;
    }

    /**
     * Calculates value of the Key Exchange Authentication Code in RAKP messages using the RAKP-None algorithm.
     */
    @Override
    public byte[] getKeyExchangeAuthenticationCode(byte[] data, String password) {
        return new byte[0];
    }

    /**
     * Performs Integrity Check in RAKP 4 message using the RAKP-None algorithm.
     */
    @Override
    public boolean doIntegrityCheck(byte[] data, byte[] reference, byte[] sik) {
        return true;
    }

    @Override
    public int getKeyLength() {
        return 0;
    }

    @Override
    public int getIntegrityCheckBaseLength() {
        return 0;
    }

}
