package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class DebugErrorDialog extends ErrorDialog {

	/**
	 * The preference key which determines whether this dialog is shown again.
	 * This key must be a valid preference in the JDIDebugUIPlugin preference
	 * store.
	 */
	private String fShowAgainKey= null;
	/**
	 * The message displayed to the user, asking if this dialog should continue
	 * to be shown.
	 */
	private String fShowAgainMessage= null;
	private Button fShowAgain= null;

	public DebugErrorDialog(Shell parentShell, String dialogTitle, String message, IStatus status, int displayMask, String showAgainKey, String showAgainMessage) {
		super(parentShell, dialogTitle, message, status, displayMask);
		fShowAgainKey= showAgainKey;
		fShowAgainMessage= showAgainMessage;
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea= (Composite) super.createDialogArea(parent);
		setShowHCRButton(createCheckButton(dialogArea, fShowAgainMessage)); //$NON-NLS-1$
		getShowHCRButton().setSelection(JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(fShowAgainKey));
		
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
		JDIDebugUIPlugin.getDefault().getPreferenceStore().setValue(fShowAgainKey, getShowHCRButton().getSelection());
	}

	protected Button getShowHCRButton() {
		return fShowAgain;
	}

	protected void setShowHCRButton(Button showHCR) {
		fShowAgain = showHCR;
	}
}
