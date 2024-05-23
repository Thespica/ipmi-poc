/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */

package com.nextian.ipmi.coding.commands;

import com.nextian.ipmi.common.TypeConverter;

/**
 * Contains codes for IPMI commands. Byte constants are encoded as pseudo unsigned bytes. IpmiLanConstants doesn't use
 * {@link TypeConverter} because fields need to be runtime constants.
 *
 * @see TypeConverter#byteToInt(byte)
 * @see TypeConverter#intToByte(int)
 */
public final class CommandCodes {

    /**
     * An IPMI code for Get Chassis Status command
     */
    public static final byte GET_CHASSIS_STATUS = 0x01;

    /**
     * An IPMI code for Chassis Control command
     */
    public static final byte CHASSIS_CONTROL = 0x02;

    /**
     * An IPMI code for Get FRU Inventory Area Info command
     */
    public static final byte GET_FRU_INVENTORY_AREA_INFO = 0x10;

    /**
     * An IPMI code for Read FRU Data command
     */
    public static final byte READ_FRU_DATA = 0x11;

    /**
     * An IPMI code for Get Device SDR Info command
     */
    public static final byte GET_SDR_REPOSITORY_INFO = 0x20;

    /**
     * An IPMI code for Get Device SDR Info command
     */
    public static final byte GET_DEVICE_SDR_INFO = 0x21;

    /**
     * An IPMI code for Reserve SDR Repository command
     */
    public static final byte RESERVE_SDR_REPOSITORY = 0x22;

    /**
     * An IPMI code for Get SDR command
     */
    public static final byte GET_SDR = 0x23;

    /**
     * An IPMI code for Get Channel Authentication Capabilities command
     */
    public static final byte GET_CHANNEL_AUTHENTICATION_CAPABILITIES = 0x38;

    /**
     * An IPMI code for Set Session Privilege Level command
     */
    public static final byte SET_SESSION_PRIVILEGE_LEVEL = 0x3B;

    /**
     * An IPMI code for Get SEL Info command
     */
    public static final byte GET_SEL_INFO = 0x40;

    /**
     * An IPMI code for Reserve SEL command
     */
    public static final byte RESERVE_SEL = 0x42;

    /**
     * An IPMI code for Get SEL Entry command
     */
    public static final byte GET_SEL_ENTRY = 0x43;

    /**
     * An IPMI code for Get Channel Cipher Suites command
     */
    public static final byte GET_CHANNEL_CIPHER_SUITES = 0x54;


    private CommandCodes() {
    }

    /**
     * Convert IPMI byte code to is appropriate name.
     *
     * @param code byte code of IPMI command to be converted
     * @return name of IPMI command
     */
    public static String getCommandCodeName(byte code) {
        String name;
        switch (code) {
            case GET_CHASSIS_STATUS:
                name = "GET_CHASSIS_STATUS";
                break;
            case CHASSIS_CONTROL:
                name = "CHASSIS_CONTROL";
                break;
            case GET_FRU_INVENTORY_AREA_INFO:
                name = "GET_FRU_INVENTORY_AREA_INFO";
                break;
            case READ_FRU_DATA:
                name = "READ_FRU_DATA";
                break;
            case GET_SDR_REPOSITORY_INFO:
                name = "GET_SDR_REPOSITORY_INFO";
                break;
            case GET_DEVICE_SDR_INFO:
                name = "GET_DEVICE_SDR_INFO";
                break;
            case RESERVE_SDR_REPOSITORY:
                name = "RESERVE_SDR_REPOSITORY";
                break;
            case GET_SDR:
                name = "GET_SDR";
                break;
            case GET_CHANNEL_AUTHENTICATION_CAPABILITIES:
                name = "GET_CHANNEL_AUTHENTICATION_CAPABILITIES";
                break;
            case SET_SESSION_PRIVILEGE_LEVEL:
                name = "SET_SESSION_PRIVILEGE_LEVEL";
                break;
            case GET_SEL_INFO:
                name = "GET_SEL_INFO";
                break;
            case RESERVE_SEL:
                name = "RESERVE_SEL";
                break;
            case GET_SEL_ENTRY:
                name = "GET_SEL_ENTRY";
                break;
            case GET_CHANNEL_CIPHER_SUITES:
                name = "GET_CHANNEL_CIPHER_SUITES";
                break;
            default:
                name = "CMD_UNKNOWN";
        }
        return name;
    }
}
