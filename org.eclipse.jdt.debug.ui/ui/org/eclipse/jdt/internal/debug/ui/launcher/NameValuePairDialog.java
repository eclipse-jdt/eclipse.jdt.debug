/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NameValuePairDialog extends Dialog {

	private String fName;
	private String fValue;

	private String fTitle;
	private String[] fFieldLabels;
	private String[] fInitialValues;
	
	private Label fNameLabel;
	private Text fNameText;
	private Label fValueLabel;
	private Text fValueText;

	public NameValuePairDialog(Shell shell, String title, String[] fieldLabels, String[] initialValues) {
		super(shell);
		fTitle = title;
		fFieldLabels = fieldLabels;
		fInitialValues = initialValues;
	}

	/**
	 * @see Dialog#createDialogArea(Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = (Composite) super.createDialogArea(parent);
		((GridLayout)comp.getLayout()).numColumns=2;
		
		fNameLabel = new Label(comp, SWT.NONE);
		fNameLabel.setText(fFieldLabels[0]);
		fNameLabel.setFont(font);
		
		ModifyListener listener= new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtons();
			}
		};
		
		fNameText = new Text(comp, SWT.BORDER | SWT.SINGLE);
		fNameText.setText(fInitialValues[0]);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 300;
		fNameText.setLayoutData(gd);
		fNameText.setFont(font);
		fNameText.addModifyListener(listener);
		
		fValueLabel = new Label(comp, SWT.NONE);
		fValueLabel.setText(fFieldLabels[1]);
		fValueLabel.setFont(font);
		
		fValueText = new Text(comp, SWT.BORDER | SWT.SINGLE);
		fValueText.setText(fInitialValues[1]);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 300;
		fValueText.setLayoutData(gd);
		fValueText.setFont(font);
		fValueText.addModifyListener(listener);		
		
		applyDialogFont(comp);
		return comp;
	}
	
	/**
	 * Return the name/value pair entered in this dialog.  If the cancel button was hit,
	 * both will be <code>null</code>.
	 */
	public String[] getNameValuePair() {
		return new String[] {fName, fValue};
	}
	
	/**
	 * @see Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			fName= fNameText.getText().trim();
			fValue = fValueText.getText().trim();
		} else {
			fName = null;
			fValue = null;
		}
		super.buttonPressed(buttonId);
	}
	
	/**
	 * @see Window#configureShell(Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (fTitle != null) {
			shell.setText(fTitle);
		}
	}
	
	/**
	 * Enable the OK button if valid input
	 */
	protected void updateButtons() {
		String name = fNameText.getText().trim();
		String value = fValueText.getText().trim();
		getButton(IDialogConstants.OK_ID).setEnabled((name.length() > 0) &&(value.length() > 0));
	}
	
	/**
	 * Enable the buttons on creation.
	 */
	@Override
	public void create() {
		super.create();
		updateButtons();
	}
}
