/*******************************************************************************
 * Copyright (c) 2016 Google, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Google, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.debug.tests.connectors;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.SocketListenConnector;
import org.eclipse.jdt.launching.SocketUtil;
import org.junit.After;
import org.junit.Test;

import com.sun.jdi.connect.Connector;

/**
 * Test the SocketListenerConnector
 */
public class MultipleConnectionsTest extends AbstractDebugTest {

	public MultipleConnectionsTest(String name) {
		super(name);
	}

	private final ILaunch launch = new MockLaunch();

	private SocketListenConnector connector;

	private int port;

	@Test
	public void testDefaultSettings() throws Exception {
		connector = new SocketListenConnector();
		Map<String, Connector.Argument> defaults = connector.getDefaultArguments();
		assertTrue(defaults.containsKey("connectionLimit"));
		assertEquals(1, ((Connector.IntegerArgument) defaults.get("connectionLimit")).intValue());
	}

	/**
	 * Ensure out-of-the-box settings mimics previous behaviour of accepting a
	 * single connection
	 */
	@Test
	public void testDefaultBehaviour() throws Exception {
		connector = new SocketListenConnector();
		Map<String, String> arguments = new HashMap<>();
		initialConnect(arguments);

		assertTrue("first connect should succeed", connect());
		assertFalse("second connect should fail", connect());
	}

	/**
	 * Ensure connector accepts a single connection
	 */
	@Test
	public void testSingleConnectionBehaviour() throws Exception {
		connector = new SocketListenConnector();
		Map<String, String> arguments = new HashMap<>();
		arguments.put("connectionLimit", "1");
		initialConnect(arguments);

		assertTrue("first connect should succeed", connect());
		assertFalse("second connect should fail", connect());
	}

	/**
	 * Ensure out-of-the-box settings mimics previous behaviour of accepting a
	 * single connection
	 */
	@Test
	public void testTwoConnectionsBehaviour() throws Exception {
		connector = new SocketListenConnector();
		Map<String, String> arguments = new HashMap<>();
		arguments.put("connectionLimit", "2");
		initialConnect(arguments);

		assertTrue("first connect should succeed", connect());
		assertTrue("second connect should succeed", connect());
	}

	/**
	 * Ensure out-of-the-box settings mimics previous behaviour of accepting a
	 * single connection
	 */
	@Test
	public void testUnlimitedConnectionsBehaviour() throws Exception {
		connector = new SocketListenConnector();
		Map<String, String> arguments = new HashMap<>();
		arguments.put("connectionLimit", "0");
		initialConnect(arguments);

		for (int i = 0; i < 10; i++) {
			assertTrue("connection " + i + " should succeed", connect());
		}
	}

	private void initialConnect(Map<String, String> arguments) throws Exception {
		for (int i = 0; i < SOCKET_BIND_ERROR_MAX_RETRIES; ++i) {
			try {
				port = SocketUtil.findFreePort();
				arguments.put("port", Integer.toString(port));
				connector.connect(arguments, new NullProgressMonitor(), launch);
				break;
			} catch (CoreException e) {
				if (!(e.getCause() instanceof BindException) || i + 1 >= SOCKET_BIND_ERROR_MAX_RETRIES) {
					throw e;
				}
			}
		}
	}

	@Override
	@After
	protected void tearDown() throws Exception {
		launch.terminate();
		super.tearDown();
	}

	private boolean connect() throws Exception {
		boolean result = true;
		// Two try blocks to distinguish between exceptions from socket close (ignorable)
		// and from dealing with the remote (errors)
		try (Socket s = new Socket()) {
			try {
				s.connect(new InetSocketAddress(InetAddress.getLocalHost(), port));
				byte[] buffer = new byte[14];
				s.getInputStream().read(buffer);
				assertEquals("JDWP-Handshake", new String(buffer));
				s.getOutputStream().write("JDWP-Handshake".getBytes());
				s.getOutputStream().flush();
				// Closing gracelessly like this produces
				// com.sun.jdi.VMDisconnectedExceptions on the log. Could
				// respond to JDWP to try to bring down the connections
				// gracefully, but it's a bit involved.
			} catch (IOException e) {
				result = false;
			}
		} catch(IOException e) {
		}
		return result;
	}
}
