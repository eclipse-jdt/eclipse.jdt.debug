/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * Overrides the dependencies specified in the Java Build path.
 */
public class OverrideDependenciesAction extends RuntimeClasspathAction {

	private JavaClasspathTab fTab;

	/**
	 * Constructor
	 * @param viewer the associated classpath viewer
	 * @param tab the tab the viewer resides in
	 */
	public OverrideDependenciesAction(IClasspathViewer viewer, JavaClasspathTab tab) {
		super(ActionMessages.Override_Dependencies_button1, viewer);
		fTab = tab;
	}

	/**
	 * View and override dependencies.
	 *
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		OverrideDependenciesDialog dialog = new OverrideDependenciesDialog(getShell(), ActionMessages.Override_Dependencies_title, null, null, 0, new String[] {
				ActionMessages.Override_Dependencies_button, IDialogConstants.CANCEL_LABEL }, 0, fTab.getLaunchConfiguration());
		int returnValue = dialog.open();
		if (returnValue == 0) {
			getViewer().notifyChanged();
		}
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return true;
	}
}
