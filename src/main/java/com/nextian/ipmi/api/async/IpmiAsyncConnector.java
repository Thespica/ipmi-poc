/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.api.async;

import com.nextian.ipmi.api.async.messages.IpmiError;
import com.nextian.ipmi.api.async.messages.IpmiResponse;
import com.nextian.ipmi.api.async.messages.IpmiResponseData;
import com.nextian.ipmi.coding.commands.IpmiCommandCoder;
import com.nextian.ipmi.coding.commands.PrivilegeLevel;
import com.nextian.ipmi.coding.commands.ResponseData;
import com.nextian.ipmi.coding.commands.session.GetChannelAuthenticationCapabilitiesResponseData;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.connection.Connection;
import com.nextian.ipmi.connection.ConnectionException;
import com.nextian.ipmi.connection.ConnectionListener;
import com.nextian.ipmi.connection.ConnectionManager;
import com.nextian.ipmi.connection.queue.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Asynchronous API for connecting to BMC via IPMI. Creating a connection consists of the following steps:
 * <ul>
 * <li>Create {@link Connection} and associate it with {@link ConnectionHandle} via {@link #createConnection(InetAddress)}</li>
 * <li>Get {@link CipherSuite}s that are available for the connection via {@link #getAvailableCipherSuites(ConnectionHandle)}</li>
 * <li>Pick {@link CipherSuite} and {@link PrivilegeLevel} that will be used during session</li>
 * <li>Get {@link GetChannelAuthenticationCapabilitiesResponseData} to find out allowed
 * authentication options via
 * {@link #getChannelAuthenticationCapabilities(ConnectionHandle, CipherSuite, PrivilegeLevel)}</li>
 * <li>Provide username, password and (if the BMC needs it) the BMC Kg key and
 * start session via  * {@link #openSession(ConnectionHandle, String, String, byte[])}</li>
 * </ul>
 * To send message register for receiving answers via {@link #registerListener(IpmiListener)} and send message via
 * {@link #sendMessage(ConnectionHandle, IpmiCommandCoder)}.
 * To close session call {@link #closeSession(ConnectionHandle)}.
 */
public class IpmiAsyncConnector implements ConnectionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpmiAsyncConnector.class);
    private final List<IpmiListener> listeners;
    private final int[] CIPHER_SUITE_RECOMMENDATION_LIST = {17, 8, 12, 3};
    public static final int DEFAULT_RETRIES_NUMBER = 5;

    private ConnectionManager connectionManager;
    private int retries;


    /**
     * Create {@link IpmiAsyncConnector} and initiates the {@link ConnectionManager} at the given port.
     * The default IP interface will be used to bind UDP socket.
     *
     * @param port the port that will be used by {@link IpmiAsyncConnector} to create UDP socket
     * @throws IOException when UDP socket cannot be created
     */
    public IpmiAsyncConnector(int port) throws IOException {
        listeners = new ArrayList<IpmiListener>();
        connectionManager = new ConnectionManager(port);
        loadProperties(DEFAULT_RETRIES_NUMBER);
    }

    /**
     * Create {@link IpmiAsyncConnector} and initiates the {@link ConnectionManager} at the given port.
     * The default IP interface will be used to bind UDP socket.
     *
     * @param port the port that will be used by {@link IpmiAsyncConnector} to create UDP socket
     * @param retries number of internal retries of operation
     * @throws IOException when UDP socket cannot be created
     */
    public IpmiAsyncConnector(int port, int retries) throws IOException {
        listeners = new ArrayList<IpmiListener>();
        connectionManager = new ConnectionManager(port);
        loadProperties(retries);
    }

    /**
     * Create {@link IpmiAsyncConnector} and initiates the {@link ConnectionManager} at the given port. UDP socket is
     * bound to network interface at specified IP address.
     *
     * @param port    UDP socket local port to use
     * @param address local IP address to bind UDP socket
     * @throws IOException when UDP socket cannot be created
     */
    public IpmiAsyncConnector(int port, InetAddress address) throws IOException {
        listeners = new ArrayList<IpmiListener>();
        connectionManager = new ConnectionManager(port, address);
        loadProperties(DEFAULT_RETRIES_NUMBER);
    }

    /**
     * Create {@link IpmiAsyncConnector} and initiates the {@link ConnectionManager} at the given port. UDP socket is
     * bound to network interface at specified IP address.
     *
     * @param port    UDP socket local port to use
     * @param address local IP address to bind UDP socket
     * @param retries number of internal retries of operation
     * @throws IOException when UDP socket cannot be created
     */
    public IpmiAsyncConnector(int port, InetAddress address, int retries) throws IOException {
        listeners = new ArrayList<IpmiListener>();
        connectionManager = new ConnectionManager(port, address);
        loadProperties(retries);
    }

    private void loadProperties(int retries) {
        this.retries = retries;
    }

    /**
     * Creates connection to the remote host.
     *
     * @param address {@link InetAddress} of the remote host to connect to
     * @return handle to the created connection
     */
    public ConnectionHandle createConnection(InetAddress address)  {
        int handle = connectionManager.createConnection(address);
        connectionManager.getConnection(handle).registerListener(this);
        return new ConnectionHandle(handle);
    }

    /**
     * Creates connection to the remote host, with pre set {@link CipherSuite} and {@link PrivilegeLevel}, skipping the
     * getAvailableCipherSuites and getChannelAuthenticationCapabilities phases.
     *
     * @param address - {@link InetAddress} of the remote host
     * @return handle to the connection to the remote host
     */
    public ConnectionHandle createConnection(InetAddress address, CipherSuite cipherSuite, PrivilegeLevel privilegeLevel)
    {
        int handle = connectionManager.createConnection(address, true);
        connectionManager.getConnection(handle).registerListener(this);

        ConnectionHandle connectionHandle = new ConnectionHandle(handle);
        connectionHandle.setCipherSuite(cipherSuite);
        connectionHandle.setPrivilegeLevel(privilegeLevel);

        return connectionHandle;
    }

    /**
     * Gets {@link CipherSuite}s available for a connection. Those are all cipher suites reported by IPMI server.
     *
     * @param connectionHandle connection to retrieve cipher suites for
     * @return list of the {@link CipherSuite}s that are allowed for the connection
     * @throws ConnectionException when request fails
     * @throws InterruptedException when thread is interrupted
     * @see #createConnection(InetAddress)
     */
    public List<CipherSuite> getAvailableCipherSuites(ConnectionHandle connectionHandle)
            throws ConnectionException, InterruptedException {
        ConnectionException lastException = null;
        int attemptNumber = 0;
        List<CipherSuite> result = null;
        while (attemptNumber <= retries && result == null) {
            try {
                ++attemptNumber;
                result = connectionManager.getAvailableCipherSuites(connectionHandle.getHandle());
            } catch (ConnectionException e) {
                lastException = e;
                LOGGER.info("Failed to receive answer, cause:", e);
            }
        }
        if (result == null && lastException != null) {
            throw lastException;
        }
        return result;
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
        List<CipherSuite> cipherSuites = getAvailableCipherSuites(connectionHandle);
        List<CipherSuite> supportedSipherSuites = new ArrayList<CipherSuite>();
        int i;

        for(i = 0; i <cipherSuites.size(); i++) {
            try {
                CipherSuite item = cipherSuites.get(i);
                if (item.getAuthenticationAlgorithm() != null && item.getIntegrityAlgorithm() != null &&
                        item.getConfidentialityAlgorithm() != null) {
                    supportedSipherSuites.add(item);
                }
            }
            catch(IllegalArgumentException ex) {
                // Nothing to do - check next item
            }
        };
        return supportedSipherSuites;
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
        List<CipherSuite> cipherSuites = getAvailableCipherSuites(connectionHandle);
        return cipherSuites.get(3);
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
        List<CipherSuite> cipherSuites = getAvailableCipherSuites(connectionHandle);
        CipherSuite cipherSuite = null;
        int i;

        for(i = 0; cipherSuite == null && i < CIPHER_SUITE_RECOMMENDATION_LIST.length; i++) {
            try {
                if (cipherSuites.size() >= CIPHER_SUITE_RECOMMENDATION_LIST[i]) {
                    CipherSuite item = cipherSuites.get(CIPHER_SUITE_RECOMMENDATION_LIST[i]);
                    if (item.getAuthenticationAlgorithm() != null && item.getIntegrityAlgorithm() != null &&
                            item.getConfidentialityAlgorithm() != null) {
                        cipherSuite = item;
                    }
                }
            }
            catch(IllegalArgumentException ex) {
                // Nothing to do - check next item
            }
        };
        return cipherSuite;
    }

    /**
     * Gets authentication capabilities for a connection.
     *
     * @param connectionHandle        {@link ConnectionHandle} to retrieve capabilities for
     * @param cipherSuite             {@link CipherSuite} to be used during the connection
     * @param requestedPrivilegeLevel {@link PrivilegeLevel} that is requested for the session
     * @return {@link GetChannelAuthenticationCapabilitiesResponseData} for the connection
     * @throws ConnectionException when connection is in the state that does not allow to
     *                             perform this operation
     * @throws InterruptedException when thread is interrupted
     */
    public GetChannelAuthenticationCapabilitiesResponseData getChannelAuthenticationCapabilities(
            ConnectionHandle connectionHandle, CipherSuite cipherSuite, PrivilegeLevel requestedPrivilegeLevel)
            throws InterruptedException, ConnectionException {
        int attemptNumber = 0;
        ConnectionException lastException = null;
        GetChannelAuthenticationCapabilitiesResponseData result = null;
        while (attemptNumber <= retries && result == null) {
            try {
                ++attemptNumber;
                result = connectionManager.getChannelAuthenticationCapabilities(
                                connectionHandle.getHandle(), cipherSuite, requestedPrivilegeLevel);
                connectionHandle.setCipherSuite(cipherSuite);
                connectionHandle.setPrivilegeLevel(requestedPrivilegeLevel);
            } catch (ConnectionException e) {
                LOGGER.debug("Failed to receive answer, cause:", e);
                lastException = e;
            }
        }
        if (result == null && lastException != null) {
            throw lastException;
        }
        return result;
    }

    /**
     * Establish a session with remote host.
     *
     * @param connectionHandle {@link ConnectionHandle} associated with the remote host
     * @param username         username for authentication
     * @param password         password for authentication
     * @param bmcKey           the key that should be provided if the two-key
     *                         authentication is enabled, null otherwise
     * @throws ConnectionException when connection is in the state that does not allow to
     *                             perform this operation
     * @throws InterruptedException when thread is interrupted
     */
    public void openSession(ConnectionHandle connectionHandle, String username, String password, byte[] bmcKey)
            throws ConnectionException, InterruptedException {
        int attemptNumber = 0;
        boolean succeeded = false;
        ConnectionException lastException = null;

        while (attemptNumber <= retries && !succeeded) {
            try {
                ++attemptNumber;
                connectionManager.startSession(connectionHandle.getHandle(), connectionHandle.getCipherSuite(),
                        connectionHandle.getPrivilegeLevel(), username, password, bmcKey);
                succeeded = true;
            } catch (InterruptedException e) {
                throw e; // Exit on interrupt
            } catch (ConnectionException e) {
                LOGGER.debug("Failed to receive answer, cause:", e);
                lastException =  e;
            } catch (Exception e) {
                LOGGER.debug("Failed to start session, cause:", e);
                lastException = new ConnectionException("Session start failed", e);
            }
        }
        if (! succeeded && lastException != null) {
            throw lastException;
        }
    }

    /**
     * Close a session.
     *
     * @param connectionHandle - {@link ConnectionHandle} associated with the remote host
     * @throws ConnectionException when connection is in the state that does not allow to
     *                             perform this operation
     * @throws InterruptedException when thread is interrupted
     */
    public void closeSession(ConnectionHandle connectionHandle) throws ConnectionException, InterruptedException {
        if (connectionManager.getConnection(connectionHandle.getHandle()).isSessionValid()) {
            ConnectionException lastException = null;
            int attemptNumber = 0;
            boolean succeeded = false;
            while (attemptNumber <= retries && !succeeded) {
                try {
                    ++attemptNumber;
                    connectionManager.getConnection(connectionHandle.getHandle()).closeSession();
                    succeeded = true;
                } catch (ConnectionException e) {
                    LOGGER.info("Failed to receive answer, cause:", e);
                    lastException = e;
                }
            }
            if(!succeeded && lastException != null) {
                throw lastException;
            }
        }
    }

    /**
     * Send IPMI message to remote host.
     *
     * @param connectionHandle connection handle (associated with the target host)
     * @param request          {@link IpmiCommandCoder} containing request to be sent
     * @return message id (the id will be also attached to the response to
     * pair requests with responses), -1 when sending of the message failed (e.g. queue full).
     * @throws ConnectionException when connection is in the state that does not allow to
     *                             perform this operation.
     * @throws InterruptedException when thread execution is interrupted
     */
    public int sendMessage(ConnectionHandle connectionHandle, IpmiCommandCoder request)
            throws ConnectionException, InterruptedException {
        int tries = 0;
        int tag = MessageQueue.ILLEGAL_SEQUENCE_NUMBER;
        while (tries <= retries && tag == MessageQueue.ILLEGAL_SEQUENCE_NUMBER) {
            try {
                ++tries;
                while (tag == MessageQueue.ILLEGAL_SEQUENCE_NUMBER) {
                    tag = connectionManager.getConnection(connectionHandle.getHandle()).sendIpmiCommand(request);
                    if (tag == MessageQueue.ILLEGAL_SEQUENCE_NUMBER) {
                        Thread.sleep(10); // MessageQueue is full so we need to wait and retry
                    }
                }
                LOGGER.debug("Sending message with tag {}  try {}", tag, tries);
            } catch (IllegalArgumentException e) {
                throw e; // Illegal parameter will not be corrected so exit immediately
            } catch (ConnectionException e) {
                LOGGER.debug("Failed to send message, cause:", e);
                if (tries > retries) {
                    throw e; // Retry failed - return last exception
                }
            }
        }
        return tag;
    }

    /**
     * Registers a listener to receive incoming incoming messages.
     *
     * @param listener listener to register
     */
    public void registerListener(IpmiListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Unregisters a listener so it will no longer receive incoming notifications.
     *
     * @param listener listener to unregister
     */
    public void unregisterListener(IpmiListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public void notify(ResponseData responseData, int handle, int tag, Exception exception) {
        IpmiResponse response = null;
        if (exception != null) {
            response = new IpmiError(exception, tag, new ConnectionHandle(handle));
        } else {
            response = new IpmiResponseData(responseData, tag, new ConnectionHandle(handle));
        }
        synchronized (listeners) {
            for (IpmiListener listener : listeners) {
                if (listener != null) {
                    listener.notify(response);
                }
            }
        }
    }

    /**
     * Closes the connection with the given handle
     */
    public void closeConnection(ConnectionHandle handle) {
        connectionManager.getConnection(handle.getHandle()).unregisterListener(this);
        connectionManager.closeConnection(handle.getHandle());
    }

    /**
     * Finalizes the connector and closes all connections.
     */
    public void tearDown() {
        connectionManager.close();
    }

    /**
     * Change timeout for connection with the given handle.
     *
     * @param handle  {@link ConnectionHandle} connection to change timeout for
     * @param timeout new timeout value in milliseconds
     */
    public void setTimeout(ConnectionHandle handle, int timeout) {
        connectionManager.getConnection(handle.getHandle()).setTimeout(timeout);
    }
}
