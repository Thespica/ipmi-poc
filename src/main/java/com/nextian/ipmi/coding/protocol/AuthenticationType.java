/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.coding.protocol;

/**
 * Available types of authentication. For IPMI v2.0 format RMCPPlus should be
 * used.
 */
public enum AuthenticationType {
    None(AuthenticationType.NONE), Md2(AuthenticationType.MD2), Md5(
            AuthenticationType.MD5), Simple(AuthenticationType.SIMPLE), Oem(
            AuthenticationType.OEM), RMCPPlus(AuthenticationType.RMCPPLUS),;

    private static final int NONE = 0;
    private static final int MD2 = 1;
    private static final int MD5 = 2;
    private static final int SIMPLE = 4;
    private static final int OEM = 5;
    private static final int RMCPPLUS = 6;

    private final int code;

    /**
     * Initialize enumeration related to given integer code
     *
     * @param code value mapped to enum value
     */
    AuthenticationType(int code) {
        this.code = code;
    }

    /**
     * Get integer representation of enum
     *
     * @return enum integer mapped value
     */
    public int getCode() {
        return code;
    }

    /**
     * Convert integer value (e.g received as response) to its appropriate enumeration
     *
     * @param value integer value to be converted
     * @return enumeration value related to given parameter
     * @throws IllegalArgumentException when provided state is out of range and does not match any defined enum value
     */
    public static AuthenticationType parseInt(int value) {
        switch (value) {
            case NONE:
                return None;
            case MD2:
                return Md2;
            case MD5:
                return Md5;
            case SIMPLE:
                return Simple;
            case OEM:
                return Oem;
            case RMCPPLUS:
                return RMCPPlus;
            default:
                throw new IllegalArgumentException("Invalid value: " + value);
        }
    }
}