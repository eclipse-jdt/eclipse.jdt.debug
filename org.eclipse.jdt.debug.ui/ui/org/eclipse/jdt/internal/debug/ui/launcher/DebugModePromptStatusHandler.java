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
package org.eclipse.jdt.internal.debug.ui.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.AlwaysNeverDialog;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;


public class DebugModePromptStatusHandler implements IStatusHandler {
	
	private static final String PREFERENCE_KEY = "org.eclipse.jdt.internal.debug.ui.launcher.DebugModePromptStatusHandler.PROMPT_USER"; //$NON-NLS-1$
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		Shell activeShell = JDIDebugUIPlugin.getActiveWorkbenchShell();
		String title = LauncherMessages.getString("DebugModePromptStatusHandler.1"); //$NON-NLS-1$
		String message = LauncherMessages.getString("DebugModePromptStatusHandler.2"); //$NON-NLS-1$
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		
		ILaunchConfiguration configuration = (ILaunchConfiguration)source;
		
		String pref = store.getString(PREFERENCE_KEY); 
		if (pref != null) {
			if (pref.equals(AlwaysNeverDialog.NEVER)) {
				return new Boolean(false);
			} else if (pref.equals(AlwaysNeverDialog.ALWAYS)) { 
				relaunchInDebugMode(configuration);
				return new Boolean(true);
			}
		}
		
		boolean switchToDebug = AlwaysNeverDialog.openQuestion(activeShell, title, message, PREFERENCE_KEY, store); 
		if (switchToDebug) {
			relaunchInDebugMode(configuration);
		}
		return new Boolean(switchToDebug);
	}
	/**
	 * @param configuration
	 */
	private void relaunchInDebugMode(ILaunchConfiguration configuration) {
		DebugUITools.launch(configuration, ILaunchManager.DEBUG_MODE);
	}
}
