/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.coding.rmcp;

/**
 * Types of RMCP messages.
 */
public enum RmcpClassOfMessage {
    /**
     * ASF ACK Class of Message
     */
    Ack(RmcpClassOfMessage.ACK), Asf(RmcpClassOfMessage.ASF),
    /**
     * OEM-defined Class of Message
     */
    Oem(RmcpClassOfMessage.OEM), Ipmi(RmcpClassOfMessage.IPMI),;

    private static final int ACK = 134;
    private static final int ASF = 6;
    private static final int OEM = 8;
    private static final int IPMI = 7;

    private int code;

    /**
     * Initialize enumeration related to given integer code
     *
     * @param code value mapped to enum value
     */
    RmcpClassOfMessage(int code) {
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
    public static RmcpClassOfMessage parseInt(int value) {
        switch (value) {
            case ACK:
                return Ack;
            case ASF:
                return Asf;
            case OEM:
                return Oem;
            case IPMI:
                return Ipmi;
            default:
                throw new IllegalArgumentException("Invalid value: " + value);
        }
    }
}