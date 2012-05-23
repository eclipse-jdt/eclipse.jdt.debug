/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
import java.io.InterruptedIOException;
import java.util.LinkedList;

import org.eclipse.jdi.internal.jdwp.JdwpPacket;
import org.eclipse.osgi.util.NLS;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.connect.spi.Connection;

/**
 * This class implements a thread that sends available packets to the Virtual
 * Machine.
 * 
 */
public class PacketSendManager extends PacketManager {
	/** List of packets to be sent to Virtual Machine */
	private LinkedList<JdwpPacket> fOutgoingPackets;

	/**
	 * Create a new thread that send packets to the Virtual Machine.
	 */
	public PacketSendManager(Connection connection) {
		super(connection);
		fOutgoingPackets = new LinkedList<JdwpPacket>();
	}

	@Override
	public void disconnectVM() {
		super.disconnectVM();
		synchronized (fOutgoingPackets) {
			fOutgoingPackets.notifyAll();
		}
	}

	/**
	 * Thread's run method.
	 */
	public void run() {
		while (!VMIsDisconnected()) {
			try {
				sendAvailablePackets();
			}
			// in each case if the remote VM fails, or has been interrupted,
			// disconnect and force a clean up, don't wait for it to happen
			catch (InterruptedException e) {
				disconnectVM();
			} catch (InterruptedIOException e) {
				disconnectVM(e);
			} catch (IOException e) {
				disconnectVM(e);
			}
		}
	}

	/**
	 * Add a packet to be sent to the Virtual Machine.
	 */
	public void sendPacket(JdwpPacket packet) {
		if (VMIsDisconnected()) {
			String message;
			if (getDisconnectException() == null) {
				message = ConnectMessages.PacketSendManager_Got_IOException_from_Virtual_Machine_1;
			} else {
				String exMessage = getDisconnectException().getMessage();
				if (exMessage == null) {
					message = NLS.bind(ConnectMessages.PacketSendManager_Got__0__from_Virtual_Machine_1,
									new String[] { getDisconnectException()
											.getClass().getName() });
				} else {
					message = NLS.bind(ConnectMessages.PacketSendManager_Got__0__from_Virtual_Machine___1__1,
									new String[] {
											getDisconnectException().getClass()
													.getName(), exMessage });
				}
			}
			throw new VMDisconnectedException(message);
		}

		synchronized (fOutgoingPackets) {
			// Add packet to list of packets to send.
			fOutgoingPackets.add(packet);
			// Notify PacketSendThread that data is available.
			fOutgoingPackets.notifyAll();
		}
	}

	/**
	 * Send available packets to the Virtual Machine.
	 */
	private void sendAvailablePackets() throws InterruptedException,
			IOException {
		LinkedList<JdwpPacket> packetsToSend = new LinkedList<JdwpPacket>();
		synchronized (fOutgoingPackets) {
			while (fOutgoingPackets.size() == 0) {
				fOutgoingPackets.wait();
			}
			packetsToSend.addAll(fOutgoingPackets);
			fOutgoingPackets.clear();
		}

		// Put available packets on Output Stream.
		while (packetsToSend.size() > 0) {
			// Note that only JdwpPackets are added to the list, so a
			// ClassCastException can't occur.
			JdwpPacket packet = packetsToSend.removeFirst();
			byte[] bytes = packet.getPacketAsBytes();
			getConnection().writePacket(bytes);
		}
	}
}
