/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests for launch delegates
 */
public class LaunchDelegateTests extends AbstractDebugTest {
	
	public LaunchDelegateTests(String name) {
		super(name);
	}
	
	/**
	 * Ensures a launch delegate can provide a launch object for
	 * a launch.
	 */
	public void testProvideLaunch() throws CoreException {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType configurationType = manager.getLaunchConfigurationType("org.eclipse.jdt.debug.tests.testConfigType");
		assertNotNull("Missing test launch config type", configurationType);
		ILaunchConfigurationWorkingCopy workingCopy = configurationType.newInstance(null, "provide-launch-object");
		// delegate will throw exception if test fails
		ILaunch launch = workingCopy.launch(ILaunchManager.DEBUG_MODE, null);
		manager.removeLaunch(launch);
	}
	
	/**
	 * Tests that a delegate extension can provide the source path computer.
	 */
	public void testSourcePathComputerExtension() {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType configurationType = manager.getLaunchConfigurationType("org.eclipse.jdt.debug.tests.testConfigType");
		assertNotNull("Missing test launch config type", configurationType);
		ISourcePathComputer sourcePathComputer = configurationType.getSourcePathComputer();
		assertEquals("Wrond source path computer", "org.eclipse.jdt.debug.tests.testSourcePathComputer", sourcePathComputer.getId());
	}
	
	/**
	 * Tests that a delegate extension can provide the source locator.
	 */
	public void testSourceLocatorExtension() {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType configurationType = manager.getLaunchConfigurationType("org.eclipse.jdt.debug.tests.testConfigType");
		assertNotNull("Missing test launch config type", configurationType);
		String sourceLocatorId = configurationType.getSourceLocatorId();
		assertEquals("Wrond source locater id", "org.eclipse.jdt.debug.tests.testSourceLocator", sourceLocatorId);
	}	
}
