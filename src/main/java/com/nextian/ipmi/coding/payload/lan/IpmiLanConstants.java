/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.coding.payload.lan;

import com.nextian.ipmi.common.TypeConverter;

/**
 * Set of constants.
 * Byte constants are encoded as pseudo unsigned bytes.
 * IpmiLanConstants doesn't use {@link TypeConverter} because fields need to be runtime constants.
 *
 * @see TypeConverter#byteToInt(byte)
 * @see TypeConverter#intToByte(int)
 */
public final class IpmiLanConstants {
    /**
     * The address of the BMC.
     */
    public static final byte BMC_ADDRESS = 0x20;

    /**
     * The address of the remote console.
     */
    public static final byte REMOTE_CONSOLE_ADDRESS = (byte) (0x81 - 256);

    private IpmiLanConstants() {
    }
}
