package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Adds a project to the runtime class path.
 */
public class AddProjectAction extends RuntimeClasspathAction {

	public AddProjectAction(RuntimeClasspathViewer viewer) {
		super(ActionMessages.getString("AddProjectAction.Add_Project_1"), viewer); //$NON-NLS-1$
	}	

	/**
	 * Prompts for a project to add.
	 * 
	 * @see IAction#run()
	 */	
	public void run() {
		List projects = getPossibleAdditions();
		
		ILabelProvider labelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(ActionMessages.getString("AddProjectAction.Project_Selection_2")); //$NON-NLS-1$
		dialog.setMessage(ActionMessages.getString("AddProjectAction.Choose_&project(s)_to_add__3")); //$NON-NLS-1$
		dialog.setElements(projects.toArray());
		dialog.setMultipleSelection(true);
		
		if (dialog.open() == dialog.OK) {			
			Object[] selections = dialog.getResult();
			IRuntimeClasspathEntry[] entries = new IRuntimeClasspathEntry[selections.length];
			for (int i = 0; i < selections.length; i++) {
				entries[i] = JavaRuntime.newProjectRuntimeClasspathEntry((IJavaProject)selections[i]);
			}
			getViewer().addEntries(entries);
		}				
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return getViewer().isEnabled() && !getPossibleAdditions().isEmpty();
	}
	
	/**
	 * Returns the possible projects that can be added
	 */
	protected List getPossibleAdditions() {
		IJavaProject[] projects;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			projects= JavaCore.create(root).getJavaProjects();
		} catch (JavaModelException e) {
			JDIDebugUIPlugin.log(e);
			projects= new IJavaProject[0];
		}
		List remaining = new ArrayList();
		for (int i = 0; i < projects.length; i++) {
			remaining.add(projects[i]);
		}
		List alreadySelected = new ArrayList();
		IRuntimeClasspathEntry[] entries = getViewer().getEntries();
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getType() == IRuntimeClasspathEntry.PROJECT) {
				IResource res = root.findMember(entries[i].getPath());
				IJavaProject jp = (IJavaProject)JavaCore.create(res);
				alreadySelected.add(jp);
			}
		}
		remaining.removeAll(alreadySelected);
		return remaining;		
	}

}
