/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.coding.protocol.decoder;

import com.nextian.ipmi.coding.protocol.IpmiMessage;
import com.nextian.ipmi.coding.rmcp.RmcpMessage;

import java.security.InvalidKeyException;

/**
 * Decodes IPMI session header and retrieves encrypted payload.
 */
public interface IpmiDecoder {

    /**
     * Decodes IPMI message.
     *
     * @param rmcpMessage RMCP message to decode.
     * @return Decoded IPMI message
     * @throws IllegalArgumentException when delivered RMCP message does not contain encapsulated IPMI message.
     * @throws InvalidKeyException      when initiation of the integrity algorithm fails
     * @see IpmiMessage
     */
    IpmiMessage decode(RmcpMessage rmcpMessage) throws IllegalArgumentException, InvalidKeyException;
}
