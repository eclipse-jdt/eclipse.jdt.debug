package org.eclipse.jdt.internal.debug.ui.launcher;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

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
			String title = LauncherMessages.getString("SelectDefaultSystemLibraryQuickFix.Select_Default_System_Library_1"); //$NON-NLS-1$
			String message = LauncherMessages.getString("SelectDefaultSystemLibraryQuickFix.&Select_the_system_library_to_use_by_default_for_building_and_running_Java_projects._2"); //$NON-NLS-1$
		
			final IVMInstall vm = chooseVMInstall(title, message);
			if (vm == null) {
				return;
			}

			ProgressMonitorDialog monitor = new ProgressMonitorDialog(JDIDebugUIPlugin.getActiveWorkbenchShell());
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
						try {
							JavaRuntime.setDefaultVMInstall(vm, monitor);
						} catch (CoreException e) {
							throw new InvocationTargetException(e);
						}
				}
			};
		
			try {
				monitor.run(true, true, runnable);
			} catch (InvocationTargetException e) {
				if (e.getTargetException() instanceof CoreException) {
					throw (CoreException)e.getTargetException();
				}
				throw new CoreException(new Status(IStatus.ERROR,
					JDIDebugUIPlugin.getUniqueIdentifier(),
					IJavaDebugUIConstants.INTERNAL_ERROR,
					LauncherMessages.getString("SelectDefaultSystemLibraryQuickFix.An_exception_occurred_while_updating_the_default_system_library._3"), e.getTargetException())); //$NON-NLS-1$
			} catch (InterruptedException e) {
				// cancelled
			}			
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("SelectDefaultSystemLibraryQuickFix.Unable_to_update_the_default_system_library._4"), e.getStatus()); //$NON-NLS-1$
		}
	}
		
	/**
	 * @see org.eclipse.ui.IMarkerResolution#getLabel()
	 */
	public String getLabel() {
		return LauncherMessages.getString("SelectDefaultSystemLibraryQuickFix.Select_default_system_library_5"); //$NON-NLS-1$
	}

}
