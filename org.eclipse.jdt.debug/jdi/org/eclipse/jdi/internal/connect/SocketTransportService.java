/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Ivan Popov - Bug 184211: JDI connectors throw NullPointerException if used separately
 *     			from Eclipse
 *     Google Inc - add support for accepting multiple connections
 *******************************************************************************/
package org.eclipse.jdi.internal.connect;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdt.internal.debug.core.JDIDebugOptions;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

import com.sun.jdi.connect.TransportTimeoutException;
import com.sun.jdi.connect.spi.ClosedConnectionException;
import com.sun.jdi.connect.spi.Connection;
import com.sun.jdi.connect.spi.TransportService;

public class SocketTransportService extends TransportService {
	/** Handshake bytes used just after connecting VM. */
	private static final byte[] handshakeBytes = "JDWP-Handshake".getBytes(); //$NON-NLS-1$

	private final Capabilities fCapabilities = new Capabilities() {
		@Override
		public boolean supportsAcceptTimeout() {
			return true;
		}

		@Override
		public boolean supportsAttachTimeout() {
			return true;
		}

		@Override
		public boolean supportsHandshakeTimeout() {
			return true;
		}

		@Override
		public boolean supportsMultipleConnections() {
			return false;
		}
	};

	private static class SocketListenKey extends ListenKey {
		private final String fAddress;

		SocketListenKey(String address) {
			fAddress = address;
		}

		@Override
		public String address() {
			return fAddress;
		}
	}

	// for listening or accepting connectors
	private ServerSocket fServerSocket;

	@Override
	public Connection accept(ListenKey listenKey, long attachTimeout,
			long handshakeTimeout) throws IOException {
		if (attachTimeout > 0) {
			if (attachTimeout > Integer.MAX_VALUE) {
				attachTimeout = Integer.MAX_VALUE; // approx 25 days!
			}
			fServerSocket.setSoTimeout((int) attachTimeout);
		}
		Socket socket;
		try {
			socket = fServerSocket.accept();
		} catch (SocketTimeoutException e) {
			TransportTimeoutException timeoutException = new TransportTimeoutException();
			timeoutException.initCause(e);
			throw timeoutException;
		}
		InputStream input = socket.getInputStream();
		OutputStream output = socket.getOutputStream();
		performHandshake(input, output, handshakeTimeout);
		return new SocketConnection(socket, input, output);
	}

	@Override
	public Connection attach(String address, long attachTimeout,
			long handshakeTimeout) throws IOException {
		String[] strings = address.split(":"); //$NON-NLS-1$
		String host = "localhost"; //$NON-NLS-1$
		int port = 0;
		if (strings.length == 2) {
			host = strings[0];
			port = Integer.parseInt(strings[1]);
		} else {
			port = Integer.parseInt(strings[0]);
		}

		return attach(host, port, attachTimeout, handshakeTimeout);
	}

	public Connection attach(final String host, final int port,
			long attachTimeout, final long handshakeTimeout) throws IOException {
		if (attachTimeout > 0) {
			if (attachTimeout > Integer.MAX_VALUE) {
				attachTimeout = Integer.MAX_VALUE; // approx 25 days!
			}
		}

		final IOException[] ex = new IOException[1];
		final SocketConnection[] result = new SocketConnection[1];
		Thread attachThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Socket socket = new Socket(host, port);
					InputStream input = socket.getInputStream();
					OutputStream output = socket.getOutputStream();
					performHandshake(input, output, handshakeTimeout);
					result[0] = new SocketConnection(socket, input, output);
					ex[0] = null;
				} catch (IOException e) {
					if (ex[0] != null) {
						// second attempt failed, fail and keep original cause
						return;
					}
					ex[0] = e;
					// Let try yet another time. There is a hard coded timeout of 2 seconds
					// between opening socket and performing handshake
					// See related code in JDK 21:
					// https://github.com/openjdk/jdk21/blob/890adb6410dab4606a4f26a942aed02fb2f55387/src/jdk.jdwp.agent/share/native/libdt_socket/socketTransport.c#L794
					// https://github.com/openjdk/jdk21/blob/890adb6410dab4606a4f26a942aed02fb2f55387/src/jdk.jdwp.agent/share/native/libjdwp/transport.c#L629
					// https://github.com/openjdk/jdk21/blob/890adb6410dab4606a4f26a942aed02fb2f55387/src/jdk.jdwp.agent/share/native/libdt_socket/socketTransport.c#L201
					if (e.getCause() instanceof EOFException) {
						if (JDIDebugOptions.DEBUG) {
							JDIDebugOptions.trace(null, "Retrying handshake", e); //$NON-NLS-1$
						}
						run();
					}
				}
			}
		}, ConnectMessages.SocketTransportService_0);
		attachThread.setDaemon(true);
		attachThread.start();
		try {
			attachThread.join(attachTimeout);
			if (attachThread.isAlive()) {
				attachThread.interrupt();
				throw new TimeoutException();
			}
		} catch (InterruptedException e) {
			String message = String.format("Interrupted while trying to attach to %s:%s (attach timeout %s, handshake timeout %s)", host, port, attachTimeout, handshakeTimeout); //$NON-NLS-1$
			JDIDebugPlugin.logError(message, e);
		}

		if (ex[0] != null) {
			String message = String.format("Failed to attach to %s:%s (attach timeout %s, handshake timeout %s)", host, port, attachTimeout, handshakeTimeout); //$NON-NLS-1$
			throw new IOException(message, ex[0]);
		}

		return result[0];
	}

	void performHandshake(final InputStream in, final OutputStream out,
			final long timeout) throws IOException {
		final IOException[] ex = new IOException[1];
		final boolean[] handshakeCompleted = new boolean[1];

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					writeHandshake(out);
					readHandshake(in);
					handshakeCompleted[0] = true;
				} catch (IOException e) {
					ex[0] = e;
				}
			}
		}, ConnectMessages.SocketTransportService_1);
		t.setDaemon(true);
		t.start();
		InterruptedException interrupted = null;
		try {
			t.join(timeout);
		} catch (InterruptedException e1) {
			interrupted = e1;
		}

		if (handshakeCompleted[0]) {
			return;
		}

		try {
			in.close();
			out.close();
		} catch (IOException e) {
		}

		if (ex[0] != null) {
			throw ex[0];
		}
		TransportTimeoutException timeoutException = new TransportTimeoutException("Timeout occured on handshake"); //$NON-NLS-1$
		if (interrupted != null) {
			timeoutException.initCause(interrupted);
		}
		throw timeoutException;
	}

	private void readHandshake(InputStream input) throws IOException {
		try {
			DataInputStream in = new DataInputStream(input);
			byte[] handshakeInput = new byte[handshakeBytes.length];
			in.readFully(handshakeInput);
			if (!Arrays.equals(handshakeInput, handshakeBytes)) {
				throw new IOException("Received invalid handshake"); //$NON-NLS-1$
			}
		} catch (EOFException e) {
			ClosedConnectionException closedConnectionException = new ClosedConnectionException("Failed to read handshake"); //$NON-NLS-1$
			closedConnectionException.initCause(e);
			throw closedConnectionException;
		}
	}

	private void writeHandshake(OutputStream out) throws IOException {
		out.write(handshakeBytes);
	}

	@Override
	public Capabilities capabilities() {
		return fCapabilities;
	}

	@Override
	public String description() {
		return "org.eclipse.jdt.debug: Socket Implementation of TransportService"; //$NON-NLS-1$
	}

	@Override
	public String name() {
		return "org.eclipse.jdt.debug_SocketTransportService"; //$NON-NLS-1$
	}

	@Override
	public ListenKey startListening() throws IOException {
		// not used by jdt debug.
		return startListening(null);
	}

	@Override
	public ListenKey startListening(String address) throws IOException {
		String host = null;
		int port = -1;
		if (address != null) {
			// jdt debugger will always specify an address in
			// the form localhost:port
			String[] strings = address.split(":"); //$NON-NLS-1$
			host = "localhost"; //$NON-NLS-1$
			if (strings.length == 2) {
				host = strings[0];
				port = Integer.parseInt(strings[1]);
			} else {
				port = Integer.parseInt(strings[0]);
			}
		}
		if (port == -1) {
			throw new IOException("Unable to decode port from address: " + address); //$NON-NLS-1$
		}
		if (host == null) {
			host = "localhost"; //$NON-NLS-1$
		}

		fServerSocket = new ServerSocket(port);
		port = fServerSocket.getLocalPort();
		ListenKey listenKey = new SocketListenKey(host + ":" + port); //$NON-NLS-1$
		return listenKey;
	}

	@Override
	public void stopListening(ListenKey arg1) throws IOException {
		if (fServerSocket != null) {
			try {
				fServerSocket.close();
			} catch (IOException e) {
			}
		}
		fServerSocket = null;
	}
}
