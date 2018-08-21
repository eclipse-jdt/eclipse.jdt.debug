/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.launcher;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Prompts the user to continue waiting for a connection
 * from a debuggable VM.
 */
public class VMConnectTimeoutStatusHandler implements IStatusHandler {

	/**
	 * @see IStatusHandler#handleStatus(IStatus, Object)
	 */
	@Override
	public Object handleStatus(IStatus status, Object source) {
		final boolean[] result = new boolean[1];
		JDIDebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				String title= LauncherMessages.VMConnectTimeoutStatusHandler_Java_Application_1;
				String message= LauncherMessages.jdkLauncher_error_timeout;
				result[0]= (MessageDialog.openQuestion(JDIDebugUIPlugin.getActiveWorkbenchShell(), title, message));
			}
		});
		return Boolean.valueOf(result[0]);
	}
}
