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
package org.eclipse.jdt.internal.debug.ui.launcher;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Superclass of for JRE resolution errors.
 */
public abstract class JREResolution implements IMarkerResolution {
	
	class JRELabelProvider extends LabelProvider {
		
		/**
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
            return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
		}

		/**
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			return ((IVMInstall)element).getName();
		}

	}
	
	/**
	 * Prompts the user to choose a JRE for the given project.
	 * Returns the selected VM or <code>null</code>.
	 * 
	 * @param title the title for the dialog
	 * @param message the message for the dialog
	 * @return selected VM or <code>null</code>
	 */
	protected IVMInstall chooseVMInstall(String title, String message) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), new JRELabelProvider());
		dialog.setElements(getAllVMs());
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setMultipleSelection(false);
		dialog.open();
		return (IVMInstall)dialog.getFirstResult();
	}
	
	/**
	 * Returns all defined VMs
	 * 
	 * @return IVMInstall[]
	 */
	protected static IVMInstall[] getAllVMs() {
		IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
		List vms = new ArrayList();
		for (int i = 0; i < types.length; i++) {
			IVMInstallType type = types[i];
			IVMInstall[] installs = type.getVMInstalls();
			for (int j = 0; j < installs.length; j++) {
				vms.add(installs[j]);
			}
		}
		return (IVMInstall[])vms.toArray(new IVMInstall[vms.size()]);		
	}

}
