/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;

/**
 * Adds a library to the runtime class path.
 */
public class AddLibraryAction extends RuntimeClasspathAction {

	public AddLibraryAction(IClasspathViewer viewer) {
		super(ActionMessages.AddLibraryAction_0, viewer); 
	}	

	/**
	 * Prompts for folder(s) to add.
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */	
	@Override
	public void run() {

		IClasspathEntry[] newEntries = BuildPathDialogAccess.chooseContainerEntries(getShell(), null, new IClasspathEntry[0]);
		if (newEntries != null) {
			IRuntimeClasspathEntry[] res= new IRuntimeClasspathEntry[newEntries.length];
			for (int i = 0; i < newEntries.length; i++) {
				IClasspathEntry entry = newEntries[i];
				try {
					res[i] = JavaRuntime.newRuntimeContainerClasspathEntry(entry.getPath(), IRuntimeClasspathEntry.STANDARD_CLASSES);
				} catch (CoreException e) {
					JDIDebugUIPlugin.statusDialog(LauncherMessages.RuntimeClasspathAdvancedDialog_Unable_to_create_new_entry__3, e.getStatus()); 
					return;
				}
			}
			getViewer().addEntries(res);
		}								
	}
		
	@Override
	protected int getActionType() {
		return ADD;
	}
}
