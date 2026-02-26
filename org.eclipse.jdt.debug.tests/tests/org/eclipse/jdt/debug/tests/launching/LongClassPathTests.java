/*******************************************************************************
 * Copyright (c) 2018 Cedric Chabanois and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Cedric Chabanois (cchabanois@gmail.com) - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.launching;

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.DebugElementKindEventWaiter;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test long classpaths. OSs have limits in term of command line length or argument length. We handle this limit differently depending on the VM
 * version and OS.
 */
public class LongClassPathTests extends AbstractDebugTest {

	protected static final String MAIN_TYPE_NAME = "test.classpath.Main";
	protected static final IPath CLASSPATH_PROJECT_CONTENT_PATH = new Path("testresources/classpathProject");
	protected IJavaProject javaProject;
	protected ILaunchConfiguration launchConfiguration;
	protected IJavaThread thread;

	private IVMInstall defaultVM1_6;
	private IVMInstall defaultVM9;

	public LongClassPathTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new LongClassPathTests("testVeryLongClasspathWithClasspathOnlyJar"));
		if (JavaProjectHelper.isJava9Compatible()) {
			suite.addTest(new LongClassPathTests("testVeryLongClasspathWithArgumentFile"));
		} else if (Platform.OS.isWindows()) {
			suite.addTest(new LongClassPathTests("testVeryLongClasspathWithEnvironmentVariable"));
		}
		return suite;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		defaultVM1_6 = prepareExecutionEnvironment(JavaProjectHelper.JAVA_SE_1_6_EE_NAME);
		defaultVM9 = prepareExecutionEnvironment(JavaProjectHelper.JAVA_SE_9_EE_NAME);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			if (thread != null) {
				terminateAndRemove(thread);
			}
			if (javaProject != null) {
				javaProject.getProject().delete(true, true, null);
			}
			if (launchConfiguration != null) {
				launchConfiguration.delete();
			}
			setExecutionEnvironment(JavaProjectHelper.JAVA_SE_1_6_EE_NAME, defaultVM1_6);
			setExecutionEnvironment(JavaProjectHelper.JAVA_SE_9_EE_NAME, defaultVM9);
		} catch (CoreException ce) {
			// ignore
		} finally {
			super.tearDown();
		}
	}

	/*
	 * When classpathOnlyJar is enabled, a classpath-only jar is created.
	 */
	public void testVeryLongClasspathWithClasspathOnlyJar() throws Exception {
		if (Platform.OS.isMac()) { // see https://github.com/eclipse-jdt/eclipse.jdt.debug/issues/782
			return;
		}
		// Given
		javaProject = createJavaProjectClone("test ä VeryLongClasspathWithClasspathOnlyJar", CLASSPATH_PROJECT_CONTENT_PATH.toString(), JavaProjectHelper.JAVA_SE_1_6_EE_NAME, true);
		launchConfiguration = createLaunchConfigurationStopInMain(javaProject, MAIN_TYPE_NAME);
		int minClasspathLength = 300000;
		setLongClasspath(javaProject, minClasspathLength);
		launchConfiguration = enableClasspathOnlyJar(launchConfiguration);
		waitForBuild();

		// When
		thread = launchAndSuspend(launchConfiguration);

		// Then
		File tempFile = getTempFile(thread.getLaunch()).orElseThrow(() -> new RuntimeException("No temp file"));
		assertTrue(tempFile.exists());
		assertTrue(tempFile.getName().endsWith(".jar"));
		String actualClasspath = doEval(thread, "System.getProperty(\"java.class.path\")").getValueString();
		assertTrue(actualClasspath.contains(tempFile.getAbsolutePath()));
		assertTrue(actualClasspath.length() < minClasspathLength);

		// When
		resumeAndExit(thread);

		// Then
		if (!Platform.OS.isWindows()) {
			// On windows, temp file deletion may fail
			assertFalse(tempFile.exists());
		}
	}

	/*
	 * When JVM > 9, an argument file for the classpath is created when classpath is too long
	 */
	public void testVeryLongClasspathWithArgumentFile() throws Exception {
		if (Platform.OS.isMac()) { // see https://github.com/eclipse-jdt/eclipse.jdt.debug/issues/782
			return;
		}
		javaProject = createJavaProjectClone("testVeryLongClasspathWithArgumentFile", CLASSPATH_PROJECT_CONTENT_PATH.toString(), JavaProjectHelper.JAVA_SE_9_EE_NAME, true);
		launchConfiguration = createLaunchConfigurationStopInMain(javaProject, MAIN_TYPE_NAME);
		assertTrue(isArgumentFileSupported(launchConfiguration));
		int minClasspathLength = 300000;

		// Given
		setLongClasspath(javaProject, minClasspathLength);
		waitForBuild();

		// When
		thread = launchAndSuspend(launchConfiguration);

		// Then
		File tempFile = getTempFile(thread.getLaunch()).orElseThrow(() -> new RuntimeException("No temp file"));
		assertTrue(tempFile.exists());
		assertTrue(tempFile.getName().endsWith(".txt"));
		assertTrue(doEval(thread, "System.getProperty(\"java.class.path\")").getValueString().length() >= minClasspathLength);

		// When
		resumeAndExit(thread);

		// Then
		if (!Platform.OS.isWindows()) {
			// On windows, temp file deletion may fail
			assertFalse(tempFile.exists());
		}
	}

	/*
	 * On Windows, for JVM < 9, the CLASSPATH env variable is used if classpath is too long
	 */
	public void testVeryLongClasspathWithEnvironmentVariable() throws Exception {
		assumeTrue("Not on Windows", Platform.OS.isWindows());

		// Given
		javaProject = createJavaProjectClone("testVeryLongClasspath", CLASSPATH_PROJECT_CONTENT_PATH.toString(), JavaProjectHelper.JAVA_SE_1_6_EE_NAME, true);
		launchConfiguration = createLaunchConfigurationStopInMain(javaProject, MAIN_TYPE_NAME);
		assertFalse(isArgumentFileSupported(launchConfiguration));
		int minClasspathLength = 300000;
		setLongClasspath(javaProject, minClasspathLength);
		waitForBuild();

		// When
		thread = launchAndSuspend(launchConfiguration);

		// Then
		assertTrue(doEval(thread, "System.getProperty(\"java.class.path\")").getValueString().length() >= minClasspathLength);
		resumeAndExit(thread);
	}

	protected Optional<File> getTempFile(ILaunch launch) {
		IProcess process = launch.getProcesses()[0];
		String tempFile = process.getAttribute(LaunchingPlugin.ATTR_LAUNCH_TEMP_FILES);
		if (tempFile == null) {
			return Optional.empty();
		}
		return Optional.of(new File(tempFile));
	}

	protected boolean isArgumentFileSupported(ILaunchConfiguration launchConfiguration) throws CoreException {
		IVMInstall vmInstall = JavaRuntime.computeVMInstall(launchConfiguration);
		if (vmInstall instanceof AbstractVMInstall) {
			AbstractVMInstall install = (AbstractVMInstall) vmInstall;
			String vmver = install.getJavaVersion();
			if (JavaCore.compareJavaVersions(vmver, JavaCore.VERSION_9) >= 0) {
				return true;
			}
		}
		return false;
	}

	private ILaunchConfiguration enableClasspathOnlyJar(ILaunchConfiguration launchConfiguration) throws CoreException {
		ILaunchConfigurationWorkingCopy configurationWorkingCopy = launchConfiguration.getWorkingCopy();
		configurationWorkingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_CLASSPATH_ONLY_JAR, true);
		return configurationWorkingCopy.doSave();
	}

	protected ILaunchConfiguration createLaunchConfigurationStopInMain(IJavaProject javaProject, String mainTypeName) throws Exception, CoreException {
		ILaunchConfiguration launchConfiguration;
		launchConfiguration = createLaunchConfiguration(javaProject, mainTypeName);
		ILaunchConfigurationWorkingCopy wc = launchConfiguration.getWorkingCopy();
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
		launchConfiguration = wc.doSave();
		return launchConfiguration;
	}

	protected void setLongClasspath(IJavaProject javaProject, int minClassPathLength) throws Exception {
		StringBuilder sb = new StringBuilder();
		List<IClasspathEntry> classpathEntries = new ArrayList<>();
		int i = 0;
		while (sb.length() < minClassPathLength) {
			String jarName = "library" + i + ".jar";
			IPath targetPath = javaProject.getPath().append("lib/" + jarName);
			javaProject.getProject().getFile("lib/classpath.jar").copy(targetPath, IResource.FORCE, new NullProgressMonitor());
			classpathEntries.add(JavaCore.newLibraryEntry(targetPath, null, null));
			if (i != 0) {
				sb.append(File.pathSeparator);
			}
			sb.append(javaProject.getProject().getFile("lib/" + jarName).getLocation().toString());
			i++;
		}
		classpathEntries.add(JavaCore.newLibraryEntry(javaProject.getPath().append("lib/classpath.jar"), null, null));
		sb.append(File.pathSeparator);
		sb.append(javaProject.getProject().getFile("lib/classpath.jar").getLocation().toString());
		classpathEntries.addAll(Arrays.asList(javaProject.getRawClasspath()));
		javaProject.setRawClasspath(classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]), null);
	}

	public static void waitForBuild() {
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
				Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, null);
				TestUtil.waitForJobs("waitForBuild", 100, 105000, ProcessConsole.class);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}

	/**
	 * Increased timeout
	 *
	 * {@inheritDoc}
	 */
	@Override
	protected IJavaThread launchAndSuspend(ILaunchConfiguration config) throws Exception {
		DebugEventWaiter waiter = new DebugElementKindEventWaiter(DebugEvent.SUSPEND, IJavaThread.class);
		waiter.setTimeout(DEFAULT_TIMEOUT * 2);
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());
		Object suspendee = launchAndWait(config, waiter);
		return (IJavaThread) suspendee;
	}
}
