package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathAdvancedDialog;
import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Opens a dialog to allow the user to choose among advanced actions.
 */
public class AddAdvancedAction extends RuntimeClasspathAction {
	
	private IAction[] fActions;

	public AddAdvancedAction(RuntimeClasspathViewer viewer, IAction[] actions) {
		super(ActionMessages.getString("AddAdvancedAction.Ad&vanced..._1"), viewer); //$NON-NLS-1$
		fActions = actions;
	}	

	/**
	 * Prompts for a project to add.
	 * 
	 * @see IAction#run()
	 */	
	public void run() {
		Dialog dialog = new RuntimeClasspathAdvancedDialog(getShell(), fActions, getViewer());
		dialog.open();			
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return getViewer().isEnabled();
	}
		
	/**
	 * @see RuntimeClasspathAction#setViewer(RuntimeClasspathViewer)
	 */
	public void setViewer(RuntimeClasspathViewer viewer) {
		super.setViewer(viewer);
		if (fActions != null) {
			for (int i = 0; i < fActions.length; i++) {
				if (fActions[i] instanceof RuntimeClasspathAction) {
					((RuntimeClasspathAction)fActions[i]).setViewer(viewer);
				}
			}
		}
	}

}
