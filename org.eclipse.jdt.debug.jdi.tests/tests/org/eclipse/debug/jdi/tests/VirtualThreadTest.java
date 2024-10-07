/*******************************************************************************
 * Copyright (c) 2022, 2024 Microsoft Corporation and others.
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdi.internal.ThreadReferenceImpl;
import org.osgi.framework.Bundle;

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
	private static Path tempClassesDirectory;

	/**
	 * Init the fields that are used by this test only.
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
		try {
			compileTestProgram();
		} catch (Exception e) {
			StringWriter err = new StringWriter();
			err.append(e.getMessage());
			e.printStackTrace(new PrintWriter(err));
			fail("Error in setup: " + err.toString());
		}
		super.setUp();
	}

	@Override
	public void setVMInfo(VMInformation info) {
		// do nothing
	}

	protected static void compileTestProgram() throws Exception {
		if (Runtime.version().feature() < 19) {
			return;
		}
		tempClassesDirectory = Files.createTempDirectory("VirtualThreadTest");
		tempClassesDirectory.toFile().deleteOnExit();
		String outputFilePath = tempClassesDirectory.toString();

		Bundle bundle = Platform.getBundle("org.eclipse.jdt.debug.jdi.tests");
		URL bundleUrl = FileLocator.find(bundle, IPath.fromOSString("java19/TryVirtualThread.java"), null);
		URL fileURL = FileLocator.toFileURL(bundleUrl);
		String sourceFilePath = fileURL.getFile();
		compileFiles(sourceFilePath, outputFilePath);
	}

	@Override
	protected String enhanceClasspath(String classPath) {
		return classPath + File.pathSeparator + tempClassesDirectory;
	}

	private static void compileFiles(String sourceFilePath, String outputPath) throws Exception {
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.ENGLISH, StandardCharsets.UTF_8);
		Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjects(new File(sourceFilePath));
		File outputFolder = new File(outputPath);
		if (!outputFolder.exists()) {
			Files.createDirectories(outputFolder.toPath());
		}
		List<String> options = List.of("-d", outputFolder.getAbsolutePath(), "-g", "-proc:none");
		final StringWriter output = new StringWriter();
		CompilationTask task = compiler.getTask(output, fileManager, null, options, null, javaFileObjects);
		boolean result = task.call();
		if (!result) {
			throw new IllegalArgumentException("Compilation failed:\n'" + output + "', compiler name: '" + compiler.name()
					+ "', supported source versions: " + compiler.getSourceVersions());
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
	 *
	 * @throws Exception
	 */
	public void testJDIPlatformThreadsOnlyFilter() throws Exception {
		// Make sure the entire VM is not suspended before we start a new thread
		// (otherwise this new thread will start suspended and we will never get the
		// ThreadStart event)
		fVM.resume();

		// Trigger a thread start event
		ThreadStartRequest vThreadStartRequest = fVM.eventRequestManager().createThreadStartRequest();
		Method method = vThreadStartRequest.getClass().getMethod("addPlatformThreadsOnlyFilter");
		method.invoke(vThreadStartRequest);
		ThreadStartEvent startEvent = (ThreadStartEvent) triggerAndWait(vThreadStartRequest, "ThreadStartEvent", true, 3000);
		assertNull("3", startEvent);

		// Trigger a thread death event
		ThreadDeathRequest vThreadDeathRequest = fVM.eventRequestManager().createThreadDeathRequest();
		method = vThreadDeathRequest.getClass().getMethod("addPlatformThreadsOnlyFilter");
		method.invoke(vThreadDeathRequest);
		ThreadDeathEvent deathEvent = (ThreadDeathEvent) triggerAndWait(vThreadDeathRequest, "ThreadDeathEvent", true, 3000);
		assertNull("6", deathEvent);
	}
}
