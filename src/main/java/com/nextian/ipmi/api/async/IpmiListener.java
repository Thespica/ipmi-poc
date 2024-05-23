/*
 * Copyright 2022 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nextian.ipmi.api.async;

import com.nextian.ipmi.api.async.messages.IpmiResponse;

/**
 * Interface for listeners of {@link IpmiAsyncConnector}. Received IPMI responses are forwarded to all listeners
 * registered in connector via overridden method {notify}.
 */
public interface IpmiListener {

    /**
     * Notifies listener about an action.
     *
     * @param response response to pass to the listener
     */
    void notify(IpmiResponse response);
}
