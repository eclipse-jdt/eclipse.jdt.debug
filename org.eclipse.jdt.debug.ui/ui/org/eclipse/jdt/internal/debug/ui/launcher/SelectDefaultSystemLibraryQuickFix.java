/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

/**
 * Quick fix to select an alternate default JRE. 
 */
public class SelectDefaultSystemLibraryQuickFix extends JREResolution {
	
	public SelectDefaultSystemLibraryQuickFix() {
		super();
	}

	/**
	 * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
	 */
	public void run(IMarker marker) {
		try {
			String title = LauncherMessages.SelectDefaultSystemLibraryQuickFix_Select_Default_System_Library_1; 
			String message = LauncherMessages.SelectDefaultSystemLibraryQuickFix__Select_the_system_library_to_use_by_default_for_building_and_running_Java_projects__2; 
		
			final IVMInstall vm = chooseVMInstall(title, message);
			if (vm == null) {
				return;
			}

			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
						try {
							JavaRuntime.setDefaultVMInstall(vm, monitor);
						} catch (CoreException e) {
							throw new InvocationTargetException(e);
						}
				}
			};
		
			try {
				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
			} catch (InvocationTargetException e) {
				if (e.getTargetException() instanceof CoreException) {
					throw (CoreException)e.getTargetException();
				}
				throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR,	"An exception occurred while updating the default system library.", e.getTargetException()));  //$NON-NLS-1$
			} catch (InterruptedException e) {
				// canceled
			}			
		} catch (CoreException e) {
			JDIDebugUIPlugin.statusDialog(LauncherMessages.SelectDefaultSystemLibraryQuickFix_Unable_to_update_the_default_system_library__4, e.getStatus()); 
		}
	}
		
	/**
	 * @see org.eclipse.ui.IMarkerResolution#getLabel()
	 */
	public String getLabel() {
		return LauncherMessages.SelectDefaultSystemLibraryQuickFix_Select_default_system_library_5; 
	}

}
