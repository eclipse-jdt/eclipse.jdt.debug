package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Prompts the user to re-launch when a working directory
 * is not supported by the Eclipse runtime.
 */
public class WorkingDirectoryStatusHandler implements IStatusHandler {

	/**
	 * @see IStatusHandler#handleStatus(IStatus, Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		final boolean[] result = new boolean[1];
		JDIDebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
			public void run() {
				String title= LauncherMessages.getString("WorkingDirectoryStatusHandler.Java_Application_1"); //$NON-NLS-1$
				String message= LauncherMessages.getString("JDKDebugLauncher.Setting_a_working_directory"); //$NON-NLS-1$
				result[0]= (MessageDialog.openQuestion(JDIDebugUIPlugin.getActiveWorkbenchShell(), title, message));
			}
		});
		return new Boolean(result[0]);
	}

}
