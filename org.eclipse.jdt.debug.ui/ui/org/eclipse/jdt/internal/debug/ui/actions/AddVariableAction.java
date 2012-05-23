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


import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;

/**
 * Adds a variable to the runtime class path.
 */
public class AddVariableAction extends RuntimeClasspathAction {

	public AddVariableAction(IClasspathViewer viewer) {
		super(ActionMessages.AddVariableAction_Add_Variables_1, viewer); 
	}	

	/**
	 * Prompts for variables to add.
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */	
	@Override
	public void run() {
		
		IPath[] paths = BuildPathDialogAccess.chooseVariableEntries(getShell(), new IPath[0]);
		if (paths != null) {			
			IRuntimeClasspathEntry[] entries = new IRuntimeClasspathEntry[paths.length];
			for (int i = 0; i < paths.length; i++) {
				entries[i] = JavaRuntime.newVariableRuntimeClasspathEntry(paths[i]);
			}
			getViewer().addEntries(entries);
		}				
	}
	
	@Override
	protected int getActionType() {
		return ADD;
	}
}
