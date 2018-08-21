/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.osgi.service.environment.Constants;

public class EnvironmentTests extends AbstractDebugTest {

	public EnvironmentTests(String name) {
		super(name);
	}

	/**
	 * Tests that we resolve environment variables as case insenitive on Windows.
	 */
	public void testWinOSCaseInsensitiveVariable() throws Exception {
		boolean win32= Platform.getOS().equals(Constants.OS_WIN32);
		if (win32) {
			IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
			String path1 = manager.performStringSubstitution("${env_var:pAth}");
			String path2 = manager.performStringSubstitution("${env_var:PaTh}");
			assertEquals("env vars should be case insensitive", path1, path2);
		}
	}

	/**
	 * Test that we can override a variable in a case insensitive way on Windows
	 */
	public void testWinOSCaseInsensitiveOverride() throws Exception {
		boolean win32= Platform.getOS().equals(Constants.OS_WIN32);
		if (win32) {
			ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, "testWinOSCaseInsensitiveOverride");
			Map<String, String> override = new HashMap<>();
			override.put("pAtH", "OVERRIDE");
			workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, override);
			String[] environment = getLaunchManager().getEnvironment(workingCopy);
			assertNotNull("env should be specified", environment);
			boolean found = false;
			for (int i = 0; i < environment.length; i++) {
				String var = environment[i];
				if (var.endsWith("OVERRIDE")) {
					found = true;
					assertTrue("env var should be PATH", var.substring(0,4).equalsIgnoreCase("PATH"));
				}
			}
			assertTrue("env var path not overridden", found);
		}
	}
}
