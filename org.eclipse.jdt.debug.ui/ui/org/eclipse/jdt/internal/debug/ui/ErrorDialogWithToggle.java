package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import org.eclipse.core.runtime.IStatus;
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
	private String fPreferenceKey= null;
	/**
	 * The message displayed to the user, with the toggle button
	 */
	private String fToggleMessage= null;
	private Button fToggleButton= null;
	/**
	 * The preference store which will be affected by the toggle button
	 */
	IPreferenceStore fStore= null;

	public ErrorDialogWithToggle(Shell parentShell, String dialogTitle, String message, IStatus status, String preferenceKey, String toggleMessage, IPreferenceStore store) {
		super(parentShell, dialogTitle, message, status, IStatus.WARNING | IStatus.ERROR | IStatus.INFO);
		fStore= store;
		fPreferenceKey= preferenceKey;
		fToggleMessage= toggleMessage;
		setBlockOnOpen(false);
	}

	protected Control createMessageArea(Composite parent) {
		Composite dialogArea= (Composite) super.createMessageArea(parent);
		setToggleButton(createCheckButton(dialogArea, fToggleMessage));
		getToggleButton().setSelection(fStore.getBoolean(fPreferenceKey));
		return dialogArea;
	}
	
	/**
	 * Creates a button with the given label and sets the default 
	 * configuration data.
	 */
	private Button createCheckButton(Composite parent, String label) {
		Button button= new Button(parent, SWT.CHECK | SWT.LEFT);
		button.setText(label);		

		// FieldEditor GridData
		GridData data = new GridData(SWT.NONE);
		data.horizontalSpan= 2;
		data.horizontalAlignment= GridData.CENTER;
		button.setLayoutData(data);
		
		return button;
	}

	protected void buttonPressed(int id) {
		if (id == IDialogConstants.OK_ID) {  // was the OK button pressed?
			storePreference();
		}
		super.buttonPressed(id);
	}
	
	private void storePreference() {
		fStore.setValue(fPreferenceKey, getToggleButton().getSelection());
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
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setFocus();
	}
}
