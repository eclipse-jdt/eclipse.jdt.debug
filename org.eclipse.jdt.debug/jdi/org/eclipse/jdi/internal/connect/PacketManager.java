/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.connect;


import java.io.IOException;

import org.eclipse.jdi.internal.VirtualMachineImpl;

/**
 * This class implements threads that receive/send packets from/to the Virtual Machine.
 *
 */
public abstract class PacketManager implements Runnable {
	/** Connector that performs IO to Virtual Machine. */
	private ConnectorImpl fConnector;
	/** Thread that handles the communication the other way (e.g. if we are sending, the receiving thread). */
	private Thread fPartnerThread;
	private IOException fDisconnectException;
	
	/**
	 * Creates new PacketManager.
	 */
	protected PacketManager(ConnectorImpl connector) {
		fConnector = connector;
	}
	
	/**
	 * Used to indicate that an IO exception occurred, closes connection to Virtual Machine.
	 * 
	 * @param disconnectException the IOException that occurred
	 */
	public synchronized void disconnectVM(IOException disconnectException) {
		fDisconnectException= disconnectException;
		disconnectVM();
	}

	/**
	 * Closes connection to Virtual Machine.
	 */
	public synchronized void disconnectVM() {
		VirtualMachineImpl vm = fConnector.virtualMachine();

		vm.setDisconnected(true);
		fConnector.close();
		// Notify any waiting threads.
		notifyAll();
		// Interrupt the sending thread if we are the receiving thread and vice versa.
		fPartnerThread.interrupt();
	}
	
	/**
	 * @return Returns whether an IO exception has occurred.
	 */
	public boolean VMIsDisconnected() {
		return fConnector.virtualMachine().isDisconnected();
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
