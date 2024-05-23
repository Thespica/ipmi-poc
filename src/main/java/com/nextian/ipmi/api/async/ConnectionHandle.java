/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */

package com.nextian.ipmi.api.async;

import com.nextian.ipmi.coding.commands.PrivilegeLevel;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.connection.ConnectionManager;

/**
 * Handle of the pseudo connection built on UDP protocol.
 * The handle property is used identify created by {@link ConnectionManager ) connection entry.
 * On IPMI capabilities request cipherSuite and privilegeLevel values may be set.
 */
public class ConnectionHandle {
    private final int handle;
    private CipherSuite cipherSuite;
    private PrivilegeLevel privilegeLevel;

    /**
     * Initialize class by providing {@link ConnectionManager} generated handle.
     *
     * @param handle identifier of connection
     */
    public ConnectionHandle(int handle) {
        this.handle = handle;
    }

    /**
     * Get connection requested cipher suite.
     *
     * @return cipher suite object
     */
    public CipherSuite getCipherSuite() {
        return cipherSuite;
    }

    /**
     * Set cipher suite that will be requested during session creation.
     *
     * @param cipherSuite cipher suite object
     */
    public void setCipherSuite(CipherSuite cipherSuite) {
        this.cipherSuite = cipherSuite;
    }

    /**
     * Get privilege level to requested for connection session.
     *
     * @return requested privilege level
     */
    public PrivilegeLevel getPrivilegeLevel() {
        return privilegeLevel;
    }

    /**
     * Set privilege level to be requested during session creation.
     *
     * @param privilegeLevel privilege level value
     */
    public void setPrivilegeLevel(PrivilegeLevel privilegeLevel) {
        this.privilegeLevel = privilegeLevel;
    }

    /**
     * Get connection handle assigned to object.
     *
     * @return handle value
     */
    public int getHandle() {
        return handle;
    }
}
