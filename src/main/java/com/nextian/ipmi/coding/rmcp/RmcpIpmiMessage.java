/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.coding.rmcp;

/**
 * A wrapper class for ASF ping message.
 */
public class RmcpIpmiMessage extends RmcpMessage {
    /**
     * Prepares a ready to send ASF IPMI message.
     *
     * @param ipmiMessage - IPMI message encoded as a byte array
     */
    public RmcpIpmiMessage(byte[] ipmiMessage) throws IllegalArgumentException {
        super();
        setVersion(RmcpVersion.RMCP1_0);
        setSequenceNumber(0xff);
        setClassOfMessage(RmcpClassOfMessage.Ipmi);
        setData(ipmiMessage);
    }

}
