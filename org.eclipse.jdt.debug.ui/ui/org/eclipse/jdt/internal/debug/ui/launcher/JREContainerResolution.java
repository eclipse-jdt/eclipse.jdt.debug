package org.eclipse.jdt.internal.debug.ui.launcher;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.launching.JREContainer;
import org.eclipse.jdt.internal.launching.JREContainerInitializer;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * Handles JRE resolution for an unbound JRE container
 */
public class JREContainerResolution extends JREResolution {
	
	private IPath fUnboundPath;
	private IJavaProject fProject;
	
	public JREContainerResolution(IPath unboundPath, IJavaProject project) {
		fUnboundPath = unboundPath;
		fProject = project;	
	}

	/**
	 * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
	 */
	public void run(IMarker marker) {
		try {
			handleContainerResolutionError(fUnboundPath, fProject);
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("JREContainerResolution.Unable_to_update_classpath_1"), e.getStatus());  //$NON-NLS-1$
		}
	}
	
	protected void handleContainerResolutionError(final IPath unboundPath, final IJavaProject project) throws CoreException {			
		
		final IVMInstall vm = chooseVMInstall(project);
		if (vm == null) {
			return;
		}

		ProgressMonitorDialog monitor = new ProgressMonitorDialog(JDIDebugUIPlugin.getActiveWorkbenchShell());
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
					String vmTypeId = vm.getVMInstallType().getId();
					String vmName = vm.getName();
					String prevId = JREContainerInitializer.getVMTypeId(unboundPath);
					String prevName = JREContainerInitializer.getVMName(unboundPath);
					try {
						IPath newBinding = unboundPath;
						if (!(prevId.equals(vmTypeId) && prevName.equals(vmName))) {
							// update classpath
							IPath newPath = new Path(JavaRuntime.JRE_CONTAINER);
							if (vmTypeId != null) {
								newPath = newPath.append(vmTypeId).append(vmName);
							}
							IClasspathEntry[] classpath = project.getRawClasspath();
							for (int i = 0; i < classpath.length; i++) {
								switch (classpath[i].getEntryKind()) {
									case IClasspathEntry.CPE_CONTAINER:
										if (classpath[i].getPath().equals(unboundPath)) {
											classpath[i] = JavaCore.newContainerEntry(newPath, classpath[i].isExported());
										}
										break;
									default:
										break;
								}
							}
							project.setRawClasspath(classpath, monitor);
							newBinding = newPath;
						}
					JavaCore.setClasspathContainer(unboundPath, new IJavaProject[] {project}, new IClasspathContainer[] {new JREContainer(vm, newBinding)}, monitor);
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
				LauncherMessages.getString("JREContainerResolution.An_exception_occurred_while_updating_the_classpath._1"), e.getTargetException())); //$NON-NLS-1$
		} catch (InterruptedException e) {
			// cancelled
		}
	}		
	/**
	 * @see org.eclipse.ui.IMarkerResolution#getLabel()
	 */
	public String getLabel() {
		return MessageFormat.format(LauncherMessages.getString("JREContainerResolution.Select_a_system_library_to_use_when_building_{0}_2"), new String[]{fProject.getElementName()}); //$NON-NLS-1$
	}

}
