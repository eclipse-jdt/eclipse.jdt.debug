package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathViewer;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.NewVariableEntryDialog;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Adds a variable to the runtime class path.
 */
public class AddVariableAction extends RuntimeClasspathAction {

	public AddVariableAction(RuntimeClasspathViewer viewer) {
		super(ActionMessages.getString("AddVariableAction.Add_Variables_1"), viewer); //$NON-NLS-1$
	}	

	/**
	 * Prompts for variables to add.
	 * 
	 * @see IAction#run()
	 */	
	public void run() {
		
		NewVariableEntryDialog dialog = new NewVariableEntryDialog(getShell(), ActionMessages.getString("AddVariableAction.Variable_Selection_1"), null); //$NON-NLS-1$
		
		if (dialog.open() == NewVariableEntryDialog.OK) {			
			IPath[] paths = dialog.getResult();
			IRuntimeClasspathEntry[] entries = new IRuntimeClasspathEntry[paths.length];
			for (int i = 0; i < paths.length; i++) {
				entries[i] = JavaRuntime.newVariableRuntimeClasspathEntry(paths[i]);
			}
			getViewer().addEntries(entries);
		}				
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return getViewer().isEnabled();
	}
	
}
