/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.events;

import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.states.State;

/**
 * Default message for acknowledging received IPMI responses. Performs a few {@link State} transitions.
 *
 * @see StateMachine
 */
public class DefaultAck extends StateMachineEvent {

}
