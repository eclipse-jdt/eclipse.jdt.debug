package org.eclipse.jdi.internal.connect;/*
 * JDI class Implementation
 *
 * (BB)
 * (C) Copyright IBM Corp. 2000
 */



import java.io.*;
import java.util.*;
import com.sun.jdi.*;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdi.internal.*;
import org.eclipse.jdi.internal.jdwp.*;

/**
 * This class implements a thread that receives packets from the Virtual Machine.
 *
 */
public class PacketReceiveManager extends PacketManager {
	/** Generic timeout value for not blocking. */
	public static final int TIMEOUT_NOT_BLOCKING = 0;
	/** Generic timeout value for infinite timeout. */
	public static final int TIMEOUT_INFINITE = -1;

	/** Virtual Machine. */
	private VirtualMachineImpl fVM;
	/** Input Stream from Virtual Machine. */
	private InputStream fInStream;
	/** List of Command packets received from Virtual Machine. */
	private LinkedList fCommandPackets;
	/** List of Reply packets received from Virtual Machine. */
	private LinkedList fReplyPackets;

	/** 
	 * Create a new thread that receives packets from the Virtual Machine.
	 */
	public PacketReceiveManager(ConnectorImpl connector) {
		super(connector);
		try {
			fVM = connector.virtualMachine();
			fInStream = connector.getInputStream();
			fCommandPackets = new LinkedList();
			fReplyPackets = new LinkedList();
		} catch (IOException e) {
			disconnectVM();
		}
	}

	/** 
	 * Thread's run method.
	 */
	public void run() {
		try {
			while (true) {
				// Read a packet from the input stream.
				readAvailablePacket();
			}
		} catch (InterruptedIOException e) {
			// Stop running.
		} catch (IOException e) {
			disconnectVM();
		}
	}
	
	/** 
	 * @return Returns a specified Command Packet from the Virtual Machine.
	 */
	public synchronized JdwpCommandPacket getCommand(int  command, long timeToWait) throws InterruptedException {
		JdwpCommandPacket packet = null;
		long remainingTime = timeToWait;
		long timeBeforeWait;
		long waitedTime;
		
		// Wait until command is available.
		while (!VMIsDisconnected()
					&& (packet = removeCommandPacket(command)) == null
					&& (timeToWait < 0 || remainingTime > 0)) {
			timeBeforeWait = System.currentTimeMillis();
			waitForPacketAvailable(timeToWait);
			waitedTime = System.currentTimeMillis() - timeBeforeWait;
			remainingTime -= waitedTime;
		}
		
		// Check for an IO Exception.
		if (VMIsDisconnected())
			throw new VMDisconnectedException("Got IOException from Virtual Machine");
			
		// Check for a timeout.
		if (packet == null)
			throw new TimeoutException();
			
		return packet;
	}
	
	/** 
	 * @return Returns a specified Reply Packet from the Virtual Machine.
	 */
	public synchronized JdwpReplyPacket getReply(int id, long timeToWait) throws InterruptedException {
		JdwpReplyPacket packet = null;
		long remainingTime = timeToWait;
		long timeBeforeWait;
		long waitedTime;
		
		// Wait until reply is available.
		while (!VMIsDisconnected()
					&& (packet = removeReplyPacket(id)) == null
					&& (timeToWait < 0 || remainingTime > 0)) {
			timeBeforeWait = System.currentTimeMillis();
			waitForPacketAvailable(timeToWait);
			waitedTime = System.currentTimeMillis() - timeBeforeWait;
			remainingTime -= waitedTime;
		}
		
		// Check for an IO Exception.
		if (VMIsDisconnected())
			throw new VMDisconnectedException("Got IOException from Virtual Machine");
			
		// Check for a timeout.
		if (packet == null)
			throw new TimeoutException();

		return packet;
	}
	
	/** 
	 * @return Returns a specified Reply Packet from the Virtual Machine.
	 */
	public JdwpReplyPacket getReply(JdwpCommandPacket commandPacket) {
		try {
			return getReply(commandPacket.getId(), fVM.getRequestTimeout());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return null;
	}

	/** 
	 * Wait for an available packet from the Virtual Machine.
	 */
	private void waitForPacketAvailable(long timeToWait) throws InterruptedException {
		if (timeToWait == 0)
			return;
		else if (timeToWait < 0)
			wait();
		else
			wait(timeToWait);
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
	private synchronized void addCommandPacket(JdwpCommandPacket packet) {
		fCommandPackets.add(packet);
		notifyAll();
	}

	/** 
	 * Add a reply packet to the reply packet list.
	 */
	private synchronized void addReplyPacket(JdwpReplyPacket packet) {
		fReplyPackets.add(packet);
		notifyAll();
	}
	
	/** 
	 * Read a packet from the input stream and add it to the appropriate packet list.
	 */
	private void readAvailablePacket() throws IOException {
		// Read a packet from the Input Stream.
		JdwpPacket packet = JdwpPacket.read(fInStream);
		
		// Add packet to command or reply queue.
		if (packet instanceof JdwpCommandPacket)
			addCommandPacket((JdwpCommandPacket)packet);
		else
			addReplyPacket((JdwpReplyPacket)packet);
	}
}
