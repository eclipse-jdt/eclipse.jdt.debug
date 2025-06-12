/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;

/**
 * Quick fix to select an alternate JRE for a project.
 */
public class SelectSystemLibraryQuickFix extends JREResolution {

	private final IPath fOldPath;
	private final IJavaProject fProject;

	public SelectSystemLibraryQuickFix(IPath oldPath, IJavaProject project) {
		fOldPath = oldPath;
		fProject = project;
	}

	/**
	 * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
	 */
	@Override
	public void run(IMarker marker) {
		try {
			handleContainerResolutionError(fOldPath, fProject);
		} catch (CoreException e) {
			JDIDebugUIPlugin.statusDialog(LauncherMessages.JREContainerResolution_Unable_to_update_classpath_1, e.getStatus());
		}
	}

	protected void handleContainerResolutionError(final IPath oldPath, final IJavaProject project) throws CoreException {

		String lib = oldPath.segment(0);
		IPath initialPath = null;
		if (JavaRuntime.JRELIB_VARIABLE.equals(lib)) {
			initialPath = JavaRuntime.newDefaultJREContainerPath();
		} else if (JavaRuntime.JRE_CONTAINER.equals(lib)) {
			initialPath = oldPath;
		}
		IClasspathEntry initialEntry = JavaCore.newContainerEntry(initialPath);
		final IClasspathEntry containerEntry = BuildPathDialogAccess.configureContainerEntry(JDIDebugUIPlugin.getActiveWorkbenchShell(), initialEntry, project, new IClasspathEntry[]{});
		if (containerEntry == null || containerEntry.getPath().equals(oldPath)) {
			return;
		}

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					IPath newPath = containerEntry.getPath();
					IClasspathEntry[] classpath = project.getRawClasspath();
					for (int i = 0; i < classpath.length; i++) {
						if (classpath[i].getPath().equals(oldPath)) {
							classpath[i] = JavaCore.newContainerEntry(newPath, classpath[i].isExported());
							break;
						}
					}
					project.setRawClasspath(classpath, monitor);
				//JavaCore.setClasspathContainer(oldPath, new IJavaProject[] {project}, new IClasspathContainer[] {new JREContainer(vm, newBinding, project)}, monitor);
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
			throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, "An exception occurred while updating the classpath.", e.getTargetException()));  //$NON-NLS-1$
		} catch (InterruptedException e) {
			// cancelled
		}
	}
	/**
	 * @see org.eclipse.ui.IMarkerResolution#getLabel()
	 */
	@Override
	public String getLabel() {
		return NLS.bind(LauncherMessages.JREContainerResolution_Select_a_system_library_to_use_when_building__0__2, fProject.getElementName());
	}

}
