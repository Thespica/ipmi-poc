/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.events;

import com.nextian.ipmi.coding.commands.session.Rakp1ResponseData;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.states.Rakp1Complete;
import com.nextian.ipmi.sm.states.Rakp3Waiting;

/**
 * Performs transition from {@link Rakp1Complete} to {@link Rakp3Waiting}.
 *
 * @see StateMachine
 */
public class Rakp2Ack extends StateMachineEvent {
    private byte statusCode;
    private CipherSuite cipherSuite;
    private int sequenceNumber;
    private int managedSystemSessionId;
    private Rakp1ResponseData rakp1ResponseData;

    /**
     * Prepares {@link Rakp2Ack}.
     *
     * @param cipherSuite            {@link CipherSuite} containing authentication, confidentiality and integrity
     *                               algorithms for this session. Only authentication algorithm is used at this point of
     *                               creating a session.
     * @param statusCode             status of the previous message.
     * @param sequenceNumber         message sequence number
     * @param managedSystemSessionId the Managed System's Session ID for this session. Must be as returned by the
     *                               Managed System in the Open Session Response message.
     * @param rakp1ResponseData      RAKP Message 2 received earlier in the authentication process
     */
    public Rakp2Ack(CipherSuite cipherSuite, int sequenceNumber, byte statusCode, int managedSystemSessionId,
                    Rakp1ResponseData rakp1ResponseData) {
        this.statusCode = statusCode;
        this.cipherSuite = cipherSuite;
        this.sequenceNumber = sequenceNumber;
        this.managedSystemSessionId = managedSystemSessionId;
        this.rakp1ResponseData = rakp1ResponseData;
    }

    public byte getStatusCode() {
        return statusCode;
    }

    public CipherSuite getCipherSuite() {
        return cipherSuite;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getManagedSystemSessionId() {
        return managedSystemSessionId;
    }

    public Rakp1ResponseData getRakp1ResponseData() {
        return rakp1ResponseData;
    }
}
