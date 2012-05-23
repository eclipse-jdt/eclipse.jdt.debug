/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;

public class AutoFormatSettingAction extends Action {
	private JavaStackTraceConsolePage fPage;
	private IPreferenceStore fPreferenceStore;

	public AutoFormatSettingAction(JavaStackTraceConsolePage page) {
		super(ConsoleMessages.AutoFormatSettingAction_0, SWT.TOGGLE); 
		fPage = page;
		
		setToolTipText(ConsoleMessages.AutoFormatSettingAction_1);  
		setImageDescriptor(JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_ELCL_AUTO_FORMAT));
		setHoverImageDescriptor(JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_ELCL_AUTO_FORMAT));
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaDebugHelpContextIds.CONSOLE_AUTOFORMAT_STACKTRACES_ACTION);
        
        fPreferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
        boolean checked = fPreferenceStore.getBoolean(IJDIPreferencesConstants.PREF_AUTO_FORMAT_JSTCONSOLE);
		setChecked(checked);
	}

	@Override
	public void run() {
		boolean checked = isChecked();
		JavaStackTraceConsoleViewer viewer = (JavaStackTraceConsoleViewer) fPage.getViewer();
		viewer.setAutoFormat(checked);
		fPreferenceStore.setValue(IJDIPreferencesConstants.PREF_AUTO_FORMAT_JSTCONSOLE, checked);
	}
}
