/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.api.async.messages;

import com.nextian.ipmi.api.async.ConnectionHandle;
import com.nextian.ipmi.api.async.IpmiListener;
import com.nextian.ipmi.coding.commands.PrivilegeLevel;
import com.nextian.ipmi.coding.security.CipherSuite;

/**
 * Interface for response messages delivered to {@link IpmiListener}s
 */
public abstract class IpmiResponse {
    private int tag;
    private ConnectionHandle handle;

    /**
     * {@link ConnectionHandle} to the message that was an origin of the
     * response Handle contains only the id of the connection, not the
     * {@link CipherSuite} and {@link PrivilegeLevel} used in that connection.
     */
    public ConnectionHandle getHandle() {
        return handle;
    }

    /**
     * Tag of the message that is associated with the {@link IpmiResponse}
     */
    public int getTag() {
        return tag;
    }

    public IpmiResponse(int tag, ConnectionHandle handle) {
        this.tag = tag;
        this.handle = handle;
    }
}
