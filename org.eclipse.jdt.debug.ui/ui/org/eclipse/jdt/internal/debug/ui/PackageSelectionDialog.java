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

package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
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
		DialogSettingsHelper.persistShellGeometry(getShell(), getDialogSettingsSectionName());
		return super.close();
	}
	
	/**
	 * Returns the name of the section that this dialog stores its settings in
	 * 
	 * @return String
	 */
	protected String getDialogSettingsSectionName() {
		return IJavaDebugUIConstants.PLUGIN_ID + ".PACKAGE_SELECTION_DIALOG_SECTION"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
	 */
	protected Point getInitialLocation(Point initialSize) {
		Point initialLocation= DialogSettingsHelper.getInitialLocation(getDialogSettingsSectionName());
		if (initialLocation != null) {
			return initialLocation;
		}
		return super.getInitialLocation(initialSize);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		return DialogSettingsHelper.getInitialSize(getDialogSettingsSectionName(), size);
	}
}
