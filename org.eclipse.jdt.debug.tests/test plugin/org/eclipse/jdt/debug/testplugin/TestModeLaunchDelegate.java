/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.testplugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.debug.tests.core.LaunchModeTests;

/**
 * Test launch delegate contributed for a contributed launch mode = "TEST_MODE".
 * Contributed for local java application launch configs.
 */
public class TestModeLaunchDelegate implements ILaunchConfigurationDelegate {
	
	// the test case to call back when launch is invoked
	private static LaunchModeTests fgTestCase;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(
		ILaunchConfiguration configuration,
		String mode,
		ILaunch launch,
		IProgressMonitor monitor)
		throws CoreException {
			if (fgTestCase == null) {
				throw new CoreException(new Status(IStatus.ERROR, JavaTestPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
				 0, "No test case registered.", null));
			} else {
				fgTestCase.launch(configuration, mode);
			}

	}
	
	public static void setTestCase(LaunchModeTests testCase) {
		fgTestCase = testCase;
	}

}
