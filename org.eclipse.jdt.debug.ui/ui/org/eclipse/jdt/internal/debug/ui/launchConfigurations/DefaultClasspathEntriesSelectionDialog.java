/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui.launchConfigurations;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class DefaultClasspathEntriesSelectionDialog extends ElementListSelectionDialog {

	private boolean addAsUnit= true;

	public DefaultClasspathEntriesSelectionDialog(Shell parent, ILabelProvider renderer) {
		super(parent, renderer);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setTitle("Default Entries");
		setMultipleSelection(false);
		setMessage("&Select the default classpath entry and how it should be added to the classpath:");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		createButtons((Composite)control);
		return control;
	}

	private void createButtons(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		container.setLayoutData(gd);
		container.setFont(parent.getFont());
		
		
		final Button unit = new Button(container, SWT.RADIO);
		unit.setFont(parent.getFont());
		unit.setText("As a &unit"); 
		unit.setSelection(true);
		unit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addAsUnit= unit.getSelection();
			}
		});
		
		Button individual = new Button(container, SWT.RADIO);
		individual.setFont(parent.getFont());
		individual.setText("As &individual entries"); 
	}
	
	public boolean addAsUnit() {
		return addAsUnit;
	}
}