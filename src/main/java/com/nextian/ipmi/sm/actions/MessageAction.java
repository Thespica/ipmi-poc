/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sm.actions;

import com.nextian.ipmi.coding.protocol.Ipmiv20Message;
import com.nextian.ipmi.sm.states.SessionValid;

/**
 * Returns response to the unknown command (recognition is up to the higher levels of the architecture) received
 * in the {@link SessionValid} state.
 */
public class MessageAction extends StateMachineAction {
    private Ipmiv20Message ipmiResponseData;

    public MessageAction(Ipmiv20Message message) {
        ipmiResponseData = message;
    }

    public Ipmiv20Message getIpmiv20Message() {
        return ipmiResponseData;
    }
}
