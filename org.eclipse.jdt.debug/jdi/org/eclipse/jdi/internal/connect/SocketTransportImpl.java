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


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

public class SocketTransportImpl extends TransportImpl {
	public static final int MIN_PORTNR = 0;
	public static final int MAX_PORTNR = 65535;
	
	/** Handshake bytes used just after connecting VM. */
	private static final byte[] handshakeBytes = "JDWP-Handshake".getBytes(); //$NON-NLS-1$
	/** Socket on which VM is connected. */
	private Socket fSocket = null;
	/** ServerSocker used to listen to connecting VMs. */
	private ServerSocket fServerSocket = null;
	
	/**
	 * Constructs new SocketTransportImpl.
	 */	
	public SocketTransportImpl() {
		super("dt_socket"); //$NON-NLS-1$
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
		closeListen();
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
				throw new IOException(ConnectMessages.getString("SocketTransportImpl.Incorrect_handshake_reply_received___1") + new String(handshakeInput)); //$NON-NLS-1$
		} catch (EOFException e) {
			throw new IOException(ConnectMessages.getString("SocketTransportImpl.EOF_encoutered_during_handshake_2")); //$NON-NLS-1$
		}
	}
}
