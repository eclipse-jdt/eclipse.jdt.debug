package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;

/**
 * A dialog for selecting projects to add to a classpath or source
 * lookup path. Optionally specifies whether
 * exported entries and required projects should also be added.
 */
public class ProjectSelectionDialog extends ListSelectionDialog {
	
	private boolean fAddExportedEntries = true;
	private boolean fAddRequiredProjects = true;

	/**
	 * @see ListSelectionDialog
	 */
	public ProjectSelectionDialog(
		Shell parentShell,
		Object input,
		IStructuredContentProvider contentProvider,
		ILabelProvider labelProvider,
		String message) {
		super(parentShell, input, contentProvider, labelProvider, message);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		
		final Button addExported = new Button(composite, SWT.CHECK);
		addExported.setText(ActionMessages.getString("ProjectSelectionDialog.Add_exported_entries_of_selected_projects._1")); //$NON-NLS-1$
		addExported.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fAddExportedEntries = addExported.getSelection();
			}
		});
		addExported.setSelection(fAddExportedEntries);
		
		final Button addRequired = new Button(composite, SWT.CHECK);
		addRequired.setText(ActionMessages.getString("ProjectSelectionDialog.Add_required_projects_of_selected_projects._2")); //$NON-NLS-1$
		addRequired.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fAddRequiredProjects = addRequired.getSelection();
			}
		});
		addRequired.setSelection(fAddRequiredProjects);		
		
		return composite;
	}
	
	/**
	 * Returns whether the user has selected to add exported entries.
	 * 	 * @return whether the user has selected to add exported entries	 */
	public boolean isAddExportedEntries() {
		return fAddExportedEntries;
	}
	
	/**
	 * Returns whether the user has selected to add required projects.
	 * 	 * @return whether the user has selected to add required projects	 */
	public boolean isAddRequiredProjects() {
		return fAddRequiredProjects;
	}

}
