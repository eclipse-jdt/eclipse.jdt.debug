/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.List;

import org.eclipse.debug.internal.ui.AbstractDebugCheckboxSelectionDialog;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog for selecting projects to add to a classpath or source
 * lookup path. Optionally specifies whether
 * exported entries and required projects should also be added.
 */
public class ProjectSelectionDialog extends AbstractDebugCheckboxSelectionDialog {
	
	private boolean fAddExportedEntries = true;
	private boolean fAddRequiredProjects = true;
	
	private List fProjects;

	/**
	 * @see ListSelectionDialog
	 */
	public ProjectSelectionDialog(Shell parentShell, List projects){
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setShowSelectAllButtons(true);
		fProjects = projects;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugCheckboxSelectionDialog#addCustomFooterControls(org.eclipse.swt.widgets.Composite)
	 */
	protected void addCustomFooterControls(Composite parent) {
		super.addCustomFooterControls(parent);
		final Button addExported = new Button(parent, SWT.CHECK);
		addExported.setText(ActionMessages.ProjectSelectionDialog_Add_exported_entries_of_selected_projects__1); 
		addExported.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fAddExportedEntries = addExported.getSelection();
			}
		});
		addExported.setSelection(fAddExportedEntries);
		addExported.setFont(parent.getFont());
		
		final Button addRequired = new Button(parent, SWT.CHECK);
		addRequired.setText(ActionMessages.ProjectSelectionDialog_Add_required_projects_of_selected_projects__2); 
		addRequired.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fAddRequiredProjects = addRequired.getSelection();
			}
		});
		addRequired.setSelection(fAddRequiredProjects);
		addRequired.setFont(parent.getFont());		
	}
	
	/**
	 * Returns whether the user has selected to add exported entries.
	 * 
	 * @return whether the user has selected to add exported entries
	 */
	public boolean isAddExportedEntries() {
		return fAddExportedEntries;
	}
	
	/**
	 * Returns whether the user has selected to add required projects.
	 * 
	 * @return whether the user has selected to add required projects
	 */
	public boolean isAddRequiredProjects() {
		return fAddRequiredProjects;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getDialogSettingsId()
	 */
	protected String getDialogSettingsId() {
		return IJavaDebugUIConstants.PLUGIN_ID + ".PROJECT_SELECTION_DIALOG_SECTION"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IJavaDebugHelpContextIds.SELECT_PROJECT_DIALOG;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerInput()
	 */
	protected Object getViewerInput() {
		return fProjects;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerLabel()
	 */
	protected String getViewerLabel() {
		return ActionMessages.ProjectSelectionDialog_0;
	}
}
