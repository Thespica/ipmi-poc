/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.coding.commands.session;

import com.nextian.ipmi.coding.commands.ResponseData;

/**
 * A wrapper for RAKP2 message (the response of RAKP1 request).
 *
 * @see Rakp1
 */
public class Rakp1ResponseData implements ResponseData {

    /**
     * Message tag corresponding to request tag
     */
    private byte messageTag;

    /**
     * Identifier of status of previous message
     */
    private byte statusCode;

    /**
     * Remote Console Session ID sent by console on RCMP+ Open Session Request
     */
    private int remoteConsoleSessionId;

    /**
     * Random system assigned by managed system
     */
    private byte[] managedSystemRandomNumber;

    /**
     * Managed system unique ID
     */
    private byte[] managedSystemGuid;


    public void setMessageTag(byte messageTag) {
        this.messageTag = messageTag;
    }

    public byte getMessageTag() {
        return messageTag;
    }

    public void setStatusCode(byte statusCode) {
        this.statusCode = statusCode;
    }

    public byte getStatusCode() {
        return statusCode;
    }

    public void setRemoteConsoleSessionId(int remoteConsoleSessionId) {
        this.remoteConsoleSessionId = remoteConsoleSessionId;
    }

    public int getRemoteConsoleSessionId() {
        return remoteConsoleSessionId;
    }

    public void setManagedSystemGuid(byte[] managedSystemGuid) {
        this.managedSystemGuid = managedSystemGuid;
    }

    public byte[] getManagedSystemGuid() {
        return managedSystemGuid;
    }

    public void setManagedSystemRandomNumber(byte[] randomNumber) {
        this.managedSystemRandomNumber = randomNumber;
    }

    public byte[] getManagedSystemRandomNumber() {
        return managedSystemRandomNumber;
    }
}
