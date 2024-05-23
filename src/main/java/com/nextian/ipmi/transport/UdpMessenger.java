/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the UDP connection.
 */
public class UdpMessenger extends Thread implements Messenger {

    private static final String DEFAULT_ADDRESS = "0.0.0.0";
    private static final int DEFAULTBUFFERSIZE = 512;
    private static final Logger LOGGER = LoggerFactory.getLogger(UdpMessenger.class);
    private static int sentPackets = 0;
    private int port;
    private DatagramSocket socket;
    private List<UdpListener> listeners;
    private boolean closing = false;

    /**
     * Size of the message data buffer. Default
     * {@link UdpMessenger#DEFAULTBUFFERSIZE}.
     */
    private int bufferSize;

    /**
     * Initiates UdpMessenger, binds it to the specified port and starts listening. Wildcard IP address will be used.
     *
     * @param port port to bind socket to.
     * @throws SocketException      if the socket could not be opened, or the socket could not bind to the specified
     *                              local port.
     * @throws UnknownHostException when default host name is not found
     */
    public UdpMessenger(int port) throws SocketException, UnknownHostException {
        this(port, InetAddress.getByName(DEFAULT_ADDRESS));
    }

    /**
     * Initiates UdpMessenger, binds it to the specified port and IP address and starts listening.
     *
     * @param port    port to bind socket to.
     * @param address IP address to bind socket to.
     * @throws SocketException if the socket could not be opened, or the socket could not bind to the specified
     *                         local port.
     */
    public UdpMessenger(int port, InetAddress address) throws SocketException {
        sentPackets = 0;
        this.port = port;
        listeners = new ArrayList<UdpListener>();
        bufferSize = DEFAULTBUFFERSIZE;
        socket = new DatagramSocket(this.port, address);
        socket.setSoTimeout(0);
        this.start();
    }

    /**
     * Returns number of packets sent since last creation of the instance of {@link UdpMessenger}.
     * For debug/testing purposes only.
     */
    public static int getSentPackets() {
        return sentPackets;
    }

    public int getPort() {
        return port;
    }

    /**
     * @return size of the message data buffer
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Sets response message data buffer size.
     *
     * @param bufferSize size of UDP message buffer
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }


    @Override
    public void run() {
        super.run();

        boolean run = true;

        while (run) {
            DatagramPacket response = new DatagramPacket(new byte[bufferSize], bufferSize);

            try {
                socket.receive(response);
                UdpMessage message = new UdpMessage();
                message.setAddress(response.getAddress());
                message.setPort(response.getPort());
                byte[] buffer = new byte[response.getLength()];
                System.arraycopy(response.getData(), 0, buffer, 0, buffer.length);
                message.setMessage(buffer);
                LOGGER.debug(String.format("UDP received: %s", message));

                synchronized (listeners) {
                    for (UdpListener listener : listeners) {
                        if (listener != null) {
                            listener.notifyMessage(message);
                        }
                    }
                }

            } catch (SocketException se) {
                if (closing) {
                    run = false;
                } else {
                    LOGGER.error(se.getMessage(), se);
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                LOGGER.info("Thread interrupted - stop");
                run = false;
            } finally {
                if (socket.isClosed()) {
                    run = false;
                }
            }
        }
    }

    /**
     * Closes the socket and releases port.
     */
    public void closeConnection() {
        closing = true;
        socket.close();
    }

    /**
     * Registers listener in the UdpMessenger so it will be notified via {@link UdpListener#notifyMessage(UdpMessage)}
     * when new message arrives.
     *
     * @param listener {@link UdpListener} to register.
     */
    public void register(UdpListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Unregisters listener from UdpMessenger so it no longer will be notified.
     *
     * @param listener {@link UdpListener} to unregister
     */
    public void unregister(UdpListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Sends {@link UdpMessage}.
     *
     * @param message {@link UdpMessage} to send.
     * @throws IOException when sending of the message fails
     */
    public synchronized void send(UdpMessage message) throws IOException {
        DatagramPacket packet = new DatagramPacket(message.getMessage(), message.getMessage().length,
                message.getAddress(), message.getPort());
        LOGGER.debug("UDP sent: {}", message);
        try {
            socket.send(packet);
        } catch (IOException e) {
            LOGGER.debug("UDP send error: {}", e.toString());
            throw e;
        }
        ++sentPackets;
    }

}
