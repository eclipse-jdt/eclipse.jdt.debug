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


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.LinkedList;

import org.eclipse.jdi.internal.jdwp.JdwpPacket;

import com.sun.jdi.VMDisconnectedException;

/**
 * This class implements a thread that sends available packets to the Virtual Machine.
 *
 */
public class PacketSendManager extends PacketManager {
	/** Output Stream to Virtual Machine. */
	private OutputStream fOutStream;
	/** List of packets to be sent to Virtual Machine */
	private LinkedList fOutgoingPackets;

	/** 
	 * Create a new thread that send packets to the Virtual Machine.
	 */
	public PacketSendManager(ConnectorImpl connector) {
		super(connector);
		try {
			fOutStream = connector.getOutputStream();
			fOutgoingPackets = new LinkedList();
		} catch (IOException e) {
			disconnectVM(e);
		}
	}

	/** 
	 * Thread's run method.
	 */
	public void run() {
		while (!VMIsDisconnected()) {
			try {
				sendAvailablePackets();
			} catch (InterruptedException e) {
			} catch (InterruptedIOException e) {
			} catch (IOException e) {
				disconnectVM(e);
			}
		}
	}
	
	/** 
	 * Add a packet to be sent to the Virtual Machine.
	 */
	public synchronized void sendPacket(JdwpPacket packet) {
		if (VMIsDisconnected()) {
			String message;
			if (getDisconnectException() == null) {
				message= ConnectMessages.getString("PacketSendManager.Got_IOException_from_Virtual_Machine_1"); //$NON-NLS-1$
			} else {
				String exMessage = getDisconnectException().getMessage();
				if (exMessage == null) {
					message= MessageFormat.format(ConnectMessages.getString("PacketSendManager.Got_{0}_from_Virtual_Machine_1"), new String[] {getDisconnectException().getClass().getName()}); //$NON-NLS-1$
				} else {
					message= MessageFormat.format(ConnectMessages.getString("PacketSendManager.Got_{0}_from_Virtual_Machine__{1}_1"), new String[] {getDisconnectException().getClass().getName(), exMessage}); //$NON-NLS-1$
				}
			}
			throw new VMDisconnectedException(message);
		}

		// Add packet to list of packets to send.
		fOutgoingPackets.add(packet);
		
		// Notify PacketSendThread that data is available.
		notifyAll();
	}
	
	/** 
	 * Send available packets to the Virtual Machine.
	 */
	private synchronized void sendAvailablePackets() throws InterruptedException, IOException {
		while (fOutgoingPackets.size() == 0)
			wait();

		// Put available packets on Output Stream.
		while (fOutgoingPackets.size() > 0) {
			// Note that only JdwpPackets are added to the list, so a ClassCastException can't occur.
			JdwpPacket packet = (JdwpPacket)fOutgoingPackets.removeFirst();
			
			// Buffer the output until a complete packet is available.
			BufferedOutputStream bufferOutStream = new BufferedOutputStream(fOutStream, packet.getLength());
			packet.write(bufferOutStream);
			bufferOutStream.flush();
		}
	}
}
