/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.DialogSettingsHelper;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
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
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Font font = parent.getFont();
		
		Composite composite = (Composite)super.createDialogArea(parent);
		
		final Button addExported = new Button(composite, SWT.CHECK);
		addExported.setText(ActionMessages.ProjectSelectionDialog_Add_exported_entries_of_selected_projects__1); //$NON-NLS-1$
		addExported.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fAddExportedEntries = addExported.getSelection();
			}
		});
		addExported.setSelection(fAddExportedEntries);
		addExported.setFont(font);
		
		final Button addRequired = new Button(composite, SWT.CHECK);
		addRequired.setText(ActionMessages.ProjectSelectionDialog_Add_required_projects_of_selected_projects__2); //$NON-NLS-1$
		addRequired.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fAddRequiredProjects = addRequired.getSelection();
			}
		});
		addRequired.setSelection(fAddRequiredProjects);
		addRequired.setFont(font);		
		
		applyDialogFont(composite);
		return composite;
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
	
	/**
	 * Returns the name of the section that this dialog stores its settings in
	 * 
	 * @return String
	 */
	protected String getDialogSettingsSectionName() {
		return IJavaDebugUIConstants.PLUGIN_ID + ".P	ROJECT_SELECTION_DIALOG_SECTION"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
	 */
	protected Point getInitialLocation(Point initialSize) {
		Point initialLocation= DialogSettingsHelper.getInitialLocation(getDialogSettingsSectionName());
		if (initialLocation != null) {
			return initialLocation;
		}
		return super.getInitialLocation(initialSize);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		return DialogSettingsHelper.getInitialSize(getDialogSettingsSectionName(), size);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		DialogSettingsHelper.persistShellGeometry(getShell(), getDialogSettingsSectionName());
		return super.close();
	}

}
