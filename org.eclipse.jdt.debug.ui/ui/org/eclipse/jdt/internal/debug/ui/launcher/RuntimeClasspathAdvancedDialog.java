package org.eclipse.jdt.internal.debug.ui.launcher;

/*******************************************************************************
 * Copyright (c) 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog of radio buttons/actions for advanced classpath options.
 */
public class RuntimeClasspathAdvancedDialog extends TitleAreaDialog {
	
	private IAction[] fActions;
	private Button[] fButtons;	

	/**
	 * Constructs a new dialog on the given shell, with the specified
	 * set of actions.
	 * 
	 * @param parentShell
	 * @param actions advanced actions
	 */
	public RuntimeClasspathAdvancedDialog(Shell parentShell, IAction[] actions) {
		super(parentShell);
		fActions = actions;
	}

	/**
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);
		
		Composite composite= (Composite) super.createDialogArea(parent);
		Composite inner= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		inner.setLayout(layout);
		
		Label l = new org.eclipse.swt.widgets.Label(inner, SWT.NONE);
		
		fButtons = new Button[fActions.length];
		for (int i = 0; i < fActions.length; i++) {
			fButtons[i] = new Button(inner, SWT.RADIO);
			fButtons[i].setText(fActions[i].getText());
			fButtons[i].setData(fActions[i]);
			fButtons[i].setEnabled(fActions[i].isEnabled());
		}
		
		setTitle(LauncherMessages.getString("RuntimeClasspathAdvancedDialog.Add_Classpath_Entry_1")); //$NON-NLS-1$
		setMessage(LauncherMessages.getString("RuntimeClasspathAdvancedDialog.Select_the_type_of_entry_to_add_2")); //$NON-NLS-1$
		return composite;

	}

	/**
	 * @see Dialog#okPressed()
	 */
	protected void okPressed() {
		for (int i = 0; i < fButtons.length; i++) {
			if (fButtons[i].getSelection()) {
				IAction action = (IAction)fButtons[i].getData();
				action.run();
				break;
			}
		}
		super.okPressed();
	}

}
