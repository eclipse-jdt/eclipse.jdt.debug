/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.classpath;

import org.eclipse.jdt.internal.launching.DefaultProjectClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Edits project default classapath.
 * 
 * @since 3.2
 */
public class DefaultClasspathEntryDialog extends MessageDialog {
	
	private DefaultProjectClasspathEntry fEntry;
	private Button fButton;

	public DefaultClasspathEntryDialog(Shell parentShell, IRuntimeClasspathEntry entry) {
		super(parentShell, ClasspathMessages.DefaultClasspathEntryDialog_0, null,
				NLS.bind(ClasspathMessages.DefaultClasspathEntryDialog_1, new String[]{entry.getJavaProject().getElementName()}),
				MessageDialog.NONE, new String[]{ClasspathMessages.DefaultClasspathEntryDialog_2, ClasspathMessages.DefaultClasspathEntryDialog_3}, 0);
		fEntry = (DefaultProjectClasspathEntry) entry;
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		fButton = new Button(parent, SWT.CHECK);
		fButton.setText(ClasspathMessages.DefaultClasspathEntryDialog_4);
		fButton.setSelection(fEntry.isExportedEntriesOnly());
		return fButton;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 0) {
			fEntry.setExportedEntriesOnly(fButton.getSelection());
		}
		super.buttonPressed(buttonId);
	}
	


}
