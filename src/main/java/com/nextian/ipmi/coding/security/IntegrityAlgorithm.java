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
import com.nextian.ipmi.common.TypeConverter;

import java.security.InvalidKeyException;

/**
 * Interface for Integrity Algorithms. All classes extending this one must implement constructor(byte[]).
 */
public abstract class IntegrityAlgorithm {

    protected byte[] sik;

    public IntegrityAlgorithm() {

    }

    /**
     * Initializes Integrity Algorithm.
     *
     * @param sik session Integrity Key calculated during the opening of the session or user password if 'one-key'
     *            logins are enabled.
     * @throws InvalidKeyException when invalid integrity key is inuse
     */
    public void initialize(byte[] sik) throws InvalidKeyException {
        this.sik = sik;
    }

    /**
     * Returns the algorithm's ID.
     */
    public abstract byte getCode();

    /**
     * Creates AuthCode field for message.
     *
     * @param base data starting with the AuthType/Format field up to and including the field that immediately
     *             precedes the AuthCode field
     * @return AuthCode field. Might be null if empty AuthCOde field is generated.
     * @see Rakp1#calculateSik(Rakp1ResponseData)
     */
    public abstract byte[] generateAuthCode(byte[] base);

    /**
     * Modifies the algorithm base since with null Auth Code during encoding Integrity Pad isn't calculated.
     *
     * @param base           integrity algorithm base without Integrity Pad.
     * @param authCodeLength expected length of the Auth Code field.
     * @return integrity algorithm base with Integrity Pad and updated Pad Length field.
     */
    protected byte[] injectIntegrityPad(byte[] base, int authCodeLength) {
        int pad = 0;
        if ((base.length + authCodeLength) % 4 != 0) {
            pad = 4 - (base.length + authCodeLength) % 4;
        }

        if (pad != 0) {
            byte[] newBase = new byte[base.length + pad];

            System.arraycopy(base, 0, newBase, 0, base.length - 2);

            for (int i = base.length - 2; i < base.length - 2 + pad; ++i) {
                newBase[i] = TypeConverter.intToByte(0xff);
            }

            newBase[newBase.length - 2] = TypeConverter.intToByte(pad);

            newBase[newBase.length - 1] = base[base.length - 1];

            return newBase;
        } else {
            return base;
        }
    }
}
