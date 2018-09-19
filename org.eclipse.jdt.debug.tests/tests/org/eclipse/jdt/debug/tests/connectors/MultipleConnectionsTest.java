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
import org.junit.Before;
import org.junit.Test;

import com.sun.jdi.connect.Connector;

/**
 * Test the SocketListenerConnector
 */
public class MultipleConnectionsTest extends AbstractDebugTest {

	public MultipleConnectionsTest(String name) {
		super(name);
	}

	private ILaunch launch = new MockLaunch();

	private SocketListenConnector connector;

	private int port;

	@Override
	@Before
	protected void setUp() throws Exception {
		super.setUp();
		port = SocketUtil.findFreePort();
	}

	@Test
	public void testDefaultSettings() throws CoreException {
		connector = new SocketListenConnector();
		Map<String, Connector.Argument> defaults = connector.getDefaultArguments();
		assertTrue(defaults.containsKey("connectionLimit"));
		assertEquals(1, ((Connector.IntegerArgument) defaults.get("connectionLimit")).intValue());
	}

	/**
	 * Ensure out-of-the-box settings mimics previous behaviour of accepting a
	 * single connection
	 *
	 * @throws IOException
	 */
	@Test
	public void testDefaultBehaviour() throws CoreException, InterruptedException {
		connector = new SocketListenConnector();
		Map<String, String> arguments = new HashMap<>();
		arguments.put("port", Integer.toString(port));
		connector.connect(arguments, new NullProgressMonitor(), launch);
		Thread.sleep(200);

		assertTrue("first connect should succeed", connect());
		assertFalse("second connect should fail", connect());
	}

	/**
	 * Ensure connector accepts a single connection
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testSingleConnectionBehaviour() throws CoreException, InterruptedException {
		connector = new SocketListenConnector();
		Map<String, String> arguments = new HashMap<>();
		arguments.put("port", Integer.toString(port));
		arguments.put("connectionLimit", "1");
		connector.connect(arguments, new NullProgressMonitor(), launch);
		Thread.sleep(200);

		assertTrue("first connect should succeed", connect());
		assertFalse("second connect should fail", connect());
	}

	/**
	 * Ensure out-of-the-box settings mimics previous behaviour of accepting a
	 * single connection
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testTwoConnectionsBehaviour() throws CoreException, InterruptedException {
		connector = new SocketListenConnector();
		Map<String, String> arguments = new HashMap<>();
		arguments.put("port", Integer.toString(port));
		arguments.put("connectionLimit", "2");
		connector.connect(arguments, new NullProgressMonitor(), launch);
		Thread.sleep(200);

		assertTrue("first connect should succeed", connect());
		assertTrue("second connect should succeed", connect());
	}

	/**
	 * Ensure out-of-the-box settings mimics previous behaviour of accepting a
	 * single connection
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testUnlimitedConnectionsBehaviour() throws CoreException, InterruptedException {
		connector = new SocketListenConnector();
		Map<String, String> arguments = new HashMap<>();
		arguments.put("port", Integer.toString(port));
		arguments.put("connectionLimit", "0");
		connector.connect(arguments, new NullProgressMonitor(), launch);
		Thread.sleep(200);

		for (int i = 0; i < 10; i++) {
			assertTrue("connection " + i + " should succeed", connect());
		}
	}

	@Override
	@After
	protected void tearDown() throws Exception {
		launch.terminate();
		super.tearDown();
	}

	private boolean connect() {
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
		try {
			// sleep to allow the remote side to setup the connection
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			// ignore
		}
		return result;
	}
}
