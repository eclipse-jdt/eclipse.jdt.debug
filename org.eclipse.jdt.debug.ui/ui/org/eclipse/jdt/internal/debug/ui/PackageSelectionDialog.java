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

package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/*
 * Same as ElementListSelectionDialog, but persists size/location
 */
public class PackageSelectionDialog extends ElementListSelectionDialog {

	public PackageSelectionDialog(Shell parent, ILabelProvider renderer) {
		super(parent, renderer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		persistShellGeometry();
		return super.close();
	}
	
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = JDIDebugUIPlugin.getDefault().getDialogSettings();
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
		return IJavaDebugUIConstants.PLUGIN_ID + ".PACKAGE_SELECTION_DIALOG_SECTION"; //$NON-NLS-1$
	}
	
	
	
	protected void persistShellGeometry() {
		Point shellLocation = getShell().getLocation();
		Point shellSize = getShell().getSize();
		IDialogSettings settings = getDialogSettings();
		settings.put(IJDIPreferencesConstants.DIALOG_ORIGIN_X, shellLocation.x);
		settings.put(IJDIPreferencesConstants.DIALOG_ORIGIN_Y, shellLocation.y);
		settings.put(IJDIPreferencesConstants.DIALOG_WIDTH, shellSize.x);
		settings.put(IJDIPreferencesConstants.DIALOG_HEIGHT, shellSize.y);
	}	


	/**
	 * @see Window#getInitialLocation(Point)
	 */
	protected Point getInitialLocation(Point initialSize) {
		IDialogSettings settings = getDialogSettings();
		try {
			int x, y;
			x = settings.getInt(IJDIPreferencesConstants.DIALOG_ORIGIN_X);
			y = settings.getInt(IJDIPreferencesConstants.DIALOG_ORIGIN_Y);
			return new Point(x,y);
		} catch (NumberFormatException e) {
		}
		return super.getInitialLocation(initialSize);
	}

	/**
	 * @see Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		
		IDialogSettings settings = getDialogSettings();
		try {
			int x, y;
			x = settings.getInt(IJDIPreferencesConstants.DIALOG_WIDTH);
			y = settings.getInt(IJDIPreferencesConstants.DIALOG_HEIGHT);
			return new Point(Math.max(x,size.x),Math.max(y,size.y));
		} catch (NumberFormatException e) {
		}
		return size;
	}
}
