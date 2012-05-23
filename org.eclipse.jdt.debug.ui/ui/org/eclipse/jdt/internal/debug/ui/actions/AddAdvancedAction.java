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


import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathAdvancedDialog;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;

/**
 * Opens a dialog to allow the user to choose among advanced actions.
 */
public class AddAdvancedAction extends RuntimeClasspathAction {
	
	private IAction[] fActions;

	public AddAdvancedAction(IClasspathViewer viewer, IAction[] actions) {
		super(ActionMessages.AddAdvancedAction_Ad_vanced____1, viewer); 
		fActions = actions;
		setViewer(viewer);
	}	

	/**
	 * Prompts for a project to add.
	 * 
	 * @see IAction#run()
	 */	
	@Override
	public void run() {
		Dialog dialog = new RuntimeClasspathAdvancedDialog(getShell(), fActions, getViewer());
		dialog.open();			
	}
		
	/**
	 * @see RuntimeClasspathAction#setViewer(RuntimeClasspathViewer)
	 */
	@Override
	public void setViewer(IClasspathViewer viewer) {
		super.setViewer(viewer);
		if (fActions != null) {
			for (int i = 0; i < fActions.length; i++) {
				if (fActions[i] instanceof RuntimeClasspathAction) {
					((RuntimeClasspathAction)fActions[i]).setViewer(viewer);
				}
			}
		}
	}
	
	@Override
	protected int getActionType() {
		return ADD;
	}
}
