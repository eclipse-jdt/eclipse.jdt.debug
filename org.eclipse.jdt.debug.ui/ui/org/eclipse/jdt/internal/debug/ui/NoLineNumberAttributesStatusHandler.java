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
package org.eclipse.jdt.internal.debug.ui;


import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.sun.jdi.ReferenceType;

public class NoLineNumberAttributesStatusHandler implements IStatusHandler {

	/**
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(IStatus, Object)
	 */
	public Object handleStatus(IStatus status, Object source) {
		ReferenceType type= (ReferenceType) source;
		IPreferenceStore preferenceStore= JDIDebugUIPlugin.getDefault().getPreferenceStore();
		if (preferenceStore.getBoolean(IJDIPreferencesConstants.PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT)) {
			final ErrorDialogWithToggle dialog= new ErrorDialogWithToggle(JDIDebugUIPlugin.getActiveWorkbenchShell(),
					DebugUIMessages.getString("NoLineNumberAttributesStatusHandler.Java_Breakpoint_1"), //$NON-NLS-1$
					MessageFormat.format(DebugUIMessages.getString("NoLineNumberAttributesStatusHandler.Attempting_to_install_a_breakpoint_in_the_type_{0}_that_has_no_line_number_attributes.__The_breakpoint_cannot_be_installed.__Class_files_must_be_generated_with_the_line_number_attributes._2"), new String[] {type.name()}), //$NON-NLS-1$
					status, IJDIPreferencesConstants.PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT,
					DebugUIMessages.getString("NoLineNumberAttributesStatusHandler.2"), //$NON-NLS-1$
					preferenceStore);
			Display display= JDIDebugUIPlugin.getStandardDisplay();
			display.syncExec(new Runnable() {
				public void run() {
					dialog.open();
				}
			});
		}
		return null;
	}

}
