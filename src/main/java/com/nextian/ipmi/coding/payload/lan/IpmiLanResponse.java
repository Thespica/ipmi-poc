/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.coding.payload.lan;

import com.nextian.ipmi.coding.payload.CompletionCode;
import com.nextian.ipmi.common.TypeConverter;

/**
 * A wrapper class for IPMI response message.
 */
public class IpmiLanResponse extends IpmiLanMessage {

    private CompletionCode completionCode;

    public void setCompletionCode(byte completionCode) {
        this.completionCode = CompletionCode.parseInt(TypeConverter.byteToInt(completionCode));
    }

    public CompletionCode getCompletionCode() {
        return completionCode;
    }

    /**
     * Builds IPMI LAN response message from byte array.
     *
     * @param rawData raw response data to be decoded
     * @throws IllegalArgumentException when checksum is corrupted
     */
    public IpmiLanResponse(byte[] rawData) throws IllegalArgumentException {
        setRequesterAddress(rawData[0]);
        networkFunction = TypeConverter.intToByte((TypeConverter.byteToInt(rawData[1]) & 0xfC) >> 2);
        setRequesterLogicalUnitNumber(TypeConverter.intToByte(TypeConverter.byteToInt(rawData[1]) & 0x03));
        if (rawData[2] != getChecksum1(rawData)) {
            throw new IllegalArgumentException("Checksum 1 failed");
        }
        setResponderAddress(rawData[3]);
        setSequenceNumber(TypeConverter.intToByte((TypeConverter.byteToInt(rawData[4]) & 0xfC) >> 2));
        setResponderLogicalUnitNumber(TypeConverter.intToByte(TypeConverter.byteToInt(rawData[4]) & 0x03));
        setCommand(rawData[5]);
        setCompletionCode(rawData[6]);

        if (rawData.length > 8) {
            byte[] data = new byte[rawData.length - 8];

            System.arraycopy(rawData, 7, data, 0, rawData.length - 8);

            setData(data);
        }

        if (rawData[rawData.length - 1] != getChecksum2(rawData)) {
            throw new IllegalArgumentException("Checksum 2 failed");
        }
    }

    @Override
    public int getPayloadLength() {
        int length = 8;
        if (getData() != null) {
            length += getData().length;
        }
        return length;
    }

    @Override
    @Deprecated
    public byte[] getPayloadData() {
        return new byte[0];
    }

    @Override
    public String toString() {
        String str;

        str = String.format(
                "#:  %s\n" +
                        "  completionCode:  %s\n" +
                        "  requesterAddr:   %d\n" +
                        "  responderAddr:   %d\n" +
                        "  networkFunc:     %s\n" +
                        "  requesterLUN:    %d\n" +
                        "  responderLUN:    %d\n" +
                        "  command:         %d\n" +
                        "  payloadLength:   %d\n",
                getSequenceNumber(),
                getCompletionCode(),
                getRequesterAddress(),
                getResponderAddress(),
                getNetworkFunction(),
                getRequesterLogicalUnitNumber(),
                getResponderLogicalUnitNumber(),
                getCommand(),
                getPayloadLength()
        );
        return str;
    }

}
