/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */

package com.nextian.ipmi.coding.commands.chassis;

/**
 * Chassis Identify State.
 * Status of machine dependent identification mechanism (blinking LED, beep, etc.).
 * This class is supposed to handle code values out of enum range in case illegal value
 * is received in response.
 */
public enum ChassisIdentifyState {
    /**
     * Machine identification is turned off
     */
    Off(ChassisIdentifyState.OFF),
    /**
     * Machine identification is temporarily enabled (by default for 15 sec)
     */
    TemporaryOn(ChassisIdentifyState.TEMPORARYON),
    /**
     * Machine identification is turned on
     */
    IndefiniteOn(ChassisIdentifyState.INDEFINITEON),;

    private static final int OFF = 0;
    private static final int TEMPORARYON = 1;
    private static final int INDEFINITEON = 2;

    private final int code;

    /**
     * Initialize with a specified chassis identify state value.
     *
     * @param code state code
     */
    ChassisIdentifyState(int code) {
        this.code = code;
    }

    /**
     * Convert integer value (e.g received as response) to its appropriate enumeration
     *
     * @param value integer value to be converted
     * @return enumeration value related to given parameter
     * @throws IllegalArgumentException when provided state is out of range and does not match any defined enum value
     */
    public static ChassisIdentifyState parseInt(int value) {
        switch (value) {
            case OFF:
                return Off;
            case TEMPORARYON:
                return TemporaryOn;
            case INDEFINITEON:
                return IndefiniteOn;
            default:
                throw new IllegalArgumentException("Invalid value: " + value);
        }
    }

    /**
     * Get integer representation of chassis identify state.
     *
     * @return value of chassis identify state
     */
    public int getCode() {
        return code;
    }
}
