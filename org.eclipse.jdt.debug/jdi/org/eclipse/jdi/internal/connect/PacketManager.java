package org.eclipse.jdi.internal.connect;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

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
	
	/**
	 * Creates new PacketManager.
	 */
	protected PacketManager(ConnectorImpl connector) {
		fConnector = connector;
	}

	/**
	 * Used to indicate that an IO exception occurred, closes connection to Virtual Machine.
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
	 * Assigns thread of partner, to be notified if we have an IO exception.
	 */
	public void setPartnerThread(Thread thread) {
		fPartnerThread = thread;
	}
}