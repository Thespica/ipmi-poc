/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.api.sync;

import com.nextian.ipmi.api.async.ConnectionHandle;
import com.nextian.ipmi.api.async.IpmiAsyncConnector;
import com.nextian.ipmi.coding.commands.IpmiCommandCoder;
import com.nextian.ipmi.coding.commands.PrivilegeLevel;
import com.nextian.ipmi.coding.commands.ResponseData;
import com.nextian.ipmi.coding.commands.session.GetChannelAuthenticationCapabilitiesResponseData;
import com.nextian.ipmi.coding.payload.CompletionCode;
import com.nextian.ipmi.coding.payload.lan.IPMIException;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.common.Defaults;
import com.nextian.ipmi.connection.Connection;
import com.nextian.ipmi.connection.ConnectionException;
import com.nextian.ipmi.connection.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Random;

/**
 * Synchronous API for connecting to BMC via IPMI. Creating connection consists of the following steps:
 * <ul>
 * <li>Create {@link Connection} and get associated with it {@link ConnectionHandle} via
 * {@link #createConnection(InetAddress)}
 * <li>Get {@link CipherSuite}s that are available for the connection via
 * {@link #getAvailableCipherSuites(ConnectionHandle)}
 * <li>Pick {@link CipherSuite} and {@link PrivilegeLevel} that will be used during session and get
 * {@link GetChannelAuthenticationCapabilitiesResponseData} to find out allowed authentication options via
 * {@link #getChannelAuthenticationCapabilities(ConnectionHandle, CipherSuite, PrivilegeLevel)}
 * <li>Provide username, password and (if the BMC needs it) the BMC Kg key and start session via
 * {@link #openSession(ConnectionHandle, String, String, byte[])} <br>
 * <p> Send message register via {@link #sendMessage(ConnectionHandle, IpmiCommandCoder)} </p> <br>
 * <p> To close session call {@link #closeSession(ConnectionHandle)} </p>
 * <p> When done with work, clean up via {@link #tearDown()} </p> <br>
 */
public class IpmiConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpmiConnector.class);

    private IpmiAsyncConnector asyncConnector;

    /**
     * Send timeout in milliseconds
     */
    private int timeout;

    /**
     * Number of send retries per request
     */
    private int retries;

    /**
     * Delay between retries in milliseconds
     */
    private int retryDelay;

    private Random random = new Random(System.currentTimeMillis());

    /**
     * Starts {@link IpmiConnector} and initiates the {@link ConnectionManager} at the given port. Wildcard IP address
     * will be used.
     *
     * @param port the port that will be used by {@link IpmiAsyncConnector} to communicate with the remote hosts.
     * @throws IOException when UDP socket cannot be created
     */
    public IpmiConnector(int port) throws IOException {
        asyncConnector = new IpmiAsyncConnector(port);
        loadProperties(Defaults.RETRIES, Defaults.RETRY_DELAY, Defaults.TIMEOUT);
    }

    /**
     * Starts {@link IpmiConnector} and initiates the {@link ConnectionManager} at the given port. Wildcard IP address
     * will be used.
     *
     * @param port the port that will be used by {@link IpmiAsyncConnector} to communicate with the remote hosts.
     * @param retries number of send retries
     * @param retryDelay delay time before send retry occurs (in ms)
     * @param timeout send timeout in ms
     * @throws IOException when UDP socket cannot be created
     */
    public IpmiConnector(int port, int retries, int retryDelay, int timeout) throws IOException {
        asyncConnector = new IpmiAsyncConnector(port);
        loadProperties(retries,  retryDelay, timeout);
    }

    /**
     * Starts {@link IpmiConnector} and initiates the {@link ConnectionManager} at the given port and IP interface.
     *
     * @param port    the port that will be used by {@link IpmiAsyncConnector} to communicate with the remote hosts.
     * @param address the IP address that will be used by {@link IpmiAsyncConnector} to communicate with the remote
     *                hosts.
     * @throws IOException when UDP socket cannot be created
     */
    public IpmiConnector(int port, InetAddress address) throws IOException {
        asyncConnector = new IpmiAsyncConnector(port, address);
        loadProperties(Defaults.RETRIES, Defaults.RETRY_DELAY, Defaults.TIMEOUT);
    }

    /**
     * Starts {@link IpmiConnector} and initiates the {@link ConnectionManager} at the given port and IP interface.
     *
     * @param port    the port that will be used by {@link IpmiAsyncConnector} to communicate with the remote hosts.
     * @param address the IP address that will be used by {@link IpmiAsyncConnector} to communicate with the remote
     *                hosts.
     * @param retries number of send retries
     * @param retryDelay delay time before send retry occurs (in ms)
     * @param timeout send timeout in ms
     * @throws IOException when UDP socket cannot be created
     */
    public IpmiConnector(int port, InetAddress address, int retries, int retryDelay, int timeout) throws IOException {
        asyncConnector = new IpmiAsyncConnector(port, address);
        loadProperties(retries,  retryDelay, timeout);
    }

    private void loadProperties(int retries, int retryDelay, int timeout) {
        this.retries = retries;
        this.retryDelay = retryDelay;
        this.timeout = timeout;
    }

    /**
     * Creates connection to the remote host.
     *
     * @param address {@link InetAddress} of the remote host
     * @return handle to the connection to the remote host
     */
    public ConnectionHandle createConnection(InetAddress address){
        return asyncConnector.createConnection(address);
    }

    /**
     * Creates connection to the remote host, with pre set {@link CipherSuite} and {@link PrivilegeLevel}, skipping the
     * getAvailableCipherSuites and getChannelAuthenticationCapabilities phases.
     *
     * @param address - {@link InetAddress} of the remote host
     * @return handle to the connection to the remote host
     */
    public ConnectionHandle createConnection(InetAddress address, CipherSuite cipherSuite,
                                             PrivilegeLevel privilegeLevel) {
        return asyncConnector.createConnection(address, cipherSuite, privilegeLevel);
    }

    /**
     * Gets {@link CipherSuite}s available for the connection with the remote host.
     *
     * @param connectionHandle {@link ConnectionHandle} to the connection created before
     * @return list of the {@link CipherSuite}s that are allowed during the connection
     * @throws ConnectionException  when error occurs during network operation
     * @throws InterruptedException when thread is interrupted
     * @see #createConnection(InetAddress)
     */
    public List<CipherSuite> getAvailableCipherSuites(ConnectionHandle connectionHandle)
            throws ConnectionException, InterruptedException {
        return asyncConnector.getAvailableCipherSuites(connectionHandle);
    }

    /**
     * Gets {@link CipherSuite}s supported bit by IPMI server and JavaIPMI API.
     *
     * @param connectionHandle connection to retrieve cipher suites for
     * @return list of the {@link CipherSuite}s that are supported for the connection
     * @throws ConnectionException when request fails
     * @throws InterruptedException when thread is interrupted
     * @see #createConnection(InetAddress)
     */
    public List<CipherSuite> getSupportedCipherSuites(ConnectionHandle connectionHandle)
            throws ConnectionException, InterruptedException {
        return asyncConnector.getSupportedCipherSuites(connectionHandle);
    }

    /**
     * Returns SHA1 + AES cipher suite, providing the strongest encryption an IPMI
     * server <strong>must</strong> implement. {@see #GetStrongestCipherSuite()}
     *
     * @param connectionHandle connection to retrieve cipher suites for
     * @return {@link CipherSuite}
     * @throws ConnectionException when request fails
     * @throws InterruptedException when thread is interrupted
     */
    public CipherSuite getDefaultCipherSuite(ConnectionHandle connectionHandle)
            throws ConnectionException, InterruptedException {
        return asyncConnector.getDefaultCipherSuite(connectionHandle);
    }

    /**
     * Negotiates and selects the strongest cipher suite supported both by the server and API.
     * {@see #getDefaultCipherSuite(ConnectionHandle)}
     *
     * @param connectionHandle connection to retrieve cipher suites for
     * @return {@link CipherSuite}
     * @throws ConnectionException when request fails
     * @throws InterruptedException when thread is interrupted
     */
    public CipherSuite getStrongestCipherSuite(ConnectionHandle connectionHandle)
            throws ConnectionException, InterruptedException {
        return asyncConnector.getStrongestCipherSuite(connectionHandle);
    }

    /**
     * Gets the authentication capabilities for the connection with the remote host.
     *
     * @param connectionHandle        {@link ConnectionHandle} associated with the host
     * @param cipherSuite             {@link CipherSuite} that will be used during the connection
     * @param requestedPrivilegeLevel {@link PrivilegeLevel} that is requested for the session
     * @return {@link GetChannelAuthenticationCapabilitiesResponseData}
     * @throws ConnectionException  when connection is in the state that does not allow to perform this operation
     * @throws InterruptedException when thread is interrupted
     */
    public GetChannelAuthenticationCapabilitiesResponseData getChannelAuthenticationCapabilities(
            ConnectionHandle connectionHandle, CipherSuite cipherSuite, PrivilegeLevel requestedPrivilegeLevel)
            throws ConnectionException, InterruptedException {
        return asyncConnector.getChannelAuthenticationCapabilities(connectionHandle, cipherSuite,
                requestedPrivilegeLevel);
    }

    /**
     * Establishes the session with the remote host.
     *
     * @param connectionHandle {@link ConnectionHandle} associated with the remote host
     * @param username         the username
     * @param password         password matching the username
     * @param bmcKey           the key that should be provided if the two-key authentication is enabled, null otherwise.
     * @throws ConnectionException  when connection is in the state that does not allow to perform this operation
     * @throws InterruptedException when thread is interrupted
     */
    public void openSession(ConnectionHandle connectionHandle, String username, String password, byte[] bmcKey)
            throws ConnectionException, InterruptedException {
        asyncConnector.openSession(connectionHandle, username, password, bmcKey);
    }

    /**
     * Closes the session with the remote host if it is currently in open state.
     *
     * @param connectionHandle {@link ConnectionHandle} associated with the remote host.
     * @throws ConnectionException  when connection is in the state that does not allow to perform this operation.
     * @throws InterruptedException when thread is interrupted
     */
    public void closeSession(ConnectionHandle connectionHandle) throws ConnectionException, InterruptedException {
        asyncConnector.closeSession(connectionHandle);
    }

    /**
     * Sends the IPMI message to the remote host.
     *
     * @param connectionHandle {@link ConnectionHandle} associated with the remote host.
     * @param request          {@link IpmiCommandCoder} containing the request to be sent
     * @return {@link ResponseData} for the <b>request</b>
     * @throws IPMIException            when received error response that cannot be retried
     * @throws IllegalArgumentException when illegal argument was used so retry cannot be done
     * @throws IOException              when communication error occurs
     * @throws InterruptedException     when thread is interrupted
     */
    public ResponseData sendMessage(ConnectionHandle connectionHandle, IpmiCommandCoder request)
            throws InterruptedException, IPMIException, IllegalArgumentException, IOException {
        int tries = 0;
        MessageListener listener = new MessageListener(connectionHandle);
        IOException lastException = null;
        IPMIException lastIPMIException = null;
        ResponseData data = null;
        int previousTag = -1;

        asyncConnector.registerListener(listener);
        try {
            while (tries <= retries && data == null) {
                try {
                    ++tries;
                    lastException = null;
                    lastIPMIException = null;
                    int tag = asyncConnector.sendMessage(connectionHandle, request);
                    LOGGER.debug("Sending message with tag {}, try {}, previous tag {}", tag, tries, previousTag);
                    previousTag = tag;
                    data = listener.waitForAnswer(tag, timeout);
                } catch (IPMIException e) {
                    if (e.getCompletionCode() != CompletionCode.InitializationInProgress
                            || e.getCompletionCode() == CompletionCode.InsufficientResources
                            || e.getCompletionCode() == CompletionCode.NodeBusy
                            || e.getCompletionCode() == CompletionCode.Timeout) {
                        lastIPMIException = e;
                        LOGGER.debug("Received response for retry" + e.getCompletionCode());
                    } else {
                        // This response means, no retries are needed
                        LOGGER.debug("Received reject response - no retry" + e.getCompletionCode());
                        throw e;
                    }
                } catch (IOException e) {
                    lastException = e;
                } catch (ConnectionException e) {
                    lastException = new IOException("Connection failed", e);
                }
                if (data == null) {
                    // Wait a little before retry
                    long sleepTime = (random.nextLong() % (retryDelay / 2)) + (retryDelay / 2);
                    Thread.sleep(sleepTime);
                }
            }
            // If exception occurred in last try throw it
            if (lastIPMIException != null) {
                throw lastIPMIException;
            }
            if (lastException != null) {
                throw lastException;
            }
        } finally {
            asyncConnector.unregisterListener(listener);
        }
        return data;
    }

    /**
     * Closes the connection with the given handle
     */
    public void closeConnection(ConnectionHandle handle) {
        asyncConnector.closeConnection(handle);
    }

    /**
     * Finalizes the connector and closes all connections.
     */
    public void tearDown() {
        asyncConnector.tearDown();
    }

    /**
     * Changes the timeout value for connection with the given handle.
     *
     * @param handle  {@link ConnectionHandle} associated with the remote host.
     * @param timeout new timeout value in ms
     */
    public void setTimeout(ConnectionHandle handle, int timeout) {
        asyncConnector.setTimeout(handle, timeout);
    }
}
