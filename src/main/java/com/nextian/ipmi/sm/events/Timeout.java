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
import com.nextian.ipmi.sm.states.Authcap;
import com.nextian.ipmi.sm.states.State;

/**
 * {@link StateMachineEvent} indicating that response for one of the messages in  the process of the session challenge
 * did not arrive in time. In most of the cases (if {@link Authcap} {@link State} was reached earlier) transits
 * {@link StateMachine} to that {@link State}.
 */
public class Timeout extends StateMachineEvent {

}
