package org.eclipse.jdi.internal.connect;/*
 * JDI class Implementation
 *
 * (BB)
 * (C) Copyright IBM Corp. 2000
 */



import java.io.*;
import java.util.*;
import org.eclipse.jdi.internal.jdwp.*;

/**
 * This class implements threads that receive/send packets from/to the Virtual Machine.
 *
 */
public abstract class PacketManager implements Runnable {
	/** Flag that indicates that the VM is dosconnected. */
	private boolean fDisconnectedVM = false;
	/** Connector that performs IO to Virtual Machine. */
	private ConnectorImpl fConnector;
	/** Thread that handles the communication the other way (e.g. if we are sending, the receiving thread). */
	private Thread fPartnerThread;
	
	/*
	 * Creates new PacketManager.
	 */
	protected PacketManager(ConnectorImpl connector) {
		fConnector = connector;
	}

	/*
	 * Used to indicate that an IO exception occurred, closes connection to Virtual Machine.
	 */
	public synchronized void disconnectVM() {
		if (fDisconnectedVM)
			return;

		fDisconnectedVM = true;
		fConnector.close();
		// Notify any waiting threads.
		notifyAll();
		// Interrupt the sending thread if we are the receiving thread and vice versa.
		fPartnerThread.interrupt();
	}
	
	/*
	 * @return Returns whether an IO exception has occurred.
	 */
	public boolean VMIsDisconnected() {
		return fDisconnectedVM;
	}
	
	/*
	 * Assigns thread of parter, to be notified if we have an IO exception.
	 */
	public void setPartnerThread(Thread thread) {
		fPartnerThread = thread;
	}
}