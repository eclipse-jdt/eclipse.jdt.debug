/*******************************************************************************
 * Copyright (c) 2022 Microsoft Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Microsoft Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.jdi.tests;

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.eclipse.jdi.internal.ThreadReferenceImpl;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;

@SuppressWarnings("restriction")
public class VirtualThreadTest extends AbstractJDITest {
	private static final String defaultJavaCompilerName = "com.sun.tools.javac.api.JavacTool";
	private static JavaCompiler compiler;
	static {
		compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			try {
				compiler = (JavaCompiler) Class.forName(defaultJavaCompilerName).getDeclaredConstructor().newInstance();
			} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException
					| IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private ThreadReference fVirtualThread;
	private ThreadReference fThread;
	private ThreadReference fMainThread;

	public VirtualThreadTest() {
		fVmArgs = "--enable-preview";
	}

	/**
	 * Init the fields that are used by this test only.
	 *
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	@Override
	public void localSetUp() {
		// Get thread
		fVirtualThread = getThread("fVirtualThread");
		fThread = getThread();
		fMainThread = getMainThread();
	}

	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		// The test has resumed the test thread, so suspend it
		waitUntilReady();
	}

	@Override
	protected void setUp() {
		compileTestProgram();
		super.setUp();
	}

	@Override
	public void setVMInfo(VMInformation info) {
		// do nothing
	}

	public static void main(String[] args) {
		compileTestProgram();
		new VirtualThreadTest().runSuite(args);
	}

	protected static void compileTestProgram() {
		if (Runtime.version().feature() < 19) {
			return;
		}

		String sourceFilePath = new File("./java19/TryVirtualThread.java").getAbsolutePath();
		String outputFilePath = new File("./bin").getAbsolutePath();
		compileFiles(sourceFilePath, outputFilePath);
	}

	private static void compileFiles(String sourceFilePath, String outputPath) {
		DiagnosticCollector<? super JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticCollector, Locale.ENGLISH, Charset.forName("utf-8"));
		Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjects(new File(sourceFilePath));
		File outputFolder = new File(outputPath);
		if (!outputFolder.exists()) {
			outputFolder.mkdir();
		}
		String[] options = new String[] { "--enable-preview", "--release", "19", "-d", outputFolder.getAbsolutePath(), "-g", "-proc:none" };
		final StringWriter output = new StringWriter();
		CompilationTask task = compiler.getTask(output, fileManager, diagnosticCollector, Arrays.asList(options), null, javaFileObjects);
		boolean result = task.call();
		if (!result) {
			throw new IllegalArgumentException("Compilation failed:\n" + output);
		}

	}

	@Override
	protected String getMainClassName() {
		return "TryVirtualThread";
	}

	/**
	 * Test JDI isVirtual()
	 */
	public void testJDIVirtualThread() {
		assertTrue("1", ((ThreadReferenceImpl) fVirtualThread).isVirtual());
		assertFalse("2", ((ThreadReferenceImpl) fMainThread).isVirtual());
	}

	/**
	 * Test JDI ThreadStartEvent/ThreadDeathEvent
	 */
	public void testJDIThreadEvents() {
		// Make sure the entire VM is not suspended before we start a new thread
		// (otherwise this new thread will start suspended and we will never get the
		// ThreadStart event)
		fVM.resume();

		// Trigger a thread start event
		ThreadStartEvent fThreadStartEvent = (ThreadStartEvent) triggerAndWait(fVM.eventRequestManager().createThreadStartRequest(), "ThreadStartEvent", true);
		assertNotNull("1", fThreadStartEvent);
		assertEquals("2", "java.lang.VirtualThread", fThreadStartEvent.thread().type().name());

		// Trigger a thread death event
		ThreadDeathEvent fThreadDeathEvent = (ThreadDeathEvent) triggerAndWait(fVM.eventRequestManager().createThreadDeathRequest(), "ThreadDeathEvent", true);
		assertNotNull("3", fThreadDeathEvent);
		assertEquals("4", "java.lang.VirtualThread", fThreadDeathEvent.thread().type().name());
	}

	/**
	 * Test restricting JDI ThreadStartEvent/ThreadDeathEvent to platform threads only
	 */
	public void testJDIPlatformThreadsOnlyFilter() {
		// Make sure the entire VM is not suspended before we start a new thread
		// (otherwise this new thread will start suspended and we will never get the
		// ThreadStart event)
		fVM.resume();

		// Trigger a thread start event
		ThreadStartRequest vThreadStartRequest = fVM.eventRequestManager().createThreadStartRequest();
		try {
			Method method = vThreadStartRequest.getClass().getMethod("addPlatformThreadsOnlyFilter");
			method.invoke(vThreadStartRequest);
		} catch (NoSuchMethodException | SecurityException e) {
			fail("1");
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			fail("2");
		}
		ThreadStartEvent startEvent = (ThreadStartEvent) triggerAndWait(vThreadStartRequest, "ThreadStartEvent", true, 3000);
		assertNull("3", startEvent);

		// Trigger a thread death event
		ThreadDeathRequest vThreadDeathRequest = fVM.eventRequestManager().createThreadDeathRequest();
		try {
			Method method = vThreadDeathRequest.getClass().getMethod("addPlatformThreadsOnlyFilter");
			method.invoke(vThreadDeathRequest);
		} catch (NoSuchMethodException | SecurityException e) {
			fail("4");
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			fail("5");
		}
		ThreadDeathEvent deathEvent = (ThreadDeathEvent) triggerAndWait(vThreadDeathRequest, "ThreadDeathEvent", true, 3000);
		assertNull("6", deathEvent);
	}
}
