/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.ibm.icu.text.MessageFormat;

/**
 * Adds an external jar to the runtime class path.
 */
public class AddExternalJarAction extends OpenDialogAction {

	public AddExternalJarAction(IClasspathViewer viewer, String dialogSettingsPrefix) {
		super(ActionMessages.AddExternalJar_Add_E_xternal_JARs_1, viewer, dialogSettingsPrefix); 
	}	

	/**
	 * Prompts for a project to add.
	 * 
	 * @see IAction#run()
	 */	
	public void run() {
							
		String lastUsedPath = getDialogSetting(LAST_PATH_SETTING);
		if (lastUsedPath == null) {
			lastUsedPath = ""; //$NON-NLS-1$
		}
		FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
		dialog.setText(ActionMessages.AddExternalJar_Jar_Selection_3); 
		dialog.setFilterExtensions(new String[] {"*.jar;*.zip","*.*"}); //$NON-NLS-1$ //$NON-NLS-2$
		dialog.setFilterPath(lastUsedPath);
		String res = dialog.open();
		if (res == null) {
			return;
		}
		String[] fileNames = dialog.getFileNames();
		int nChosen = fileNames.length;
			
		IPath filterPath = new Path(dialog.getFilterPath());
		ArrayList list = new ArrayList();
		IPath path = null;
		for (int i= 0; i < nChosen; i++) {
			path = filterPath.append(fileNames[i]).makeAbsolute();	
			if(path.toFile().exists()) {
				list.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
			}
			else {
				MessageDialog.openError(getShell(), ActionMessages.AddExternalJarAction_error_box_title, MessageFormat.format(ActionMessages.AddExternalJarAction_error_box_message, new String[] {path.makeAbsolute().toOSString()}));
			}
		}
		if(list.size() > 0) {
			setDialogSetting(LAST_PATH_SETTING, filterPath.toOSString());
			getViewer().addEntries((IRuntimeClasspathEntry[]) list.toArray(new IRuntimeClasspathEntry[list.size()]));
		}
	}	
}
