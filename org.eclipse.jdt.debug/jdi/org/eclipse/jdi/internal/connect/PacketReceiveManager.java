/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.connect;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpPacket;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;
import org.eclipse.jdt.internal.debug.core.JDIDebugOptions;
import org.eclipse.osgi.util.NLS;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.connect.spi.Connection;

/**
 * This class implements a thread that receives packets from the Virtual
 * Machine.
 */
public class PacketReceiveManager extends PacketManager {

	/** Generic timeout value for not blocking. */
	public static final int TIMEOUT_NOT_BLOCKING = 0;

	/** Generic timeout value for infinite timeout. */
	public static final int TIMEOUT_INFINITE = -1;

	/** List of Command packets received from Virtual Machine. */
	private final LinkedList<JdwpCommandPacket> fCommandPackets;

	/** List of Reply packets received from Virtual Machine. */
	private final LinkedList<JdwpReplyPacket> fReplyPackets;

	/**
	 * List of Packets that have timed out already. Maintained so that responses
	 * can be discarded if/when they are received.
	 */
	private final ArrayList<Integer> fTimedOutPackets;

	private final VirtualMachineImpl fVM;

	/**
	 * Create a new thread that receives packets from the Virtual Machine.
	 */
	public PacketReceiveManager(Connection connection, VirtualMachineImpl vmImpl) {
		super(connection);
		fVM = vmImpl;
		fCommandPackets = new LinkedList<>();
		fReplyPackets = new LinkedList<>();
		fTimedOutPackets = new ArrayList<>();
	}

	@Override
	public void disconnectVM() {
		super.disconnectVM();
		synchronized (fCommandPackets) {
			fCommandPackets.notifyAll();
		}
		synchronized (fReplyPackets) {
			fReplyPackets.notifyAll();
		}
	}

	/**
	 * Thread's run method.
	 */
	@Override
	public void run() {
		try {
			while (!VMIsDisconnected()) {
				// Read a packet from the input stream.
				readAvailablePacket();
			}
		}
		// if the remote VM is interrupted, drop the connection and clean up,
		// don't wait for it to happen on its own
		catch (InterruptedIOException e) {
			disconnectVM(e);
		} catch (IOException e) {
			disconnectVM(e);
		}
	}

	/**
	 * @return Returns a specified Command Packet from the Virtual Machine.
	 */
	public JdwpCommandPacket getCommand(int command, long timeToWait)
			throws InterruptedException {
		JdwpCommandPacket packet = null;
		synchronized (fCommandPackets) {
			long remainingTime = timeToWait;
			long timeBeforeWait;
			long waitedTime;

			// Wait until command is available.
			while (!VMIsDisconnected()
					&& (packet = removeCommandPacket(command)) == null
					&& (timeToWait < 0 || remainingTime > 0)) {
				timeBeforeWait = System.currentTimeMillis();
				waitForPacketAvailable(remainingTime, fCommandPackets);
				waitedTime = System.currentTimeMillis() - timeBeforeWait;
				remainingTime -= waitedTime;
			}
		}
		// Check for an IO Exception.
		if (VMIsDisconnected()) {
			String message;
			if (getDisconnectException() == null) {
				message = ConnectMessages.PacketReceiveManager_Got_IOException_from_Virtual_Machine_1;
			} else {
				String exMessage = getDisconnectException().getMessage();
				if (exMessage == null) {
					message = NLS.bind(ConnectMessages.PacketReceiveManager_Got__0__from_Virtual_Machine_1,
							getDisconnectException().getClass().getName());
				} else {
					message = NLS.bind(ConnectMessages.PacketReceiveManager_Got__0__from_Virtual_Machine___1__1,
							getDisconnectException().getClass().getName(), exMessage);
				}
			}
			throw new VMDisconnectedException(message);
		}
		// Check for a timeout.
		if (packet == null) {
			throw new TimeoutException();
		}
		return packet;
	}

	/**
	 * @return Returns a specified Reply Packet from the Virtual Machine.
	 */
	public JdwpReplyPacket getReply(int id, long timeToWait) {
		JdwpReplyPacket packet = null;
		long remainingTime = timeToWait;
		synchronized (fReplyPackets) {
			final long timeBeforeWait = System.currentTimeMillis();
			// Wait until reply is available.
			while (!VMIsDisconnected() && remainingTime > 0) {
				packet = removeReplyPacket(id);
				if (packet != null) {
					break;
				}
				try {
					waitForPacketAvailable(remainingTime, fReplyPackets);
				}
				// if the remote VM is interrupted DO NOT drop the connection -
				// see bug 171075
				// just stop waiting for the reply and treat it as a timeout
				catch (InterruptedException e) {
					if (JDIDebugOptions.DEBUG) {
						JDIDebugOptions.trace(null, "Interrupt observed while waiting for packet: " + id, e); //$NON-NLS-1$
					}
					// Do not stop waiting on interrupt, this causes
					// sporadic TimeoutException's without timeout
					// break;
				}
				long waitedTime = System.currentTimeMillis() - timeBeforeWait;
				remainingTime = timeToWait - waitedTime;
			}
		}
		if (packet == null) {
			synchronized (fReplyPackets) {
				packet = removeReplyPacket(id);
			}
		}
		// Check for an IO Exception.
		if (VMIsDisconnected()) {

			throw new VMDisconnectedException(
					ConnectMessages.PacketReceiveManager_Got_IOException_from_Virtual_Machine_2);

		}
		// Check for a timeout.
		if (packet == null) {
			synchronized (fTimedOutPackets) {
				fTimedOutPackets.add(Integer.valueOf(id));
			}
			throw new TimeoutException(NLS.bind(
					ConnectMessages.PacketReceiveManager_0, id + "")); //$NON-NLS-1$
		}
		return packet;
	}

	/**
	 * @return Returns a specified Reply Packet from the Virtual Machine.
	 */
	public JdwpReplyPacket getReply(JdwpCommandPacket commandPacket) {
		return getReply(commandPacket.getId(), fVM.getRequestTimeout());
	}

	/**
	 * Wait for an available packet from the Virtual Machine.
	 */
	private void waitForPacketAvailable(long timeToWait, Object lock)
			throws InterruptedException {
		if (timeToWait == 0) {

			return;

		} else if (timeToWait < 0) {

			lock.wait();

		} else {

			lock.wait(timeToWait);

		}
	}

	/**
	 * @return Returns and removes a specified command packet from the command
	 *         packet list.
	 */
	private JdwpCommandPacket removeCommandPacket(int command) {
		ListIterator<JdwpCommandPacket> iter = fCommandPackets.listIterator();
		while (iter.hasNext()) {
			JdwpCommandPacket packet = iter.next();
			if (packet.getCommand() == command) {
				iter.remove();
				return packet;
			}
		}
		return null;
	}

	/**
	 * @return Returns a specified reply packet from the reply packet list.
	 */
	private JdwpReplyPacket removeReplyPacket(int id) {
		ListIterator<JdwpReplyPacket> iter = fReplyPackets.listIterator();
		while (iter.hasNext()) {
			JdwpReplyPacket packet = iter.next();
			if (packet.getId() == id) {
				iter.remove();
				return packet;
			}
		}
		return null;
	}

	/**
	 * Add a command packet to the command packet list.
	 */
	private void addCommandPacket(JdwpCommandPacket packet) {
		if (isTimedOut(packet)) {
			return; // already timed out. No need to keep this one
		}
		synchronized (fCommandPackets) {
			fCommandPackets.add(packet);
			fCommandPackets.notifyAll();
		}
	}

	/**
	 * Returns whether the request for the given packet has already timed out.
	 *
	 * @param packet
	 *            response packet
	 * @return whether the request for the given packet has already timed out
	 */
	private boolean isTimedOut(JdwpPacket packet) {
		synchronized (fTimedOutPackets) {
			if (fTimedOutPackets.isEmpty()) {
				return false;
			}
			Integer id = Integer.valueOf(packet.getId());
			return fTimedOutPackets.remove(id);
		}
	}

	/**
	 * Add a reply packet to the reply packet list.
	 */
	private void addReplyPacket(JdwpReplyPacket packet) {
		if (isTimedOut(packet)) {
			return; // already timed out. No need to keep this one
		}
		synchronized (fReplyPackets) {
			fReplyPackets.add(packet);
			fReplyPackets.notifyAll();
		}
	}

	/**
	 * Read a packet from the input stream and add it to the appropriate packet
	 * list.
	 */
	private void readAvailablePacket() throws IOException {
		// Read a packet from the Input Stream.
		byte[] bytes = getConnection().readPacket();
		JdwpPacket packet = JdwpPacket.build(bytes);
		// Add packet to command or reply queue.
		if (packet instanceof JdwpCommandPacket) {

			addCommandPacket((JdwpCommandPacket) packet);

		} else {

			addReplyPacket((JdwpReplyPacket) packet);

		}
	}
}
