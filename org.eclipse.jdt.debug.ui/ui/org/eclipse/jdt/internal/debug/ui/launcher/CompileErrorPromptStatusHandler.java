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
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.AlwaysNeverDialog;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;


public class CompileErrorPromptStatusHandler implements IStatusHandler {
	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		Shell shell = JDIDebugUIPlugin.getActiveWorkbenchShell();
		String title = LauncherMessages.getString("CompileErrorPromptStatusHandler.1"); //$NON-NLS-1$
		String message = LauncherMessages.getString("CompileErrorPromptStatusHandler.2"); //$NON-NLS-1$
		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore(); 
		
		String pref = store.getString(IJDIPreferencesConstants.PREF_CONTINUE_WITH_COMPILE_ERROR);
		if (pref != null) {
			if (pref.equals(AlwaysNeverDialog.ALWAYS)) {
				return new Boolean(true);
			} else if (pref.equals(AlwaysNeverDialog.NEVER)) {
				return new Boolean(false);
			}
		}
		
		boolean answer = AlwaysNeverDialog.openQuestion(shell, title, message, IJDIPreferencesConstants.PREF_CONTINUE_WITH_COMPILE_ERROR, store);
		return new Boolean(answer);
	}

	
}
