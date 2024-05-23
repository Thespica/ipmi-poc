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
import com.nextian.ipmi.coding.commands.session.GetChannelCipherSuites;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.states.CiphersWaiting;
import com.nextian.ipmi.sm.states.State;

/**
 * Performed in {@link CiphersWaiting} {@link State} indicates that not all available {@link CipherSuite}s were
 * received from the remote system and more {@link GetChannelCipherSuites} commands are needed.
 * 
 * @see StateMachine
 */
public class GetChannelCipherSuitesPending extends Default {

	public GetChannelCipherSuitesPending(int sequenceNumber) {
		super(CipherSuite.getEmpty(), sequenceNumber, PrivilegeLevel.MaximumAvailable);
	}

}
