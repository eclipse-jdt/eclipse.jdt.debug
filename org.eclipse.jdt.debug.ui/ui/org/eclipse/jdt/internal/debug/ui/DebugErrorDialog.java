package org.eclipse.jdt.internal.debug.ui;

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

	Button fShowHCR= null;

	public DebugErrorDialog(Shell parentShell, String dialogTitle, String message, IStatus status, int displayMask) {
		super(parentShell, dialogTitle, message, status, displayMask);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea= (Composite) super.createDialogArea(parent);
		fShowHCR= createCheckButton(dialogArea, DebugUIMessages.getString("Always_alert_me_of_hot_code_replace_failure_1")); //$NON-NLS-1$
		fShowHCR.setSelection(JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.ALERT_HCR_FAILED));
		
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
		JDIDebugUIPlugin.getDefault().getPreferenceStore().setValue(IJDIPreferencesConstants.ALERT_HCR_FAILED, fShowHCR.getSelection());
	}

}
