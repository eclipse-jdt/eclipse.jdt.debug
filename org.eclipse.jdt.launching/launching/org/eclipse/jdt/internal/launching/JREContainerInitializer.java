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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
 * <b>IMPLEMENTATION IN PROGRESS</b>
 * 
 * Resolves libraries contained in a JRE.
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
					if (size == 3) {
						// specific JRE
						String vmTypeId = containerPath.segment(1);
						String vmName = containerPath.segment(2);
						IVMInstallType vmType = JavaRuntime.getVMInstallType(vmTypeId);
						if (vmType == null) {
							// unknown type
							// XXX: use status handler to prompt for a JRE
						} else {
							IVMInstall[] vms = vmType.getVMInstalls();
							for (int i = 0; i < vms.length; i++) {
								if (vmName.equals(vms[i].getName())) {
									vm = vms[i];
									break;
								}
							}
							if (vm == null) {
								// could not find corresponding VM
								// XXX: use status handler to prompt to a JRE
							}
						}
					} else {
						// invalid format
						// XXX: log error and prompt for JRE
					}
				} else {
					// workspace default JRE
					vm = JavaRuntime.getDefaultVMInstall();
					if (vm == null) {
						// XXX: prompt for a JRE
					}
				}
				
				if (vm != null) {
					JavaCore.setClasspathContainer(containerPath, new IJavaProject[] {project}, new IClasspathContainer[] {new JREContainer(vm, containerPath)}, null);
				}
			}
		}
	}

}
