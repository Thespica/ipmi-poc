/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.states;

import com.nextian.ipmi.coding.Encoder;
import com.nextian.ipmi.coding.commands.session.Rakp1;
import com.nextian.ipmi.coding.commands.session.Rakp3;
import com.nextian.ipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.nextian.ipmi.coding.rmcp.RmcpMessage;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.actions.ErrorAction;
import com.nextian.ipmi.sm.actions.GetSikAction;
import com.nextian.ipmi.sm.events.Rakp2Ack;
import com.nextian.ipmi.sm.events.StateMachineEvent;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * At this state RAKP Message 2 was received - waiting for the confirmation to send RAKP Message 3. Transition
 * to {@link Rakp3Waiting} on {@link Rakp2Ack}.
 */
public class Rakp1Complete extends State {

	private Rakp1 rakp1;

	/**
	 * Initiates state.
	 * 
	 * @param rakp1 the {@link Rakp1} message that was sent earlier in the authentication process.
	 */
	public Rakp1Complete(Rakp1 rakp1) {
		this.rakp1 = rakp1;
	}

	@Override
	public void doTransition(StateMachine stateMachine, StateMachineEvent machineEvent) throws InterruptedException {
		if (machineEvent instanceof Rakp2Ack) {
			Rakp2Ack event = (Rakp2Ack) machineEvent;

			Rakp3 rakp3 = new Rakp3(event.getStatusCode(), event.getManagedSystemSessionId(), event.getCipherSuite(),
					rakp1, event.getRakp1ResponseData());

			try {
				stateMachine.setCurrent(new Rakp3Waiting(event.getSequenceNumber(), rakp1,
						event.getRakp1ResponseData(), event.getCipherSuite()));
				stateMachine.sendMessage(Encoder.encode(new Protocolv20Encoder(), rakp3, event.getSequenceNumber(), 0));
				stateMachine.doExternalAction(new GetSikAction(rakp1.calculateSik(event.getRakp1ResponseData())));
			} catch (IOException e) {
				stateMachine.setCurrent(this);
				stateMachine.doExternalAction(new ErrorAction(e));
			} catch (NoSuchAlgorithmException e) {
				stateMachine.setCurrent(this);
				stateMachine.doExternalAction(new ErrorAction(e));
			}catch (InvalidKeyException e) {
				stateMachine.setCurrent(this);
				stateMachine.doExternalAction(new ErrorAction(e));
			}
		} else {
			stateMachine.doExternalAction(new ErrorAction(new IllegalArgumentException("Invalid transition")));
		}

	}

	@Override
	public void doAction(StateMachine stateMachine, RmcpMessage message) {
		// No action is performed in this state
	}

}
