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
 * Class representing no confidentiality algorithm.
 */
public class ConfidentialityNone extends ConfidentialityAlgorithm {

    public ConfidentialityNone() {
        super();
    }

    @Override
    public byte getCode() {
        return SecurityConstants.CA_NONE;
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return data;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return data;
    }

    @Override
    public int getConfidentialityOverheadSize(int payloadSize) {
        return 0;
    }

}
