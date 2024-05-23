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
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.states.Authcap;
import com.nextian.ipmi.sm.states.AuthcapWaiting;

/**
 * Performs transition from {@link AuthcapWaiting} to {@link Authcap}.
 *
 * @see StateMachine
 */
public class AuthenticationCapabilitiesReceived extends StateMachineEvent {
    private final int sessionId;
    private final PrivilegeLevel privilegeLevel;

    public AuthenticationCapabilitiesReceived(int sessionId, PrivilegeLevel privilegeLevel) {
        this.sessionId = sessionId;
        this.privilegeLevel = privilegeLevel;
    }

    public int getSessionId() {
        return sessionId;
    }

    public PrivilegeLevel getPrivilegeLevel() {
        return privilegeLevel;
    }


}
