package org.eclipse.jdi.internal.connect;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
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
			disconnectVM();
		}
	}

	/** 
	 * Thread's run method.
	 */
	public void run() {
		try {
			while(true) {
				sendAvailablePackets();
			}
		} catch (InterruptedException e) {
			// Stop running.
		} catch (InterruptedIOException e) {
			// Stop running.
		} catch (IOException e) {
			disconnectVM();
		}
	}
	
	/** 
	 * Add a packet to be sent to the Virtual Machine.
	 */
	public synchronized void sendPacket(JdwpPacket packet) {
		if (VMIsDisconnected())
			throw (new VMDisconnectedException(ConnectMessages.getString("PacketSendManager.Got_IOException_from_Virtual_Machine_1"))); //$NON-NLS-1$

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
