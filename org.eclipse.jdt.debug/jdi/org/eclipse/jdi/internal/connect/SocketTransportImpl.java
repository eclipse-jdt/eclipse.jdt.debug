package org.eclipse.jdi.internal.connect;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.net.*;
import java.util.*;
import java.io.*;

public class SocketTransportImpl extends TransportImpl {
	/** Handshake bytes used just after connecting VM. */
	private static final byte[] handshakeBytes = "JDWP-Handshake".getBytes();
	/** Socket on which VM is connected. */
	private Socket fSocket = null;
	/** ServerSocker used to listen to connecting VMs. */
	private ServerSocket fServerSocket = null;
	
	/**
	 * Constructs new SocketTransportImpl.
	 */	
	public SocketTransportImpl() {
		super("dt_socket");
	}

	/**
	 * Establishes a client connection to a virtual machine.
	 */
	public void attach(String hostname, int port) throws IOException {
		fSocket = new Socket(hostname, port);
		PerformHandshake();
	}
	
	/**
	 * Listens for connections initiated by target VMs. 
	 */
	public void listen(int port) throws IOException {
		fServerSocket = new ServerSocket(port);
	}
	
	/**
	 * @return Returns port number that is listened to. 
	 */
	public int listeningPort() {
		if (fServerSocket != null)
			return fServerSocket.getLocalPort();
		else
			return 0;
	}
	
	/**
	 * Closes socket connection.
	 */	
	public void closeListen() throws IOException {
		if (fServerSocket == null)
			return;

		fServerSocket.close();
		fServerSocket = null;
	}
	
	/**
	 * Accepts connections initiated by target VMs. 
	 */
	public void accept() throws IOException {
		fSocket =fServerSocket.accept();
		PerformHandshake();
	}
	
	/**
	 * Sets timeout on accept. 
	 */
	public void setAcceptTimeout(int timeout) throws SocketException {
		fServerSocket.setSoTimeout(timeout);
	}
	
	/**
	 * @return Returns true if we have an open connection.
	 */
	public boolean isOpen() {
		return fSocket != null;
	}
	
	/**
	 * Closes socket connection.
	 */	
	public void close() {
		if (fSocket == null)
			return;

		try {
			fSocket.close();
		} catch (IOException e) {
		} finally {
			fSocket = null;
		}
	}

	/**
	 * @return Returns InputStream from Virtual Machine.
	 */	
	public InputStream getInputStream() throws IOException {
		return fSocket.getInputStream();
	}
	
	/**
	 * @return Returns OutputStream to Virtual Machine.
	 */	
	public OutputStream getOutputStream() throws IOException {
		return fSocket.getOutputStream();
	}

  	/**
 	 * Performs handshake protocol.
	 */
	private void PerformHandshake() throws IOException {
		DataOutputStream out = new DataOutputStream(fSocket.getOutputStream());
		out.write(handshakeBytes);
		
		try {
			DataInputStream in = new DataInputStream(fSocket.getInputStream());
			byte[] handshakeInput = new byte[handshakeBytes.length];
			in.readFully(handshakeInput);
			if (!Arrays.equals(handshakeInput,handshakeBytes))
				throw new IOException("Incorrect handshake reply received: " + new String(handshakeInput));
		} catch (EOFException e) {
			throw new IOException("EOF encoutered during handshake");
		}
	}
}