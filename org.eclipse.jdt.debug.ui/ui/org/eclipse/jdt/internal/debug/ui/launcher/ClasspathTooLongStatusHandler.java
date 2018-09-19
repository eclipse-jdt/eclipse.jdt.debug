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
 *     Cedric Chabanois (cchabanois@gmail.com) - Launching command line exceeds the process creation command limit on *nix - https://bugs.eclipse.org/bugs/show_bug.cgi?id=385738
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Handle status when classpath is too long and that this could not be solved automatically.
 *
 * We ask the user if he wants to use classpath-only jar. We need confirmation from the user because it can have side effects
 * (System.getProperty("java.class.path") will return a classpath with only one jar).
 */
public class ClasspathTooLongStatusHandler implements IStatusHandler {

	@Override
	public Object handleStatus(IStatus status, Object source) {
		ILaunch launch = (ILaunch) source;
		ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
		boolean enableClasspathOnlyJar = askEnableClasspathOnlyJar();
		if (enableClasspathOnlyJar && launchConfiguration != null) {
			try {
				ILaunchConfigurationWorkingCopy configurationWorkingCopy = launchConfiguration.getWorkingCopy();
				configurationWorkingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_CLASSPATH_ONLY_JAR, true);
				configurationWorkingCopy.doSave();
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		return Boolean.valueOf(enableClasspathOnlyJar);
	}

	private boolean askEnableClasspathOnlyJar() {
		final boolean[] result = new boolean[1];
		JDIDebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				String title = LauncherMessages.ClasspathTooLongStatusHandler_0;
				String message = LauncherMessages.ClasspathTooLongStatusHandler_1;
				result[0] = (MessageDialog.openQuestion(JDIDebugUIPlugin.getActiveWorkbenchShell(), title, message));
			}
		});
		return result[0];
	}

}
