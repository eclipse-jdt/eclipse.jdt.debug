/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDetailFormattersPreferencePage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * 
 */
public class DetailOptionsDialog extends Dialog {

    private Button fInlineAllButton;
    private Button fInlineFormattersButton;
	// preference prefix	
	private String fPrefix;
    
    /**
     * @param parentShell
     */
    protected DetailOptionsDialog(Shell parentShell, String prefix) {
        super(parentShell);
        fPrefix= prefix;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		WorkbenchHelp.setHelp(
			parent,
			IJavaDebugHelpContextIds.DETAIL_DISPLAY_OPTIONS_DIALOG);		
		
		getShell().setText(ActionMessages.getString("DetailOptionsDialog.0")); //$NON-NLS-1$
		Composite composite = (Composite)super.createDialogArea(parent);
		
		Label label= new Label(composite, SWT.NONE);
		label.setText(ActionMessages.getString("DetailOptionsDialog.1")); //$NON-NLS-1$
		
		String preference= AbstractDisplayOptionsAction.getStringPreferenceValue(fPrefix, IJDIPreferencesConstants.PREF_SHOW_DETAILS);
		
		// Create the 3 detail option radio buttons
		fInlineFormattersButton = new Button(composite, SWT.RADIO);
		fInlineFormattersButton.setText(ActionMessages.getString("DetailOptionsDialog.2")); //$NON-NLS-1$
		fInlineFormattersButton.setSelection(preference.equals(IJDIPreferencesConstants.INLINE_FORMATTERS));
		
		fInlineAllButton = new Button(composite, SWT.RADIO);
		fInlineAllButton.setText(ActionMessages.getString("DetailOptionsDialog.3")); //$NON-NLS-1$
		fInlineAllButton.setSelection(preference.equals(IJDIPreferencesConstants.INLINE_ALL));
		
		Button detailPane = new Button(composite, SWT.RADIO);
		detailPane.setText(ActionMessages.getString("DetailOptionsDialog.4")); //$NON-NLS-1$
		detailPane.setSelection(preference.equals(IJDIPreferencesConstants.DETAIL_PANE));
		
		Button editFormatters= new Button(composite, SWT.PUSH);
		editFormatters.setText(ActionMessages.getString("DetailOptionsDialog.5")); //$NON-NLS-1$
		editFormatters.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
        		IPreferencePage page = new JavaDetailFormattersPreferencePage();
        		showPreferencePage("org.eclipse.jdt.debug.ui.JavaDetailFormattersPreferencePage", page); //$NON-NLS-1$
            }
        });
		
		applyDialogFont(composite);
		return composite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		String value= IJDIPreferencesConstants.DETAIL_PANE;
		if (fInlineAllButton.getSelection()) {
		    value= IJDIPreferencesConstants.INLINE_ALL;
		} else if (fInlineFormattersButton.getSelection()) {
		    value= IJDIPreferencesConstants.INLINE_FORMATTERS;
		}
		store.setValue(fPrefix + "." + IJDIPreferencesConstants.PREF_SHOW_DETAILS, value); //$NON-NLS-1$
		super.okPressed();
	}
	
	protected void showPreferencePage(String id, IPreferencePage page) {
		final IPreferenceNode targetNode = new PreferenceNode(id, page);
		
		PreferenceManager manager = new PreferenceManager();
		manager.addToRoot(targetNode);
		final PreferenceDialog dialog = new PreferenceDialog(DebugUIPlugin.getShell(), manager);
		final boolean [] result = new boolean[] { false };
		BusyIndicator.showWhile(DebugUIPlugin.getStandardDisplay(), new Runnable() {
			public void run() {
				dialog.create();
				dialog.setMessage(targetNode.getLabelText());
				result[0]= (dialog.open() == Window.OK);
			}
		});		
	}	

}
