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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
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
			return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_LIBRARY);
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
	 * @param project
	 * @return selected VM or <code>null</code>
	 */
	protected IVMInstall chooseVMInstall(IJavaProject project) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), new JRELabelProvider());
		dialog.setElements(getAllVMs());
		dialog.setTitle(LauncherMessages.getString("JREResolution.Select_System_Library_1")); //$NON-NLS-1$
		dialog.setMessage(MessageFormat.format(LauncherMessages.getString("JREResolution.Select_a_system_library_to_use_when_building_{0}_2"), new String[]{project.getElementName()})); //$NON-NLS-1$
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