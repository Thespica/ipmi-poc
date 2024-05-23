/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.sample;

import com.nextian.ipmi.api.async.ConnectionHandle;
import com.nextian.ipmi.api.sync.IpmiConnector;
import com.nextian.ipmi.coding.commands.IpmiVersion;
import com.nextian.ipmi.coding.commands.PrivilegeLevel;
import com.nextian.ipmi.coding.commands.chassis.GetChassisStatus;
import com.nextian.ipmi.coding.commands.chassis.GetChassisStatusResponseData;
import com.nextian.ipmi.coding.protocol.AuthenticationType;
import com.nextian.ipmi.connection.Connection;

import java.net.InetAddress;

public class SkipAuthCap {

    /**
     * Run sample program with provided arguments. If arguments are missing display help message.
     *
     * @param args program arguments including: address, user name, password
     */
    public static void main(String[] args) {
        if (args.length == 3) {
            sampleRun(args[0], args[1], args[2]);
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
    private static void sampleRun(String address, String userName, String password) {
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
