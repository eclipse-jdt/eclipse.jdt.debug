/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CreateStepFilterDialog extends StatusDialog {

	private static final String DEFAULT_NEW_FILTER_TEXT = ""; //$NON-NLS-1$
	
	private Text text;
	private Filter filter;
	private Button okButton;

	private boolean filterValid;
	private boolean okClicked;
	private Filter[] existingFilters;

	private CreateStepFilterDialog(Shell parent, Filter filter, Filter[] existingFilters) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.filter = filter;
		this.existingFilters = existingFilters;
		
		setTitle(DebugUIMessages.getString("CreateStepFilterDialog.2")); //$NON-NLS-1$
		setStatusLineAboveButtons(false);
		
	}
	
	static Filter showCreateStepFilterDialog(Shell parent, Filter[] existingFilters) {
		CreateStepFilterDialog createStepFilterDialog = new CreateStepFilterDialog(parent, new Filter(DEFAULT_NEW_FILTER_TEXT, true), existingFilters);
		createStepFilterDialog.create();
		createStepFilterDialog.open();
		
		return createStepFilterDialog.filter;		
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		okButton= createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);		
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite)super.createDialogArea(parent);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 15;
		gridLayout.marginWidth = 15;
		container.setLayout(gridLayout);

		int textStyles = SWT.SINGLE | SWT.LEFT;
		Label label= new Label(container, textStyles);
		label.setText(DebugUIMessages.getString("CreateStepFilterDialog.3")); //$NON-NLS-1$
		label.setFont(container.getFont());
		
		// create & configure Text widget for editor
		// Fix for bug 1766.  Border behavior on for text fields varies per platform.
		// On Motif, you always get a border, on other platforms,
		// you don't.  Specifying a border on Motif results in the characters
		// getting pushed down so that only there very tops are visible.  Thus,
		// we have to specify different style constants for the different platforms.
		if (!SWT.getPlatform().equals("motif")) {  //$NON-NLS-1$
			textStyles |= SWT.BORDER;
		}
		
		text = new Text(container, textStyles);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);		
		gridData.horizontalSpan=1;
		gridData.widthHint = 300;
		text.setLayoutData(gridData);
		text.setFont(container.getFont());
		
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateChange();
				if (!filterValid) 
					updateStatus(new StatusInfo(IStatus.ERROR, DebugUIMessages.getString("CreateStepFilterDialog.4"))); //$NON-NLS-1$
				else if (isDuplicateFilter(text.getText().trim())) {
					updateStatus(new StatusInfo(IStatus.WARNING, DebugUIMessages.getString("CreateStepFilterDialog.5"))); //$NON-NLS-1$
					return;
				} else 
					updateStatus(new StatusInfo());		
			}
		});
	
		return container;
	}
	
	private void validateChange() {
		String trimmedValue = text.getText().trim();

		if (trimmedValue.length()>0 && validateInput(trimmedValue)) {
			okButton.setEnabled(true);
			filter.setName(text.getText());
			filterValid = true;
		} else {
			okButton.setEnabled(false);
			filter.setName(DEFAULT_NEW_FILTER_TEXT);
			filterValid = false;
		}
	}
	
	private boolean isDuplicateFilter(String trimmedValue) {
		for (int i=0; i<existingFilters.length; i++)
			if(existingFilters[i].getName().equals(trimmedValue))
				return true;
		return false;
	}
	/**
	 * A valid step filter is simply one that is a valid Java identifier.
	 * and, as defined in the JDI spec, the regular expressions used for
	 * step filtering must be limited to exact matches or patterns that
	 * begin with '*' or end with '*'. Beyond this, a string cannot be validated
	 * as corresponding to an existing type or package (and this is probably not
	 * even desirable).  
	 */
	private boolean validateInput(String trimmedValue) {
		char firstChar= trimmedValue.charAt(0);
		if (!Character.isJavaIdentifierStart(firstChar)) {
			if (!(firstChar == '*')) {
				return false;
			}
		}
		int length= trimmedValue.length();
		for (int i= 1; i < length; i++) {
			char c= trimmedValue.charAt(i);
			if (!Character.isJavaIdentifierPart(c)) {
				if (c == '.' && i != (length - 1)) {
					continue;
				}
				if (c == '*' && i == (length - 1)) {
					continue;
				}
				return false;
			}
		}
		return true;
	}	

	
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(getDialogSettingsSectionName());
		if (section == null) {
			section = settings.addNewSection(getDialogSettingsSectionName());
		} 
		return section;
	}
	
	/**
	 * Returns the name of the section that this dialog stores its settings in
	 * 
	 * @return String
	 */
	protected String getDialogSettingsSectionName() {
		return IDebugUIConstants.PLUGIN_ID + ".CREATE_STEP_FILTER_DIALOG_SECTION"; //$NON-NLS-1$
	}
	
	private void persistShellGeometry() {
		Point shellLocation = getShell().getLocation();
		Point shellSize = getShell().getSize();
		IDialogSettings settings = getDialogSettings();
		settings.put(IDebugPreferenceConstants.DIALOG_ORIGIN_X, shellLocation.x);
		settings.put(IDebugPreferenceConstants.DIALOG_ORIGIN_Y, shellLocation.y);
		settings.put(IDebugPreferenceConstants.DIALOG_WIDTH, shellSize.x);
		settings.put(IDebugPreferenceConstants.DIALOG_HEIGHT, shellSize.y);
	}	


	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
	 */
	protected Point getInitialLocation(Point initialSize) {
		IDialogSettings settings = getDialogSettings();
		try {
			int x, y;
			x = settings.getInt(IDebugPreferenceConstants.DIALOG_ORIGIN_X);
			y = settings.getInt(IDebugPreferenceConstants.DIALOG_ORIGIN_Y);
			return new Point(x,y);
		} catch (NumberFormatException e) {
		}
		return super.getInitialLocation(initialSize);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		
		IDialogSettings settings = getDialogSettings();
		try {
			int x, y;
			x = settings.getInt(IDebugPreferenceConstants.DIALOG_WIDTH);
			y = settings.getInt(IDebugPreferenceConstants.DIALOG_HEIGHT);
			return new Point(Math.max(x,size.x),Math.max(y,size.y));
		} catch (NumberFormatException e) {
		}
		return size;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		if (!okClicked) {
			filterValid = false;
			filter = null;
		}
		persistShellGeometry();
		return super.close();
	}

	protected void okPressed() {
		okClicked = true;
		super.okPressed();
	}
}