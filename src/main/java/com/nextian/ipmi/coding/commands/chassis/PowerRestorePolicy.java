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
 * Chassis power restore policy.
 */
public enum PowerRestorePolicy {
    /**
     * Stay off after power is back.
     */
    PoweredOff,
    /**
     * Restore power state that was in effect before power was lost.
     */
    PowerRestored,
    /**
     * Always power up once the power is back.
     */
    PoweredUp,
}
