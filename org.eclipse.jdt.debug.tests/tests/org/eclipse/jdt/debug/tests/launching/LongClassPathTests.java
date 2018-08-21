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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
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
 *
 */
public class LongClassPathTests extends AbstractDebugTest {
	private static final String MAIN_TYPE_NAME = "test.classpath.Main";
	private static final IPath CLASSPATH_PROJECT_CONTENT_PATH = new Path("testresources/classpathProject");
	private IJavaProject javaProject;
	private ILaunchConfiguration launchConfiguration;
	private IJavaThread thread;

	public LongClassPathTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new LongClassPathTests("testVeryLongClasspathWithClasspathOnlyJar"));
		if (JavaProjectHelper.isJava9Compatible()) {
			suite.addTest(new LongClassPathTests("testVeryLongClasspathWithArgumentFile"));
		} else if (Platform.getOS().equals(Platform.OS_WIN32)) {
			suite.addTest(new LongClassPathTests("testVeryLongClasspathWithEnvironmentVariable"));
		}
		return suite;
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
		// Given
		javaProject = createJavaProjectClone("testVeryLongClasspathWithClasspathOnlyJar", CLASSPATH_PROJECT_CONTENT_PATH.toString(), JavaProjectHelper.JAVA_SE_1_6_EE_NAME, true);
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
		if (!Platform.getOS().equals(Platform.OS_WIN32)) {
			// On windows, temp file deletion may fail
			assertFalse(tempFile.exists());
		}
	}

	/*
	 * When JVM > 9, an argument file for the classpath is created when classpath is too long
	 */
	public void testVeryLongClasspathWithArgumentFile() throws Exception {
		javaProject = createJavaProjectClone("testVeryLongClasspathWithArgumentFile", CLASSPATH_PROJECT_CONTENT_PATH.toString(), JavaProjectHelper.JAVA_SE_9_EE_NAME, true);
		launchConfiguration = createLaunchConfigurationStopInMain(javaProject, MAIN_TYPE_NAME);
		assumeTrue(isArgumentFileSupported(launchConfiguration));
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
		if (!Platform.getOS().equals(Platform.OS_WIN32)) {
			// On windows, temp file deletion may fail
			assertFalse(tempFile.exists());
		}
	}

	/*
	 * On Windows, for JVM < 9, the CLASSPATH env variable is used if classpath is too long
	 */
	public void testVeryLongClasspathWithEnvironmentVariable() throws Exception {
		assumeThat(Platform.getOS(), equalTo(Platform.OS_WIN32));

		// Given
		javaProject = createJavaProjectClone("testVeryLongClasspath", CLASSPATH_PROJECT_CONTENT_PATH.toString(), JavaProjectHelper.JAVA_SE_1_6_EE_NAME, true);
		launchConfiguration = createLaunchConfigurationStopInMain(javaProject, MAIN_TYPE_NAME);
		assumeFalse(isArgumentFileSupported(launchConfiguration));
		int minClasspathLength = 300000;
		setLongClasspath(javaProject, minClasspathLength);
		waitForBuild();

		// When
		thread = launchAndSuspend(launchConfiguration);

		// Then
		assertTrue(doEval(thread, "System.getProperty(\"java.class.path\")").getValueString().length() >= minClasspathLength);
		resumeAndExit(thread);
	}

	private Optional<File> getTempFile(ILaunch launch) {
		IProcess process = launch.getProcesses()[0];
		String tempFile = process.getAttribute(LaunchingPlugin.ATTR_LAUNCH_TEMP_FILES);
		if (tempFile == null) {
			return Optional.empty();
		}
		return Optional.of(new File(tempFile));
	}

	private boolean isArgumentFileSupported(ILaunchConfiguration launchConfiguration) throws CoreException {
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

	private ILaunchConfiguration createLaunchConfigurationStopInMain(IJavaProject javaProject, String mainTypeName) throws Exception, CoreException {
		ILaunchConfiguration launchConfiguration;
		launchConfiguration = createLaunchConfiguration(javaProject, mainTypeName);
		ILaunchConfigurationWorkingCopy wc = launchConfiguration.getWorkingCopy();
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
		launchConfiguration = wc.doSave();
		return launchConfiguration;
	}

	private void setLongClasspath(IJavaProject javaProject, int minClassPathLength) throws Exception {
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

}
