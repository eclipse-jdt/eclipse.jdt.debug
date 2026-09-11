/*******************************************************************************
 * Copyright (c) 2026 contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.debug.jdi.tests;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

/** Tests the deadline and subsequent cleanup with the real JDI connector. */
public class VMConnectionTimeoutTest extends TestCase {

	public void testPeerThatDoesNotCompleteHandshakeCannotBlockStartup() throws Exception {
		assertSilentPeerIsBounded(false);
	}

	public void testHandshakeTimeoutReachesCleanupAndRetainsDiagnostics() throws Exception {
		assertSilentPeerIsBounded(true);
	}

	private void assertSilentPeerIsBounded(boolean throughStartup) throws Exception {
		int previousPort = AbstractJDITest.fBackEndPort;
		ConnectionProbe probe = new ConnectionProbe();
		FutureTask<Void> attempt = new FutureTask<>(() -> {
			if (throughStartup) {
				probe.launchTargetAndConnectToVM();
			} else {
				probe.connectToVM();
			}
			return null;
		});
		Thread connecting = new Thread(attempt, "JDI startup timeout test");
		connecting.setDaemon(true);
		try (ServerSocket listener = new ServerSocket(0)) {
			listener.setSoTimeout(10000);
			AbstractJDITest.fBackEndPort = listener.getLocalPort();
			connecting.start();
			try (Socket peer = listener.accept()) {
				peer.setSoTimeout(10000);
				assertEquals("JDWP-Handshake", new String(peer.getInputStream().readNBytes(14), StandardCharsets.US_ASCII));
				// Keep the connection open without answering its JDWP handshake.
				try {
					attempt.get(10, TimeUnit.SECONDS);
					fail("A peer without a JDWP handshake must not be accepted as a VM");
				} catch (ExecutionException expected) {
					Throwable failure = expected.getCause();
					assertTrue("Startup must report its ordinary connection failure: " + failure, failure instanceof Error);
					assertEquals("Could not contact the VM", failure.getMessage());
					assertTrue("Retain the transport timeout as the cause", failure.getCause() instanceof org.eclipse.jdi.TimeoutException);
					assertTrue("Record the process state before cleanup, not after destroying it",
							Arrays.stream(failure.getSuppressed()).anyMatch(detail -> detail.getMessage() != null
									&& detail.getMessage().contains("pid=42, alive=true")
									&& detail.getMessage().contains("localhost:" + listener.getLocalPort())));
					if (throughStartup) {
						assertFalse("Cleanup must terminate the owned process", probe.process.isAlive());
						assertNull("Cleanup must clear the target handle", probe.fLaunchedVM);
						assertNull(probe.fConsoleReader);
						assertNull(probe.fConsoleErrorReader);
					}
				} catch (TimeoutException expected) {
					fail("The connector blocked past the startup deadline on a silent peer");
				}
			}
		} finally {
			// Closing the peer releases even the unfixed implementation.
			try {
				connecting.join(10000);
				assertFalse("Startup thread did not terminate after the peer closed", connecting.isAlive());
			} finally {
				probe.stopConsoleTestReaders();
				probe.process.destroy();
				AbstractJDITest.fBackEndPort = previousPort;
			}
		}
	}

	private static final class ConnectionProbe extends AbstractJDITest {
		final Process process = new StreamOnlyProcess();

		ConnectionProbe() {
			fLaunchedVM = process;
		}

		@Override
		protected void launchTarget() {
			// Only the process is substituted; attach uses the real transport.
		}

		@Override
		public void localSetUp() {
			// No target program is needed for a transport-level startup failure.
		}

		void stopConsoleTestReaders() {
			if (fConsoleReader != null) {
				fConsoleReader.stop();
			}
			if (fConsoleErrorReader != null) {
				fConsoleErrorReader.stop();
			}
		}
	}

	private static final class StreamOnlyProcess extends Process {
		private volatile boolean alive = true;

		@Override
		public OutputStream getOutputStream() {
			return OutputStream.nullOutputStream();
		}

		@Override
		public InputStream getInputStream() {
			return InputStream.nullInputStream();
		}

		@Override
		public InputStream getErrorStream() {
			return InputStream.nullInputStream();
		}

		@Override
		public int waitFor() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int exitValue() {
			if (alive) {
				throw new IllegalThreadStateException();
			}
			return 0;
		}

		@Override
		public long pid() {
			return 42;
		}

		@Override
		public void destroy() {
			alive = false;
		}
	}
}
