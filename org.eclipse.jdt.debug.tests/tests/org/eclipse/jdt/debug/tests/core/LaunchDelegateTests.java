/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
}
