/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
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
					DebugUIMessages.NoLineNumberAttributesStatusHandler_Java_Breakpoint_1, 
					NLS.bind(DebugUIMessages.NoLineNumberAttributesStatusHandler_2, new String[] {type.name()}), 
					status, IJDIPreferencesConstants.PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT,
					DebugUIMessages.NoLineNumberAttributesStatusHandler_3, 
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
