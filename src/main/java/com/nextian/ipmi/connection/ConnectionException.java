/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */

package com.nextian.ipmi.connection;

/**
 * Exception indicating that connecting to remote machine failed.
 */
public class ConnectionException extends Exception {
	private static final long serialVersionUID = 3912859025179839078L;

	public ConnectionException(String message) {
		super(message);
	}

	public ConnectionException(String message, Throwable cause ) {
		super(message, cause);
	}
}
