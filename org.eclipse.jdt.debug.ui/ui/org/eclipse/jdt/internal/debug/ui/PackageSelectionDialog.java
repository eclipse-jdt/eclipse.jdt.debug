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

package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/*
 * Same as ElementListSelectionDialog, but persists size/location
 */
public class PackageSelectionDialog extends ElementListSelectionDialog {

	public PackageSelectionDialog(Shell parent, ILabelProvider renderer) {
		super(parent, renderer);
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
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
     */
    @Override
	protected IDialogSettings getDialogBoundsSettings() {
    	 IDialogSettings settings = JDIDebugUIPlugin.getDefault().getDialogSettings();
         IDialogSettings section = settings.getSection(getDialogSettingsSectionName());
         if (section == null) {
             section = settings.addNewSection(getDialogSettingsSectionName());
         } 
         return section;
    }
}
