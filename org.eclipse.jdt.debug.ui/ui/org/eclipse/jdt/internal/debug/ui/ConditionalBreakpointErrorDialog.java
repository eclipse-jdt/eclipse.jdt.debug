package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class ConditionalBreakpointErrorDialog extends ErrorDialog {
	
	public ConditionalBreakpointErrorDialog(Shell parentShell, String message, IStatus status) {
		super(parentShell, DebugUIMessages.getString("ConditionalBreakpointErrorDialog.Conditional_Breakpoint_Error_1"), message, status, IStatus.ERROR); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create Edit and Cancel buttons
		createButton(parent, IDialogConstants.OK_ID, DebugUIMessages.getString("ConditionalBreakpointErrorDialog.&Edit_Condition_2"), true); //$NON-NLS-1$
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

}
