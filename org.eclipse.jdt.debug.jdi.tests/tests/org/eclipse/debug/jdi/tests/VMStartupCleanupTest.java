/*******************************************************************************
 * Copyright (c) 2026 Carsten Hammer and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Carsten Hammer - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.jdi.tests;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.sun.jdi.VirtualMachine;

import junit.framework.TestCase;

/** Tests failure paths without relying on a slow or overloaded debug VM. */
public class VMStartupCleanupTest extends TestCase {
	private int previousPort;
	private String previousCommand;

	@Override
	protected void setUp() {
		previousPort = AbstractJDITest.fBackEndPort;
		previousCommand = AbstractJDITest.fVmCmd;
		AbstractJDITest.fVmCmd = null;
	}

	@Override
	protected void tearDown() {
		AbstractJDITest.fBackEndPort = previousPort;
		AbstractJDITest.fVmCmd = previousCommand;
	}

	public void testFailedAttachCleansProcessesWithoutEventReader() {
		Fixture fixture = new Fixture();
		FakeProcess vm = new FakeProcess();
		FakeProcess proxy = new FakeProcess();
		fixture.nextVM = vm;
		fixture.nextProxy = proxy;
		fixture.attachFailure = new Error("attach failed");
		AbstractJDITest.fBackEndPort = 0;

		assertStartupFailure(fixture, fixture.attachFailure);

		assertFalse(vm.isAlive());
		assertFalse(proxy.isAlive());
		assertCleared(fixture);
		assertTrue(AbstractJDITest.fBackEndPort > 0);
	}

	public void testLaunchFailureCleansAlreadyStartedProxy() {
		Fixture fixture = new Fixture();
		FakeProcess proxy = new FakeProcess();
		fixture.nextProxy = proxy;
		fixture.launchFailure = new Error("VM launch failed");

		assertStartupFailure(fixture, fixture.launchFailure);

		assertFalse(proxy.isAlive());
		assertCleared(fixture);
	}

	public void testProgramStartupFailureCleansVMAndReaders() {
		Fixture fixture = new Fixture();
		FakeProcess vm = new FakeProcess();
		fixture.nextVM = vm;
		fixture.programFailure = new IllegalStateException("program did not become ready");
		AbstractReader reader = new AbstractReader("cleanup test reader") {
			@Override
			protected void readerLoop() {
			}
		};
		fixture.fConsoleReader = reader;
		fixture.fConsoleErrorReader = reader;
		fixture.fProxyReader = reader;
		fixture.fProxyErrorReader = reader;

		assertStartupFailure(fixture, fixture.programFailure);

		assertTrue(reader.fIsStopping);
		assertFalse(vm.isAlive());
		assertCleared(fixture);
	}

	public void testCleanupFailureDoesNotReplaceStartupFailure() {
		Fixture fixture = new Fixture();
		FakeProcess vm = new FakeProcess();
		vm.refuseExit = true;
		FakeProcess proxy = new FakeProcess();
		fixture.nextVM = vm;
		fixture.nextProxy = proxy;
		fixture.attachFailure = new Error("original attach failure");

		assertStartupFailure(fixture, fixture.attachFailure);

		assertEquals(1, fixture.attachFailure.getSuppressed().length);
		assertTrue(fixture.attachFailure.getSuppressed()[0] instanceof IllegalStateException);
		assertFalse(proxy.isAlive());
		assertSame(vm, fixture.fLaunchedVM);
		vm.alive = false;
		fixture.shutDownTarget();
		assertCleared(fixture);
	}

	public void testShutdownIsIdempotent() {
		Fixture fixture = new Fixture();
		FakeProcess vm = new FakeProcess();
		fixture.fLaunchedVM = vm;
		fixture.shutDownTarget();
		int port = AbstractJDITest.fBackEndPort;
		fixture.shutDownTarget();
		assertEquals(List.of("destroy", "wait"), vm.calls);
		assertEquals(port, AbstractJDITest.fBackEndPort);
		assertCleared(fixture);
	}

	public void testCustomVMCommandKeepsPort() {
		AbstractJDITest.fVmCmd = "user supplied command";
		Fixture fixture = new Fixture();
		fixture.fLaunchedVM = new FakeProcess();
		fixture.shutDownTarget();
		assertEquals(previousPort, AbstractJDITest.fBackEndPort);
		assertCleared(fixture);
	}

	public void testNextStartupDoesNotReuseFailedVM() {
		Fixture fixture = new Fixture();
		FakeProcess first = new FakeProcess();
		fixture.nextVM = first;
		fixture.attachFailure = new Error("first attach failed");
		assertStartupFailure(fixture, fixture.attachFailure);
		assertCleared(fixture);

		FakeProcess second = new FakeProcess();
		fixture.nextVM = second;
		fixture.attachFailure = null;
		try {
			fixture.launchTargetAndConnectToVM();
			assertFalse(first.isAlive());
			assertTrue(second.isAlive());
			assertSame(second, fixture.fLaunchedVM);
			assertNotNull(fixture.fVM);
		} finally {
			fixture.shutDownTarget();
		}
		assertCleared(fixture);
	}

	public void testInterruptedAttachCleansVMAndPreservesInterrupt() {
		Fixture fixture = new Fixture();
		FakeProcess vm = new FakeProcess();
		fixture.nextVM = vm;
		fixture.useRealConnect = true;
		Thread.currentThread().interrupt();
		try {
			try {
				fixture.launchTargetAndConnectToVM();
				fail("Interrupted startup should fail");
			} catch (Error failure) {
				assertTrue(failure.getCause() instanceof InterruptedException);
			}
			assertTrue(Thread.currentThread().isInterrupted());
			assertFalse(vm.isAlive());
			assertCleared(fixture);
		} finally {
			Thread.interrupted();
		}
	}

	public void testGracefulTerminationWaitsForExit() {
		FakeProcess process = new FakeProcess();
		TestProcessCleanup.terminate(10000, process);
		assertEquals(List.of("destroy", "wait"), process.calls);
		assertFalse(process.isAlive());
	}

	public void testForcedTerminationAlsoWaitsForExit() {
		FakeProcess process = new FakeProcess();
		process.forceNeeded = true;
		TestProcessCleanup.terminate(10000, process);
		assertEquals(List.of("destroy", "wait", "force", "wait"), process.calls);
		assertFalse(process.isAlive());
	}

	public void testInterruptedWaitStillTerminatesProcess() {
		FakeProcess process = new FakeProcess();
		process.interruptWait = true;
		try {
			TestProcessCleanup.terminate(10000, process);
			assertEquals(List.of("destroy", "wait", "force", "wait"), process.calls);
			assertFalse(process.isAlive());
			assertTrue(Thread.currentThread().isInterrupted());
		} finally {
			Thread.interrupted();
		}
	}

	public void testAlreadyInterruptedCleanupStillWaitsForExit() {
		FakeProcess process = new FakeProcess();
		Thread.currentThread().interrupt();
		try {
			TestProcessCleanup.terminate(10000, process);
			assertEquals(List.of("destroy", "force", "wait"), process.calls);
			assertFalse(process.isAlive());
			assertTrue(Thread.currentThread().isInterrupted());
		} finally {
			Thread.interrupted();
		}
	}

	public void testAlreadyExitedAndMissingProcessesAreIgnored() {
		FakeProcess process = new FakeProcess();
		process.alive = false;
		TestProcessCleanup.terminate(10000, null, process);
		assertTrue(process.calls.isEmpty());
	}

	public void testAllProcessesAreAttemptedAndFailuresPreserved() {
		FakeProcess first = new FakeProcess();
		FakeProcess second = new FakeProcess();
		FakeProcess third = new FakeProcess();
		first.refuseExit = true;
		second.refuseExit = true;
		try {
			TestProcessCleanup.terminate(10000, first, second, third);
			fail("Unterminated processes must be reported");
		} catch (IllegalStateException failure) {
			assertTrue(failure.getMessage().contains("42"));
			assertEquals(1, failure.getSuppressed().length);
		}
		assertEquals(List.of("destroy", "wait", "force", "wait"), first.calls);
		assertEquals(first.calls, second.calls);
		assertFalse(third.isAlive());
	}

	public void testFailedStartupTerminatesRealJavaProcess() throws Exception {
		Path directory = Files.createTempDirectory("jdi-startup-cleanup");
		Path ready = directory.resolve("ready");
		Process process = null;
		try {
			String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
			process = new ProcessBuilder(java, "-cp", AbstractJDITest.fClassPath,
					WaitingProcess.class.getName(), ready.toString())
					.redirectError(ProcessBuilder.Redirect.INHERIT).start();
			long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(AbstractJDITest.TIMEOUT);
			while (!Files.exists(ready) && process.isAlive() && System.nanoTime() < deadline) {
				Thread.sleep(10);
			}
			assertTrue("Child JVM did not become ready", Files.exists(ready));
			assertTrue("Child JVM exited before cleanup", process.isAlive());
			Fixture fixture = new Fixture();
			fixture.nextVM = process;
			fixture.attachFailure = new Error("injected attach failure");
			assertStartupFailure(fixture, fixture.attachFailure);
			assertFalse("Failed startup left its JVM alive", process.isAlive());
			assertCleared(fixture);
		} finally {
			try {
				if (process != null) {
					process.destroyForcibly();
					assertTrue("Could not clean up child JVM", process.waitFor(5, TimeUnit.SECONDS));
				}
			} finally {
				Files.deleteIfExists(ready);
				Files.deleteIfExists(directory);
			}
		}
	}

	private static void assertStartupFailure(Fixture fixture, Throwable expected) {
		try {
			fixture.runBare();
			fail("Startup should fail");
		} catch (Throwable actual) {
			assertSame(expected, actual);
		}
		assertFalse("Test body must not run after failed setUp", fixture.testRan);
		assertFalse("JUnit must not be relied on to clean up failed setUp", fixture.tornDown);
	}

	private static void assertCleared(Fixture fixture) {
		assertNull(fixture.fVM);
		assertNull(fixture.fLaunchedVM);
		assertNull(fixture.fLaunchedProxy);
		assertNull(fixture.fEventReader);
		assertNull(fixture.fConsoleReader);
		assertNull(fixture.fConsoleErrorReader);
		assertNull(fixture.fProxyReader);
		assertNull(fixture.fProxyErrorReader);
	}

	private static class Fixture extends AbstractJDITest {
		Process nextVM;
		Process nextProxy;
		Error launchFailure;
		Error attachFailure;
		RuntimeException programFailure;
		boolean useRealConnect;
		boolean testRan;
		boolean tornDown;

		@Override
		protected void launchTarget() {
			fLaunchedProxy = nextProxy;
			if (launchFailure != null) {
				throw launchFailure;
			}
			fLaunchedVM = nextVM;
		}

		@Override
		protected void connectToVM() {
			if (useRealConnect) {
				super.connectToVM();
				return;
			}
			if (attachFailure != null) {
				throw attachFailure;
			}
			fVM = (VirtualMachine) Proxy.newProxyInstance(VirtualMachine.class.getClassLoader(),
					new Class<?>[] { VirtualMachine.class }, (proxy, method, args) -> {
						if (method.getName().equals("exit")) {
							return null;
						}
						throw new AssertionError("Unexpected VM call: " + method.getName());
					});
			fEventReader = new EventReader("cleanup test event reader", null);
		}

		@Override
		protected void startProgram() {
			if (programFailure != null) {
				throw programFailure;
			}
		}

		@Override
		public void localSetUp() {
		}

		@Override
		protected void runTest() {
			testRan = true;
		}

		@Override
		protected void tearDown() {
			tornDown = true;
			shutDownTarget();
		}
	}

	private static class FakeProcess extends Process {
		final List<String> calls = new ArrayList<>();
		boolean alive = true;
		boolean forceNeeded;
		boolean forced;
		boolean refuseExit;
		boolean interruptWait;

		@Override
		public boolean isAlive() {
			return alive;
		}

		@Override
		public long pid() {
			return 42;
		}

		@Override
		public void destroy() {
			calls.add("destroy");
		}

		@Override
		public Process destroyForcibly() {
			calls.add("force");
			forced = true;
			return this;
		}

		@Override
		public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
			assertTrue("Wait must be bounded and positive", timeout > 0);
			calls.add("wait");
			if (interruptWait) {
				interruptWait = false;
				throw new InterruptedException();
			}
			if (refuseExit || (forceNeeded && !forced)) {
				return false;
			}
			alive = false;
			return true;
		}

		@Override
		public int waitFor() {
			throw new AssertionError("Unbounded waitFor must not be used");
		}

		@Override
		public int exitValue() {
			if (alive) {
				throw new IllegalThreadStateException();
			}
			return 0;
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
		public OutputStream getOutputStream() {
			return OutputStream.nullOutputStream();
		}
	}

	/** A real subprocess that cannot exit normally before cleanup is requested. */
	public static class WaitingProcess {
		public static void main(String[] args) throws Exception {
			Files.writeString(Path.of(args[0]), "ready");
			new CountDownLatch(1).await();
		}
	}
}
