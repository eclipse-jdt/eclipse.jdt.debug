package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jdt.internal.debug.ui.DebugUIMessages;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Dialog to alter primitive display options for a specific view
 */
public class PrimitiveOptionsDialog extends Dialog {

	private Button fHexButton;
	private Button fCharButton;
	private Button fUnsignedButton;
	// preference prefix	
	private String fPrefix;
	
	/**
	 * @param parentShell
	 */
	public PrimitiveOptionsDialog(Shell parentShell, String prefix) {
		super(parentShell);
		fPrefix = prefix;
	}
	
	/**
	 * @see Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		WorkbenchHelp.setHelp(
			parent,
			IJavaDebugHelpContextIds.PRIMITIVE_DISPLAY_OPTIONS_DIALOG);		
		
		getShell().setText(ActionMessages.getString("PrimitiveOptionsDialog.Primitive_Type_Display_Options_1")); //$NON-NLS-1$
		Composite composite = (Composite)super.createDialogArea(parent);
		
		// Create the 3 primitive display checkboxes
		fHexButton = new Button(composite, SWT.CHECK);
		fHexButton.setText(DebugUIMessages.getString("JavaDebugPreferencePage.Display_&hexadecimal_values_(byte,_short,_char,_int,_long)_3")); //$NON-NLS-1$
		fHexButton.setSelection(PrimitiveOptionsAction.getPreferenceValue(fPrefix, IJDIPreferencesConstants.PREF_SHOW_HEX));
		fCharButton = new Button(composite, SWT.CHECK);
		fCharButton.setText(DebugUIMessages.getString("JavaDebugPreferencePage.Display_ASCII_&character_values_(byte,_short,_int,_long)_4")); //$NON-NLS-1$
		fCharButton.setSelection(PrimitiveOptionsAction.getPreferenceValue(fPrefix, IJDIPreferencesConstants.PREF_SHOW_CHAR));
		fUnsignedButton = new Button(composite, SWT.CHECK);
		fUnsignedButton.setText(DebugUIMessages.getString("JavaDebugPreferencePage.Display_&unsigned_values_(byte)_5")); //$NON-NLS-1$
		fUnsignedButton.setSelection(PrimitiveOptionsAction.getPreferenceValue(fPrefix, IJDIPreferencesConstants.PREF_SHOW_UNSIGNED));
		return composite;
	}
	
	/**
	 * @see Dialog#okPressed()
	 */
	protected void okPressed() {
		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		store.setValue(fPrefix + "." + IJDIPreferencesConstants.PREF_SHOW_HEX, fHexButton.getSelection()); //$NON-NLS-1$
		store.setValue(fPrefix + "." + IJDIPreferencesConstants.PREF_SHOW_CHAR, fCharButton.getSelection()); //$NON-NLS-1$
		store.setValue(fPrefix + "." + IJDIPreferencesConstants.PREF_SHOW_UNSIGNED, fUnsignedButton.getSelection()); //$NON-NLS-1$
		super.okPressed();
	}	
}
