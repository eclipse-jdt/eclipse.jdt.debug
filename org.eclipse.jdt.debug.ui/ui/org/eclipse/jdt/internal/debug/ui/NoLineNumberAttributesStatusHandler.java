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
package org.eclipse.jdt.internal.debug.ui;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.sun.jdi.ReferenceType;

public class NoLineNumberAttributesStatusHandler implements IStatusHandler {

	/**
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(IStatus, Object)
	 */
	@Override
	public Object handleStatus(IStatus status, Object source) {
		ReferenceType type= (ReferenceType) source;
		IPreferenceStore preferenceStore= JDIDebugUIPlugin.getDefault().getPreferenceStore();
		if (preferenceStore.getBoolean(IJDIPreferencesConstants.PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT)) {
			Display display= JDIDebugUIPlugin.getStandardDisplay();
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					final ErrorDialogWithToggle dialog = new ErrorDialogWithToggle(PlatformUI.getWorkbench().getModalDialogShellProvider().getShell(),
							DebugUIMessages.NoLineNumberAttributesStatusHandler_Java_Breakpoint_1,
							NLS.bind(DebugUIMessages.NoLineNumberAttributesStatusHandler_2, type.name()),
							status, IJDIPreferencesConstants.PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT,
							DebugUIMessages.NoLineNumberAttributesStatusHandler_3,
							preferenceStore);
					dialog.open();
				}
			});
		}
		return null;
	}

}
