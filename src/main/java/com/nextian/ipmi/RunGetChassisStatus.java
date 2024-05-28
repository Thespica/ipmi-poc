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
package com.nextian.ipmi;

import com.nextian.ipmi.api.async.ConnectionHandle;
import com.nextian.ipmi.api.sync.IpmiConnector;
import com.nextian.ipmi.coding.commands.IpmiVersion;
import com.nextian.ipmi.coding.commands.PrivilegeLevel;
import com.nextian.ipmi.coding.commands.chassis.GetChassisStatus;
import com.nextian.ipmi.coding.commands.chassis.GetChassisStatusResponseData;
import com.nextian.ipmi.coding.protocol.AuthenticationType;
import com.nextian.ipmi.connection.Connection;

import java.net.InetAddress;

public class RunGetChassisStatus {

    /**
     * Run sample program with provided arguments. If arguments are missing display help message.
     *
     * @param args program arguments including: address, user name, password
     */
    public static void main(String[] args) {
        if (args.length == 3) {
            getChassisStatus(args[0], args[1], args[2]);
        } else {
            System.out.println("Program requires parameters: <IPMI server address> <User name> <Password>");
        }
    }

    /**
     * Run sample program
     *
     * @param address  IPMI server address
     * @param userName IPMI server user name
     * @param password IPMI server password
     */
    private static void getChassisStatus(String address, String userName, String password) {
        IpmiConnector connector = null;

        try {

            // Create the connector providing port number that will be used to communicate
            // with the remote host. The UDP layer starts listening at this port, so
            // no two connectors can be created on the same port at the same time.
            // The second parameter is optional - it binds the underlying socket to
            // a specific IP interface.
            connector = new IpmiConnector(6000);
            System.out.println("Connector created");

            // Create connection to target host. The connection is registered in ConnectionManager,
            // the handle will be needed to identify it among other connections
            // (target IP address isn't enough, multiple connections to the same host can be created).
            // Since the Get Channel Authentication Capabilities phase will be skipped, a preset cipher suite and privilege
            // level is explicitly provided.
            ConnectionHandle handle = connector.createConnection(InetAddress.getByName(address),
                    Connection.getDefaultCipherSuite(), PrivilegeLevel.User);
            System.out.println("Connection created");

            // Start the session, provide username and password, and optionally the
            // BMC key (only if the remote host has two-key authentication enabled,
            // otherwise this parameter should be null)
            connector.openSession(handle, userName, password, null);
            System.out.println("Session open");

            // Send some message and read the response
            GetChassisStatusResponseData rd = (GetChassisStatusResponseData) connector.sendMessage(handle,
                    new GetChassisStatus(IpmiVersion.V20, handle.getCipherSuite(), AuthenticationType.RMCPPlus));

            System.out.println("Received answer");
            System.out.println("System power state is " + (rd.isPowerOn() ? "up" : "down"));

            // Close the session
            connector.closeSession(handle);
            System.out.println("Session closed");

        } catch (Exception e) {
            // Simple exception handling
            System.out.println("Connection failed: " + e.getMessage());

        } finally {
            // Close connection manager and release the listener port.
            if (connector != null) {
                connector.tearDown();
                System.out.println("Connection manager closed");
            }
        }
    }
}
