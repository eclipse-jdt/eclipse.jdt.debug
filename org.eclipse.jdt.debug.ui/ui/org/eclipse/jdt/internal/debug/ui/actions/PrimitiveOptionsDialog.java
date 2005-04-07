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
import org.eclipse.ui.PlatformUI;

/**
 * Dialog to alter primitive display options for a specific view
 */
public class PrimitiveOptionsDialog extends Dialog {

	private Button fHexButton;
	private Button fCharButton;
	private Button fUnsignedButton;
	// preference prefix	
	private String fPrefix;
	
	public PrimitiveOptionsDialog(Shell parentShell, String prefix) {
		super(parentShell);
		fPrefix = prefix;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(
			parent,
			IJavaDebugHelpContextIds.PRIMITIVE_DISPLAY_OPTIONS_DIALOG);		
		
		getShell().setText(ActionMessages.PrimitiveOptionsDialog_Primitive_Type_Display_Options_1); //$NON-NLS-1$
		Composite composite = (Composite)super.createDialogArea(parent);
		
		// Create the 3 primitive display checkboxes
		fHexButton = new Button(composite, SWT.CHECK);
		fHexButton.setText(DebugUIMessages.JavaDebugPreferencePage_Display__hexadecimal_values__byte__short__char__int__long__3); //$NON-NLS-1$
		fHexButton.setSelection(getBooleanPreferenceValue(fPrefix, IJDIPreferencesConstants.PREF_SHOW_HEX));
		fCharButton = new Button(composite, SWT.CHECK);
		fCharButton.setText(DebugUIMessages.JavaDebugPreferencePage_Display_ASCII__character_values__byte__short__int__long__4); //$NON-NLS-1$
		fCharButton.setSelection(getBooleanPreferenceValue(fPrefix, IJDIPreferencesConstants.PREF_SHOW_CHAR));
		fUnsignedButton = new Button(composite, SWT.CHECK);
		fUnsignedButton.setText(DebugUIMessages.JavaDebugPreferencePage_Display__unsigned_values__byte__5); //$NON-NLS-1$
		fUnsignedButton.setSelection(getBooleanPreferenceValue(fPrefix, IJDIPreferencesConstants.PREF_SHOW_UNSIGNED));
		applyDialogFont(composite);
		return composite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		store.setValue(fPrefix + "." + IJDIPreferencesConstants.PREF_SHOW_HEX, fHexButton.getSelection()); //$NON-NLS-1$
		store.setValue(fPrefix + "." + IJDIPreferencesConstants.PREF_SHOW_CHAR, fCharButton.getSelection()); //$NON-NLS-1$
		store.setValue(fPrefix + "." + IJDIPreferencesConstants.PREF_SHOW_UNSIGNED, fUnsignedButton.getSelection()); //$NON-NLS-1$
		super.okPressed();
	}
    
    /**
     * Returns the value of this filters preference (on/off) for the given
     * view.
     * 
     * @param part
     * @return boolean
     */
    public static boolean getBooleanPreferenceValue(String id, String preference) {
        String compositeKey = id + "." + preference; //$NON-NLS-1$
        IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
        boolean value = false;
        if (store.contains(compositeKey)) {
            value = store.getBoolean(compositeKey);
        } else {
            value = store.getBoolean(preference);
        }
        return value;       
    }
}
