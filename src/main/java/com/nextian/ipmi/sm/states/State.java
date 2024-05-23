/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.states;

import com.nextian.ipmi.coding.rmcp.RmcpMessage;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.events.StateMachineEvent;

/**
 * The abstract for state of the {@link StateMachine}.
 */
public abstract class State {
    /**
     * Defines the action performed when the state is entered.
     *
     * @param stateMachine the context
     */
    public void onEnter(StateMachine stateMachine) {
        // No default action is performed on enter state
    }

    /**
     * Performs the state transition
     *
     * @param stateMachine the context
     * @param machineEvent the {@link StateMachineEvent} that was the cause of the transition
     * @throws InterruptedException when thread is interrupted
     */
    public abstract void doTransition(StateMachine stateMachine, StateMachineEvent machineEvent)
            throws InterruptedException;

    /**
     * Defines the action that should be performed when a response form the remote system arrives in the current state.
     *
     * @param stateMachine the context
     * @param message      the message that appeared
     * @throws InterruptedException when thread is interrupted
     */
    public abstract void doAction(StateMachine stateMachine, RmcpMessage message) throws InterruptedException;
}
