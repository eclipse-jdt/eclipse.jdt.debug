package org.eclipse.jdt.internal.launching;

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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

/**
 * Resolves a container for a JRE classpath container entry.
 */
public class JREContainerInitializer extends ClasspathContainerInitializer {

	/**
	 * @see ClasspathContainerInitializer#initialize(IPath, IJavaProject)
	 */
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		int size = containerPath.segmentCount();
		if (size > 0) {
			if (containerPath.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
				IVMInstall vm = null;
				if (size > 1) {
					// specific JRE
					String vmTypeId = containerPath.segment(1);
					String vmName = containerPath.segment(2);
					IVMInstallType vmType = JavaRuntime.getVMInstallType(vmTypeId);
					if (vmType != null) {
						IVMInstall[] vms = vmType.getVMInstalls();
						for (int i = 0; i < vms.length; i++) {
							if (vmName.equals(vms[i].getName())) {
								vm = vms[i];
								break;
							}
						}
					}
					if (vm == null) {
						handleResolutionError(containerPath, project);
						return;
					}					
				} else {
					// workspace default JRE
					vm = JavaRuntime.getDefaultVMInstall();
				}
				
				if (vm != null) {
					JavaCore.setClasspathContainer(containerPath, new IJavaProject[] {project}, new IClasspathContainer[] {new JREContainer(vm, containerPath)}, null);
				}
			}
		}
	}
	
	protected void handleResolutionError(IPath containerPath, IJavaProject project) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), JavaRuntime.ERR_UNABLE_TO_RESOLVE_JRE, 
			MessageFormat.format(LaunchingMessages.getString("JREContainerInitializer.Unable_to_locate_JRE_named_{0}_to_build_project_{1}._1"), new String[] {containerPath.segment(2), project.getElementName()}), null); //$NON-NLS-1$
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
		IVMInstall vm = null;
		if (handler != null) {
			vm = (IVMInstall)handler.handleStatus(status, project);
		}
		
		if (vm == null) {
			throw new CoreException(status);
		} else {
			String vmTypeId = vm.getVMInstallType().getId();
			String vmName = vm.getName();
			String prevId = containerPath.segment(1);
			String prevName = containerPath.segment(2);
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
							if (classpath[i].getPath().equals(containerPath)) {
								classpath[i] = JavaCore.newContainerEntry(newPath, classpath[i].isExported());
							}
							break;
						default:
							break;
					}
				}
				project.setRawClasspath(classpath, null);
				containerPath = newPath;
			}
			JavaCore.setClasspathContainer(containerPath, new IJavaProject[] {project}, new IClasspathContainer[] {new JREContainer(vm, containerPath)}, null);
		}
	}

}
