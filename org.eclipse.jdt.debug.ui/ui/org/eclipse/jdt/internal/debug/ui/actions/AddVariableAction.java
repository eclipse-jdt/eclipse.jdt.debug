package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathEntryLabelProvider;
import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

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
		IRuntimeClasspathEntry[] vars = getPossibleAdditions();
		
		ILabelProvider labelProvider= new RuntimeClasspathEntryLabelProvider();
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(ActionMessages.getString("AddVariableAction.Variable_Selection_2")); //$NON-NLS-1$
		dialog.setMessage(ActionMessages.getString("AddVariableAction.Choose_&variables_to_add__3")); //$NON-NLS-1$
		dialog.setElements(vars);
		
		if (dialog.open() == dialog.OK) {			
			Object[] selections = dialog.getResult();
			IRuntimeClasspathEntry[] entries = new IRuntimeClasspathEntry[selections.length];
			for (int i = 0; i < selections.length; i++) {
				entries[i] = (IRuntimeClasspathEntry)selections[i];
			}
			getViewer().addEntries(entries);
		}				
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return getViewer().isEnabled() && getPossibleAdditions().length > 0;
	}
	
	/**
	 * Returns the possible variables that can be added.
	 */
	protected IRuntimeClasspathEntry[] getPossibleAdditions() {
		String[] allNames = JavaCore.getClasspathVariableNames();		
		List remaining = new ArrayList();
		for (int i = 0; i < allNames.length; i++) {
			remaining.add(allNames[i]);
		}
		List alreadySelected = new ArrayList();
		IRuntimeClasspathEntry[] entries = getViewer().getEntries();
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getType() == IRuntimeClasspathEntry.VARIABLE) {
				alreadySelected.add(entries[i].getVariableName());
			}
		}
		remaining.removeAll(alreadySelected);
		IRuntimeClasspathEntry[] rtes = new IRuntimeClasspathEntry[remaining.size()];
		for (int i = 0; i < rtes.length; i++) {
			rtes[i] = JavaRuntime.newVariableRuntimeClasspathEntry((String)remaining.get(i));
		}
		return rtes;
	}

}
