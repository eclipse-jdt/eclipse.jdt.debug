/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * An error dialog which allows the user to set
 * a boolean preference.
 *
 * This is typically used to set a preference that
 * determines if the dialog should be shown in
 * the future
 */
public class ErrorDialogWithToggle extends ErrorDialog {

	/**
	 * The preference key which is set by the toggle button.
	 * This key must be a boolean preference in the preference store.
	 */
	private String fPreferenceKey;
	/**
	 * The message displayed to the user, with the toggle button
	 */
	private String fToggleMessage1;
	private Button fToggleButton;
	/**
	 * Optional message displayed to the user only applicable for HCR Failure, with the toggle button
	 */
	private String fToggleMessage2;

	private Button fToggleButton2;
	/**
	 * The preference store which will be affected by the toggle button
	 */
	IPreferenceStore fStore= null;

	public ErrorDialogWithToggle(Shell parentShell, String dialogTitle, String message, IStatus status, String preferenceKey, String toggleMessage1, String toggleMessage2, IPreferenceStore store) {
		super(parentShell, dialogTitle, message, status, IStatus.WARNING | IStatus.ERROR | IStatus.INFO);
		fStore = store;
		fPreferenceKey = preferenceKey;
		fToggleMessage1 = toggleMessage1;
		fToggleMessage2 = toggleMessage2;
	}
	public ErrorDialogWithToggle(Shell parentShell, String dialogTitle, String message, IStatus status, String preferenceKey, String toggleMessage, IPreferenceStore store) {
		super(parentShell, dialogTitle, message, status, IStatus.WARNING | IStatus.ERROR | IStatus.INFO);
		fStore= store;
		fPreferenceKey= preferenceKey;
		fToggleMessage1= toggleMessage;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogComposite= (Composite) super.createDialogArea(parent);
		dialogComposite.setFont(parent.getFont());
		setToggleButton(createCheckButton(dialogComposite, fToggleMessage1));
		if (fToggleMessage2 != null) {
			fToggleButton2 = createCheckButton(dialogComposite, fToggleMessage2);
		}
		getToggleButton().setSelection(!fStore.getBoolean(fPreferenceKey));
		applyDialogFont(dialogComposite);
		return dialogComposite;
	}

	/**
	 * Creates a button with the given label and sets the default
	 * configuration data.
	 */
	private Button createCheckButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.CHECK | SWT.LEFT);
		button.setText(label);
		GridData data = new GridData(SWT.NONE);
		data.horizontalIndent = 40;
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.BEGINNING;
		button.setLayoutData(data);
		button.setFont(parent.getFont());
		return button;
	}

	protected void buttonPressed(int id, IDebugTarget target) {
		if (id == IDialogConstants.OK_ID) {  // was the OK button pressed?
			storePreference(target);
		}
		super.buttonPressed(id);
	}

	@Override
	protected void buttonPressed(int id) {
		buttonPressed(id, null);
	}

	private void storePreference(IDebugTarget target) {
		fStore.setValue(fPreferenceKey, !getToggleButton().getSelection());
		if (fToggleButton2 != null) {
			if (target instanceof JDIDebugTarget jdiTarget) {
				jdiTarget.setHcrDebugErrorPref(fToggleButton2.getSelection());
			}
		}
	}

	protected Button getToggleButton() {
		return fToggleButton;
	}

	protected void setToggleButton(Button button) {
		fToggleButton = button;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setFocus();
	}
}
