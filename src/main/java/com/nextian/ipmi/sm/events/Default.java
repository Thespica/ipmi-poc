/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.events;

import com.nextian.ipmi.coding.commands.PrivilegeLevel;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.sm.StateMachine;

/**
 * Generic event that is used in a few transitions.
 *
 * @see StateMachine
 */
public class Default extends StateMachineEvent {
    private final CipherSuite cipherSuite;
    private final int sequenceNumber;
    private final PrivilegeLevel privilegeLevel;

    public Default(CipherSuite cipherSuite, int sequenceNumber, PrivilegeLevel privilegeLevel) {
        this.cipherSuite = cipherSuite;
        this.sequenceNumber = sequenceNumber;
        this.privilegeLevel = privilegeLevel;
    }

    public CipherSuite getCipherSuite() {
        return cipherSuite;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public PrivilegeLevel getPrivilegeLevel() {
        return privilegeLevel;
    }


}
