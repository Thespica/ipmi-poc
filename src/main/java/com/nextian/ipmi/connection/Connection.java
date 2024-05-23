/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.connection;

import com.nextian.ipmi.coding.commands.IpmiCommandCoder;
import com.nextian.ipmi.coding.commands.IpmiVersion;
import com.nextian.ipmi.coding.commands.PrivilegeLevel;
import com.nextian.ipmi.coding.commands.ResponseData;
import com.nextian.ipmi.coding.commands.session.*;
import com.nextian.ipmi.coding.payload.lan.IPMIException;
import com.nextian.ipmi.coding.payload.lan.IpmiLanResponse;
import com.nextian.ipmi.coding.protocol.Ipmiv20Message;
import com.nextian.ipmi.coding.security.AuthenticationRakpHmacSha1;
import com.nextian.ipmi.coding.security.CipherSuite;
import com.nextian.ipmi.coding.security.ConfidentialityAesCbc128;
import com.nextian.ipmi.coding.security.IntegrityHmacSha1_96;
import com.nextian.ipmi.common.Defaults;
import com.nextian.ipmi.common.TypeConverter;
import com.nextian.ipmi.connection.queue.MessageQueue;
import com.nextian.ipmi.sm.MachineObserver;
import com.nextian.ipmi.sm.StateMachine;
import com.nextian.ipmi.sm.actions.ErrorAction;
import com.nextian.ipmi.sm.actions.GetSikAction;
import com.nextian.ipmi.sm.actions.MessageAction;
import com.nextian.ipmi.sm.actions.ResponseAction;
import com.nextian.ipmi.sm.actions.StateMachineAction;
import com.nextian.ipmi.sm.events.AuthenticationCapabilitiesReceived;
import com.nextian.ipmi.sm.events.Authorize;
import com.nextian.ipmi.sm.events.CloseSession;
import com.nextian.ipmi.sm.events.Default;
import com.nextian.ipmi.sm.events.DefaultAck;
import com.nextian.ipmi.sm.events.GetChannelCipherSuitesPending;
import com.nextian.ipmi.sm.events.OpenSessionAck;
import com.nextian.ipmi.sm.events.Rakp2Ack;
import com.nextian.ipmi.sm.events.Sendv20Message;
import com.nextian.ipmi.sm.events.StartSession;
import com.nextian.ipmi.sm.events.Timeout;
import com.nextian.ipmi.sm.states.Authcap;
import com.nextian.ipmi.sm.states.Ciphers;
import com.nextian.ipmi.sm.states.SessionValid;
import com.nextian.ipmi.sm.states.Uninitialized;
import com.nextian.ipmi.transport.Messenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A connection with the specific remote host.
 */
public class Connection extends TimerTask implements MachineObserver {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final int DEFAULT_CIPHER_SUITE = 3;
    private static final int NOTIFICATION_TIMEOUT = 250; // Maximum time in milliseconds to propagate response from socket to active operation
    private final List<ConnectionListener> listeners;
    private final StateMachine stateMachine;
    /**
     * Message queue is periodically analyzed for timed out messages using this delay (in ms).
     */
    private final int cleaningFrequency;
    private final Exchanger<StateMachineAction> lastAction;
    private final int handle;

    /**
     * Time in ms after which a message times out. It is propagated to {{@link MessageQueue}} either on connection
     * request (when queue is created) or on existing queue if connection is established.
     */
    private int timeout;
    private int sessionId;
    private int managedSystemSessionId;
    private byte[] sik;
    private int lastReceivedSequenceNumber = 0;
    private MessageQueue messageQueue;
    private Timer timer;

    /**
     * Creates the connection.
     *
     * @param messenger {@link Messenger} associated with the proper {@link Defaults#IPMI_PORT}
     * @param handle    id of the connection
     * @param timeout   requests timeout in ms
     * @param cleaningFrequency delay in ms between message queue cleaning action (responsible for timeout discovery)
     */
    public Connection(Messenger messenger, int handle, int timeout, int cleaningFrequency) {
        stateMachine = new StateMachine(messenger);
        this.handle = handle;
        this.timeout = timeout;
        this.cleaningFrequency = cleaningFrequency;
        lastAction = new Exchanger<StateMachineAction>();
        listeners = new ArrayList<ConnectionListener>();
    }

    /**
     * @return Default cipher suite (3)
     * @throws NoSuchAlgorithmException when cipher authentication algorithm cannot be set
     */
    public static CipherSuite getDefaultCipherSuite() throws NoSuchAlgorithmException {
        return new CipherSuite((byte) DEFAULT_CIPHER_SUITE, new AuthenticationRakpHmacSha1().getCode(),
                new ConfidentialityAesCbc128().getCode(), new IntegrityHmacSha1_96().getCode());
    }

    public int getHandle() {
        return handle;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
        messageQueue.setTimeout(timeout);
    }

    /**
     * Registers the listener so it will receive notifications from this connection.
     *
     * @param listener {@link ConnectionListener} to notify
     */
    public void registerListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters the {@link ConnectionListener}.
     *
     * @param listener {@link ConnectionListener} to unregister
     */
    public void unregisterListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Starts the connection to the specified {@link InetAddress}.
     *
     * @param address    IP address of the managed system
     * @param keepalivePeriod frequency of the no-op commands that will be sent to keep up the session
     * @see #disconnect()
     */
    public void connect(InetAddress address, int keepalivePeriod) {
        connect(address, keepalivePeriod, false);
    }

    /**
     * Starts the connection to the specified {@link InetAddress}
     *
     * @param address     IP address of the managed system
     * @param keepalivePeriod  frequency of the no-op commands that will be sent to keep up the session
     * @param skipCiphers determines if the getAvailableCipherSuites and getChannelAuthenticationCapabilities phases
     *                    should be skipped
     * @see #disconnect()
     */
    public void connect(InetAddress address, int keepalivePeriod, boolean skipCiphers) {
        messageQueue = new MessageQueue(this, timeout, cleaningFrequency);
        timer = new Timer();
        timer.schedule(this, keepalivePeriod, keepalivePeriod);
        stateMachine.register(this);
        if (skipCiphers) {
            stateMachine.start(address);
            sessionId = ConnectionManager.generateSessionId();
            stateMachine.setCurrent(new Authcap());
        } else {
            stateMachine.start(address);
        }
    }

    /**
     * Ends the connection.
     *
     * @see #connect(InetAddress, int)
     */
    public void disconnect() {
        timer.cancel();
        stateMachine.stop();
        messageQueue.tearDown();
    }

    /**
     * Checks if the connection is active.
     *
     * @return true if the connection is active, false otherwise
     * @see #connect(InetAddress, int)
     * @see #disconnect()
     */
    public boolean isActive() {
        return stateMachine.isActive();
    }

    /**
     * Gets from the managed system supported {@link CipherSuite}s. Should be performed only immediately after
     * {@link #connect(InetAddress, int)}.
     *
     * @param tag the integer from range 0-63 to match request with response
     * @return list of the {@link CipherSuite}s supported by the managed system.
     * @throws ConnectionException  when connection is in the state that does not allow to perform this operation.
     * @throws InterruptedException when thread is interrupted
     */
    public List<CipherSuite> getAvailableCipherSuites(int tag) throws ConnectionException, InterruptedException {

        if (stateMachine.getCurrent().getClass() != Uninitialized.class) {
            throw new ConnectionException("Illegal connection state: "
                    + stateMachine.getCurrent().getClass().getSimpleName());
        }

        boolean process = true;

        ArrayList<byte[]> rawCipherSuites = new ArrayList<byte[]>();

        while (process) {

            stateMachine.doTransition(new GetChannelCipherSuitesPending(tag));

            ResponseAction action = getResponse(timeout);

            if (!(action.getIpmiResponseData() instanceof GetChannelCipherSuitesResponseData)) {
                stateMachine.doTransition(new Timeout());
                throw new ConnectionException("Response data not matching Get Channel Cipher Suites command.");
            }

            GetChannelCipherSuitesResponseData responseData =
                    (GetChannelCipherSuitesResponseData) action.getIpmiResponseData();

            rawCipherSuites.add(responseData.getCipherSuiteData());

            if (responseData.getCipherSuiteData().length < 16) {
                process = false;
            }
        }

        stateMachine.doTransition(new DefaultAck());

        int length = 0;

        for (byte[] partial : rawCipherSuites) {
            length += partial.length;
        }

        byte[] csRaw = new byte[length];

        int index = 0;

        for (byte[] partial : rawCipherSuites) {
            System.arraycopy(partial, 0, csRaw, index, partial.length);
            index += partial.length;
        }

        return CipherSuite.getCipherSuites(csRaw);
    }

    private ResponseAction getResponse(int responseTimeout) throws InterruptedException, ConnectionException {
        StateMachineAction action;

        try {
            action = lastAction.exchange(null, responseTimeout, TimeUnit.MILLISECONDS);
            if (action instanceof ErrorAction) {
                throw new ConnectionException("Response receiving failure" + ((ErrorAction) action).getException());
            } else {
                if (!(action instanceof ResponseAction || action instanceof GetSikAction)) {
                    throw new ConnectionException("Invalid StateMachine response: " + action.getClass().getSimpleName());
                }
            }
        } catch (TimeoutException e) {
            stateMachine.doTransition(new Timeout());
            throw new ConnectionException("Command timed out", e);
        }
        return (ResponseAction) action;
    }


    /**
     * Queries the managed system for the details of the authentication process. Must be performed after
     * {@link #getAvailableCipherSuites(int)}
     *
     * @param tag                     the integer from range 0-63 to match request with response
     * @param cipherSuite             {@link CipherSuite} requested for the session
     * @param requestedPrivilegeLevel {@link PrivilegeLevel} requested for the session
     * @return {@link GetChannelAuthenticationCapabilitiesResponseData}
     * @throws ConnectionException  when connection is in the state that does not allow to perform this operation.
     * @throws InterruptedException when thread is interrupted
     */
    public GetChannelAuthenticationCapabilitiesResponseData getChannelAuthenticationCapabilities(
            int tag, CipherSuite cipherSuite, PrivilegeLevel requestedPrivilegeLevel)
            throws InterruptedException, ConnectionException {

        if (stateMachine.getCurrent().getClass() != Ciphers.class) {
            throw new ConnectionException("Illegal connection state: " + stateMachine.getCurrent().getClass().getSimpleName());
        }

        stateMachine.doTransition(new Default(cipherSuite, tag, requestedPrivilegeLevel));

        ResponseAction action = getResponse(timeout);

        if (!(action.getIpmiResponseData() instanceof GetChannelAuthenticationCapabilitiesResponseData)) {
            stateMachine.doTransition(new Timeout());
            throw new ConnectionException("Response data not matching Get Channel Authentication Capabilities command.");
        }

        GetChannelAuthenticationCapabilitiesResponseData responseData =
                (GetChannelAuthenticationCapabilitiesResponseData) action.getIpmiResponseData();

        sessionId = ConnectionManager.generateSessionId();

        stateMachine.doTransition(new AuthenticationCapabilitiesReceived(sessionId, requestedPrivilegeLevel));

        return responseData;
    }

    /**
     * Initiates the session with the managed system. Must be performed after
     * {@link #getChannelAuthenticationCapabilities(int, CipherSuite, PrivilegeLevel)}
     * or {@link #closeSession()}
     *
     * @param tag            the integer from range 0-63 to match request with response
     * @param cipherSuite    {@link CipherSuite} that will be used during the session
     * @param privilegeLevel requested {@link PrivilegeLevel} - most of the time it will be {@link PrivilegeLevel#User}
     * @param username       the username
     * @param password       the password matching the username
     * @param bmcKey         the key that should be provided if the two-key authentication is enabled, null otherwise.
     * @throws ConnectionException      when connection is in the state that does not allow to perform this operation.
     * @throws InterruptedException     when thread is interrupted
     * @throws NoSuchPaddingException   when cipher algorithm initialization fails
     * @throws NoSuchAlgorithmException when cipher algorithm initialization fails
     */
    public void startSession(int tag, CipherSuite cipherSuite, PrivilegeLevel privilegeLevel, String username,
                             String password, byte[] bmcKey)
            throws ConnectionException, InterruptedException, NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException {
        if (stateMachine.getCurrent().getClass() != Authcap.class) {
            throw new ConnectionException("Illegal connection state: " +
                    stateMachine.getCurrent().getClass().getSimpleName());
        }

        // Open Session
        stateMachine.doTransition(new Authorize(cipherSuite, tag, privilegeLevel, sessionId));

        ResponseAction action = getResponse(timeout);

        if (!(action.getIpmiResponseData() instanceof OpenSessionResponseData)) {
            stateMachine.doTransition(new Timeout());
            throw new ConnectionException("Response data not matching OpenSession response data");
        }

        managedSystemSessionId = ((OpenSessionResponseData) action.getIpmiResponseData()).getManagedSystemSessionId();

        stateMachine.doTransition(new DefaultAck());

        // RAKP 1
        stateMachine.doTransition(new OpenSessionAck(cipherSuite, privilegeLevel, tag, managedSystemSessionId, username,
                password, bmcKey));

        action = getResponse(timeout);

        if (!(action.getIpmiResponseData() instanceof Rakp1ResponseData)) {
            stateMachine.doTransition(new Timeout());
            LOGGER.info(Thread.currentThread().getId() + " Open session timed out");
            throw new ConnectionException("Response data not matching RAKP Message 2: "
                    + action.getIpmiResponseData().getClass().getSimpleName());
        }

        Rakp1ResponseData rakp1ResponseData = (Rakp1ResponseData) action.getIpmiResponseData();

        stateMachine.doTransition(new DefaultAck());

        // RAKP 3
        stateMachine.doTransition(new Rakp2Ack(cipherSuite, tag, (byte) 0, managedSystemSessionId, rakp1ResponseData));

        action = getResponse(timeout);

        if (sik == null) {
            throw new ConnectionException("Session Integrity Key is null");
        }

        cipherSuite.initializeAlgorithms(sik);

        if (!(action.getIpmiResponseData() instanceof Rakp3ResponseData)) {
            stateMachine.doTransition(new Timeout());
            throw new ConnectionException("Response data not matching RAKP Message 4");
        }

        stateMachine.doTransition(new DefaultAck());
        stateMachine.doTransition(new StartSession(cipherSuite, sessionId));
    }

    /**
     * Closes the session. Can be performed only if the session is already open.
     *
     * @throws ConnectionException  when connection is in the state that does not allow to perform this operation.
     * @throws InterruptedException when thread is interrupted
     */
    public void closeSession() throws ConnectionException, InterruptedException {
        if (stateMachine.getCurrent().getClass() == SessionValid.class) {
            stateMachine.doTransition(new CloseSession(managedSystemSessionId, messageQueue.getSequenceNumber()));
        } else {
            throw new ConnectionException("Illegal connection state: "
                    + stateMachine.getCurrent().getClass().getSimpleName());
        }
    }

    /**
     * Attempts to send IPMI request to the managed system.
     *
     * @param commandCoder {@link IpmiCommandCoder} representing the request
     * @return ID of the message that will be also attached to the response to pair request with response if queue
     * was not full and message was sent, -1 if sending of the message failed.
     * @throws ConnectionException  when connection isn't in state where sending commands is allowed
     * @throws ArithmeticException  when {@link Connection} runs out of available ID's for the messages. If this
     *                              happens session needs to be restarted.
     * @throws InterruptedException when thread is interrupted
     */
    public int sendIpmiCommand(IpmiCommandCoder commandCoder)
            throws ConnectionException, ArithmeticException, InterruptedException {
        int seq;
        if ((stateMachine.getCurrent().getClass() == SessionValid.class)) {
            seq = messageQueue.add(commandCoder);
            if (seq > 0) {
                stateMachine.doTransition(new Sendv20Message(commandCoder, managedSystemSessionId, seq));
            }
        } else {
            throw new ConnectionException("Illegal connection state: " +
                    stateMachine.getCurrent().getClass().getSimpleName());
        }
        return seq % 64;
    }

    /**
     * Attempts to retry sending a message (message will be sent only if current number of retries does not exceed
     * and is not equal to maxAllowedRetries. <br>
     * IMPORTANT <br>
     * Tag of the message changes (a new one is a return value of this function).
     *
     * @param tag               tag of the message to retry
     * @param maxAllowedRetries maximum number of retries that are allowed to be performed
     * @return new tag if message was retried, -1 if operation failed
     * @throws ConnectionException  when connection isn't in state where sending commands is allowed
     * @throws ArithmeticException  when {@link Connection} runs out of available ID's for the messages. If this
     *                              happens session needs to be restarted
     * @throws InterruptedException when thread is interrupted
     */
    @Deprecated
    public int retry(int tag, int maxAllowedRetries)
            throws ArithmeticException, ConnectionException, InterruptedException {

        int retries = messageQueue.getMessageRetries(tag);

        if (retries < 0 || retries >= maxAllowedRetries) {
            return -1;
        }

        IpmiCommandCoder coder = messageQueue.getMessageFromQueue(tag);

        if (coder == null) {
            return -1;
        }

        messageQueue.remove(tag);

        return sendIpmiCommand(coder);
    }

    /**
     * Analyz incoming message and forward it to registered listeners.
     *
     * @param message IPMI message to be handled
     * @throws NullPointerException acceptable error in message parsing
     */
    private void handleIncomingMessage(Ipmiv20Message message) throws NullPointerException {
        int seq = message.getSessionSequenceNumber();

        if (seq != 0 && (seq > lastReceivedSequenceNumber + 15 || seq < lastReceivedSequenceNumber - 16)) {
            LOGGER.debug("Dropping message {}", seq);
            return; // if the message's sequence number gets out of the sliding window range we need to drop it
        }

        if (seq != 0) {
            lastReceivedSequenceNumber = seq > lastReceivedSequenceNumber ? seq : lastReceivedSequenceNumber;
        }

        if (message.getPayload() instanceof IpmiLanResponse) {

            IpmiCommandCoder coder = messageQueue.getMessageFromQueue(
                    ((IpmiLanResponse) message.getPayload()).getSequenceNumber());
            int tag = ((IpmiLanResponse) message.getPayload()).getSequenceNumber();

            LOGGER.debug("Received message with tag {}", tag);

            if (coder == null) {
                LOGGER.debug("No message tagged with {} in queue. Dropping orphan message.", tag);
                return;
            }

            if (coder.getClass() == GetChannelAuthenticationCapabilities.class) {
                messageQueue.remove(tag);
            } else {

                try {
                    ResponseData responseData = coder.getResponseData(message);
                    if (responseData != null) {
                        notifyListeners(handle, tag, responseData, null);
                    } else {
                        notifyListeners(handle, tag, null, new IOException("Empty response"));
                    }
                } catch (IPMIException e) {
                    notifyListeners(handle, tag, null, e);
                } catch (NoSuchAlgorithmException e) {
                    notifyListeners(handle, tag, null, e);
                } catch (InvalidKeyException e) {
                    notifyListeners(handle, tag, null, e);
                }
                messageQueue.remove(((IpmiLanResponse) message.getPayload()).getSequenceNumber());
            }
        }
    }

    public void notifyListeners(int handle, int tag, ResponseData responseData, Exception exception) {
        for (ConnectionListener listener : listeners) {
            if (listener != null) {
                listener.notify(responseData, handle, tag, exception);
            }
        }
    }

    @Override
    public void notify(StateMachineAction action) throws InterruptedException {
        if (action instanceof GetSikAction) {
            sik = ((GetSikAction) action).getSik();
        } else if (!(action instanceof MessageAction)) {
            try {
                lastAction.exchange(action, NOTIFICATION_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                // Nothing to do - application will handle missing response on its own
            }
        } else {
            handleIncomingMessage(((MessageAction) action).getIpmiv20Message());
        }
    }

    /**
     * {@link TimerTask} runner - periodically sends no-op messages to keep the
     * session up
     */
    @Override
    public void run() {
        int result = -1;
        do {
            try {
                if (!(stateMachine.getCurrent() instanceof SessionValid)) {
                    break;
                }
                result = sendIpmiCommand(new GetChannelAuthenticationCapabilities(
                        IpmiVersion.V20, IpmiVersion.V20, ((SessionValid) stateMachine.getCurrent()).getCipherSuite(),
                        PrivilegeLevel.Callback, TypeConverter.intToByte(0xe)));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        } while (result <= 0);
    }

    public InetAddress getRemoteMachineAddress() {
        return stateMachine.getRemoteMachineAddress();
    }

    /**
     * Checks if session is currently open.
     */
    public boolean isSessionValid() {
        return stateMachine.getCurrent() instanceof SessionValid;
    }
}
