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
import java.io.InterruptedIOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.ListIterator;

import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpPacket;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.connect.spi.Connection;

/**
 * This class implements a thread that receives packets from the Virtual Machine.
 *
 */
public class PacketReceiveManager extends PacketManager {

    /** Generic timeout value for not blocking. */
	public static final int TIMEOUT_NOT_BLOCKING = 0;
	/** Generic timeout value for infinite timeout. */
	public static final int TIMEOUT_INFINITE = -1;

	/** List of Command packets received from Virtual Machine. */
	private LinkedList fCommandPackets;
	/** List of Reply packets received from Virtual Machine. */
	private LinkedList fReplyPackets;
    private VirtualMachineImpl fVM;

	/** 
	 * Create a new thread that receives packets from the Virtual Machine.
	 */
	public PacketReceiveManager(Connection connection, VirtualMachineImpl vmImpl) {
		super(connection);
		fVM = vmImpl;
		fCommandPackets = new LinkedList();
		fReplyPackets = new LinkedList();
	}

    public void disconnectVM() {
        super.disconnectVM();
        synchronized(fCommandPackets) {
            fCommandPackets.notifyAll();
        }
        synchronized (fReplyPackets) {
            fReplyPackets.notifyAll();
        }
    }

	/** 
	 * Thread's run method.
	 */
	public void run() {
		try {
			while (!VMIsDisconnected()) {
				// Read a packet from the input stream.
				readAvailablePacket();
			}
		} catch (InterruptedIOException e) {
			// Stop running.
		} catch (IOException e) {		    
			disconnectVM(e);
		}
	}
	
	/** 
	 * @return Returns a specified Command Packet from the Virtual Machine.
	 */
	public JdwpCommandPacket getCommand(int command, long timeToWait) throws InterruptedException {
		JdwpCommandPacket packet = null;
		
		synchronized(fCommandPackets) {
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
				message= ConnectMessages.getString("PacketReceiveManager.Got_IOException_from_Virtual_Machine_1"); //$NON-NLS-1$
			} else {
				String exMessage = getDisconnectException().getMessage();
				if (exMessage == null) {
					message= MessageFormat.format(ConnectMessages.getString("PacketReceiveManager.Got_{0}_from_Virtual_Machine_1"), new String[] {getDisconnectException().getClass().getName()}); //$NON-NLS-1$
				} else {
					message = MessageFormat.format(ConnectMessages.getString("PacketReceiveManager.Got_{0}_from_Virtual_Machine__{1}_1"), new String[]{getDisconnectException().getClass().getName(), exMessage}); //$NON-NLS-1$
				}
			}
			throw new VMDisconnectedException(message);
		}
			
		// Check for a timeout.
		if (packet == null)
			throw new TimeoutException();
			
		return packet;
	}
	
	/** 
	 * @return Returns a specified Reply Packet from the Virtual Machine.
	 */
	public JdwpReplyPacket getReply(int id, long timeToWait) {
		JdwpReplyPacket packet = null;
		
		synchronized(fReplyPackets) {
		    long remainingTime = timeToWait;
		    long timeBeforeWait;
		    long waitedTime;
		    
		    // Wait until reply is available.
		    while (!VMIsDisconnected() && (timeToWait < 0 || remainingTime > 0)) {
		        packet = removeReplyPacket(id);
		        if (packet != null) {
		            break;
		        }
		        
		        timeBeforeWait = System.currentTimeMillis();
		        try {
		            waitForPacketAvailable(remainingTime, fReplyPackets);
		        } catch (InterruptedException e) {
		        }
		        waitedTime = System.currentTimeMillis() - timeBeforeWait;
		        remainingTime -= waitedTime;
		    }
		}
		
		// Check for an IO Exception.
		if (VMIsDisconnected())
			throw new VMDisconnectedException(ConnectMessages.getString("PacketReceiveManager.Got_IOException_from_Virtual_Machine_2")); //$NON-NLS-1$
			
		// Check for a timeout.
		if (packet == null)
			throw new TimeoutException(MessageFormat.format(ConnectMessages.getString("PacketReceiveManager.0"), new String[] {id+""})); //$NON-NLS-1$ //$NON-NLS-2$

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
	private void waitForPacketAvailable(long timeToWait, Object lock) throws InterruptedException {
		if (timeToWait == 0)
			return;
		else if (timeToWait < 0)
			lock.wait();
		else
			lock.wait(timeToWait);
	}
	
	/** 
	 * @return Returns and removes a specified command packet from the command packet list.
	 */
	private JdwpCommandPacket removeCommandPacket(int command) {
		ListIterator iter = fCommandPackets.listIterator();
		while (iter.hasNext()) {
			JdwpCommandPacket packet = (JdwpCommandPacket)iter.next();
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
		ListIterator iter = fReplyPackets.listIterator();
		while (iter.hasNext()) {
			JdwpReplyPacket packet = (JdwpReplyPacket)iter.next();
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
	    synchronized (fCommandPackets) {
			fCommandPackets.add(packet);
			fCommandPackets.notifyAll();
        }
	}

	/** 
	 * Add a reply packet to the reply packet list.
	 */
	private void addReplyPacket(JdwpReplyPacket packet) {
	    synchronized (fReplyPackets) {
			fReplyPackets.add(packet);
			fReplyPackets.notifyAll();
        }
	}
	
	/** 
	 * Read a packet from the input stream and add it to the appropriate packet list.
	 */
	private void readAvailablePacket() throws IOException {	    
		// Read a packet from the Input Stream.
	    byte[] bytes = getConnection().readPacket();
		JdwpPacket packet = JdwpPacket.build(bytes);
		// Add packet to command or reply queue.
		if (packet instanceof JdwpCommandPacket)
			addCommandPacket((JdwpCommandPacket)packet);
		else
			addReplyPacket((JdwpReplyPacket)packet);
	}
}
