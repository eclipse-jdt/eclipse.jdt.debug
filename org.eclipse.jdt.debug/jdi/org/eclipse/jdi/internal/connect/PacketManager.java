/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.connect;

import java.io.IOException;

import com.sun.jdi.connect.spi.Connection;

/**
 * This class implements threads that receive/send packets from/to the Virtual
 * Machine.
 * 
 */
public abstract class PacketManager implements Runnable {
	/** Connector that performs IO to Virtual Machine. */
	private Connection fConnection;
	/**
	 * Thread that handles the communication the other way (e.g. if we are
	 * sending, the receiving thread).
	 */
	private Thread fPartnerThread;
	private IOException fDisconnectException;

	/**
	 * Creates new PacketManager.
	 */
	protected PacketManager(Connection connection) {
		fConnection = connection;
	}

	public Connection getConnection() {
		return fConnection;
	}

	/**
	 * Used to indicate that an IO exception occurred, closes connection to
	 * Virtual Machine.
	 * 
	 * @param disconnectException
	 *            the IOException that occurred
	 */
	public void disconnectVM(IOException disconnectException) {
		fDisconnectException = disconnectException;
		disconnectVM();
	}

	/**
	 * Closes connection to Virtual Machine.
	 */
	public void disconnectVM() {
		try {
			fConnection.close();
		} catch (IOException e) {
			fDisconnectException = e;
		}
		// Interrupt the sending thread if we are the receiving thread and vice
		// versa.
		if (fPartnerThread != null) {
			fPartnerThread.interrupt();
		}
	}

	/**
	 * @return Returns whether an IO exception has occurred.
	 */
	public boolean VMIsDisconnected() {
		return fConnection == null || !fConnection.isOpen();
	}

	/**
	 * Returns the IOException that caused this packet manager to disconnect or
	 * <code>null</code> if none.
	 */
	public IOException getDisconnectException() {
		return fDisconnectException;
	}

	/**
	 * Assigns thread of partner, to be notified if we have an IO exception.
	 */
	public void setPartnerThread(Thread thread) {
		fPartnerThread = thread;
	}
}
