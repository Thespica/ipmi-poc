/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.connection.queue;

import com.nextian.ipmi.coding.commands.IpmiCommandCoder;
import com.nextian.ipmi.connection.Connection;
import com.nextian.ipmi.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Queues messages to send and checks for timeouts.
 */
public class MessageQueue extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageQueue.class);
    /**
     * Size of the queue determined by IPMI sliding window algorithm specification. <br>
     * When queue size is 16, BMC drops some of the messages under heavy load.
     */
    private static final int QUEUE_SIZE = 8;

    /**
     * The unassigned-illegal sequence number.
     */
    public static final int ILLEGAL_SEQUENCE_NUMBER = -1;

    private List<QueueElement> queue;
    private int timeout;
    private Timer timer;
    private Connection connection;
    private int lastSequenceNumber;
    private Object lastSequenceNumberLock = new Object();
    private List<Integer> reservedTags;

    public MessageQueue(Connection connection, int timeout, int cleaningFrequency) {
        reservedTags = new ArrayList<Integer>();
        lastSequenceNumber = 0;
        this.connection = connection;
        queue = new ArrayList<QueueElement>();
        setTimeout(timeout);
        timer = new Timer();
        timer.schedule(this, cleaningFrequency, cleaningFrequency);
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Stops the MessageQueue
     */
    public void tearDown() {
        timer.cancel();
    }

    /**
     * Check if the tag is reserved.
     */
    private synchronized boolean isReserved(int tag) {
        return reservedTags.contains(tag);
    }

    /**
     * Reserves given tag for the use of the invoker.
     *
     * @param tag tag to reserve
     * @return true if tag was reserved successfully, false otherwise
     */
    private synchronized boolean reserveTag(int tag) {
        if (isReserved(tag)) {
            reservedTags.add(tag);
            return true;
        }
        return false;
    }

    /**
     * Release already used tag
     *
     * @param tag tag to be released
     */
    private synchronized void releaseTag(int tag) {
        reservedTags.remove((Integer) tag);
    }

    /**
     * Adds request to the queue and generates the tag.
     *
     * @return Session sequence number of the message if it was added to the queue, {@link #ILLEGAL_SEQUENCE_NUMBER}
     * otherwise. The tag used to identify message is equal to that value % 64.
     */
    public int add(IpmiCommandCoder request) throws InterruptedException {
        run();
        boolean first = true;
        synchronized (queue) {
            synchronized (lastSequenceNumberLock) {
                if (queue.size() < QUEUE_SIZE) {
                    int sequenceNumber = (lastSequenceNumber + 1) % (Integer.MAX_VALUE / 4);

                    if (sequenceNumber == 0) {
                        throw new ArithmeticException("Session sequence number overload. Reset session");
                    }

                    while (isReserved(sequenceNumber % 64)) {
                        sequenceNumber = (sequenceNumber + 1) % (Integer.MAX_VALUE / 4);

                        if (!first) {
                            Thread.sleep(1);
                        }

                        if (sequenceNumber == 0) {
                            throw new ArithmeticException("Session sequence number overload. Reset session");
                        }
                        first = false;
                    }

                    reserveTag(sequenceNumber % 64);

                    lastSequenceNumber = sequenceNumber;

                    QueueElement element = new QueueElement(sequenceNumber, request);

                    queue.add(element);
                    return sequenceNumber;
                }
            }
        }
        return ILLEGAL_SEQUENCE_NUMBER;

    }

    /**
     * Removes message with the given tag from the queue.
     */
    public void remove(int tag) {
        run();
        synchronized (queue) {
            int i = 0;
            int index = -1;
            for (QueueElement element : queue) {
                if (element.getId() % 64 == tag) {
                    index = i;
                    break;
                }
                ++i;
            }
            if (index == 0) {
                queue.remove(0);
                releaseTag(tag);
                while (! queue.isEmpty() && queue.get(0).getRequest() == null) {
                    int additionalTag = queue.get(0).getId() % 64;
                    queue.remove(0);
                    releaseTag(additionalTag);
                }
            } else if (index > 0) {
                queue.get(index).setRequest(null);
            }

        }
    }

    /**
     * Removes message from queue at given index.
     *
     * @param index position of message to be removed
     */
    public void removeAt(int index) {
        if (index >= queue.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds : " + index);
        }

        remove(queue.get(index).getId() % 64);
    }

    /**
     * Checks if queue contains message with the given sequence number.
     */
    public boolean containsId(int sequenceNumber) {
        synchronized (queue) {

            for (QueueElement element : queue) {
                if (element.getId() == sequenceNumber && element.getRequest() != null) {
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * Returns valid session sequence number that cannot be used as a tag though
     */
    public int getSequenceNumber() {
        synchronized (lastSequenceNumberLock) {
            int sequenceNumber = (lastSequenceNumber + 1) % (Integer.MAX_VALUE / 4);

            if (sequenceNumber == 0) {
                throw new ArithmeticException("Session sequence number overload. Reset session");
            }
            lastSequenceNumber = sequenceNumber;
            return sequenceNumber;
        }
    }

    /**
     * Returns message with the given sequence number from the queue or null if no message with the given tag is
     * currently in the queue.
     */
    public IpmiCommandCoder getMessageFromQueue(int tag) {
        synchronized (queue) {
            for (QueueElement element : queue) {
                if (element.getId() % 64 == tag && element.getRequest() != null) {
                    return element.getRequest();
                }
            }
        }
        return null;
    }

    /**
     * Returns index of the message with the given sequence number from the queue or -1 if no message with the given
     * tag is currently in the queue.
     */
    public int getMessageIndexFromQueue(int tag) {
        synchronized (queue) {
            int i = 0;
            for (QueueElement element : queue) {
                if (element.getId() % 64 == tag && element.getRequest() != null) {
                    return i;
                }
                ++i;
            }
        }
        return -1;
    }

    /**
     * Returns number of retries that were performed on message tagged with tag or -1 if no such message can be found
     * in the queue.
     */
    @Deprecated
    public int getMessageRetries(int tag) {
        synchronized (queue) {
            for (QueueElement element : queue) {
                if (element.getId() % 64 == tag && element.getRequest() != null) {
                    return element.getRetries();
                }
            }
        }
        return -1;
    }

    /**
     * Returns the ID of the {@link QueueElement} in the queue with the given tag.
     *
     * @param tag tag of the message to find
     */
    public int getMessageSequenceNumber(int tag) {
        synchronized (queue) {
            for (QueueElement element : queue) {
                if (element.getId() % 64 == tag && element.getRequest() != null) {
                    return element.getId();
                }
            }
        }
        return -1;
    }

    /**
     * {@link TimerTask} runner - periodically checks queue for timed out messages.
     */
    @Override
    public void run() {
        if (queue != null) {
            synchronized (queue) {
                boolean process = true;
                while (process && ! queue.isEmpty()) {
                    Date now = new Date();
                    if (now.getTime() - queue.get(0).getTimestamp().getTime() > (long) timeout
                            || queue.get(0).getRequest() == null) {
                        int tag = queue.get(0).getId() % 64;
                        boolean done = queue.get(0).getRequest() == null;
                        queue.remove(0);
                        LOGGER.info("Removing message after timeout, tag: {}", tag);
                        releaseTag(tag);
                        if (!done) {
                            connection.notifyListeners(connection.getHandle(), tag, null,
                                    new ConnectionException("Message timed out"));
                        }
                    } else {
                        process = false;
                    }
                }
            }
        }
    }
}
