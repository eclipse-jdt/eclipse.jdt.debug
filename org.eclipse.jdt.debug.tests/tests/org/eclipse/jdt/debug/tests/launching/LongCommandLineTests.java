/*******************************************************************************
 * Copyright (c) 2022 Andrey Loskutov (loskutov@gmx.de) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.launching;

import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test JVM startup with long classpath / system properties by explicitly enabling {@link IJavaLaunchConfigurationConstants.ATTR_USE_ARGFILE} option
 * in the "Arguments" part of the process launch configuration
 */
public class LongCommandLineTests extends LongClassPathTests {

	static final int MIN_CLASSPATH_LENGTH = 300000;
	boolean createLongArguments;

	public LongCommandLineTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new LongCommandLineTests("testVeryLongClasspathWithArgumentFile"));
		suite.addTest(new LongCommandLineTests("testVeryLongSystemPropertiesWithArgumentFile"));
		return suite;
	}

	/*
	 * Test will create lot of "-Dfoo=12345" system arguments for the started JVM and check if the startup works with argument file
	 */
	public void testVeryLongSystemPropertiesWithArgumentFile() throws Exception {
		createLongArguments = true;

		javaProject = createJavaProjectClone("test ä VeryLongSystemPropertiesWithArgumentFile", CLASSPATH_PROJECT_CONTENT_PATH.toString(), JavaProjectHelper.JAVA_SE_9_EE_NAME, true);
		launchConfiguration = createLaunchConfigurationStopInMain(javaProject, MAIN_TYPE_NAME);
		assumeTrue(isArgumentFileSupported(launchConfiguration));

		// Given
		waitForBuild();

		// When
		thread = launchAndSuspend(launchConfiguration);

		// Then
		File tempFile = getTempFile(thread.getLaunch()).orElseThrow(() -> new RuntimeException("No temp file"));
		assertTrue("No temp file created: " + tempFile, tempFile.exists());
		assertTrue("Unexpected temp file name: " + tempFile, tempFile.getName().endsWith(".txt"));
		String valueString = doEval(thread, "java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString()").getValueString();
		assertTrue("Unexpected small system arguments list: " + valueString, valueString.length() >= MIN_CLASSPATH_LENGTH);

		// When
		resumeAndExit(thread);

		// Then
		if (!Platform.getOS().equals(Platform.OS_WIN32)) {
			// On windows, temp file deletion may fail
			assertFalse(tempFile.exists());
		}
	}

	private void setLongVmArgumentsList(ILaunchConfigurationWorkingCopy wc, int minArgumentsLength) throws Exception {
		final String keyArgs = IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS;
		String args = wc.getAttribute(keyArgs, "");
		StringBuilder sb = new StringBuilder(args);
		while (sb.length() < minArgumentsLength) {
			sb.append(" ");
			sb.append("-Dfoo=1234567890_1234567890_1234567890_1234567890_1234567890_1234567890_1234567890_1234567890");
		}
		args += sb.toString();
		wc.setAttribute(keyArgs, args);
	}

	@Override
	protected ILaunchConfiguration createLaunchConfigurationStopInMain(IJavaProject javaProject, String mainTypeName) throws Exception, CoreException {
		ILaunchConfiguration launchConfiguration;
		launchConfiguration = createLaunchConfiguration(javaProject, mainTypeName);
		ILaunchConfigurationWorkingCopy wc = launchConfiguration.getWorkingCopy();
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
		// Always use argfile option
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_ARGFILE, true);
		// Only set if required by particular test
		if (createLongArguments) {
			setLongVmArgumentsList(wc, MIN_CLASSPATH_LENGTH);
		}
		launchConfiguration = wc.doSave();
		return launchConfiguration;
	}

}
