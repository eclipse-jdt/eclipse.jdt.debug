package org.eclipse.jdt.launching;

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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Default implementation of source lookup path computation and resolution.
 * <p>
 * This class may be subclassed.
 * </p>
 * @since 2.0
 */
public class StandardSourcePathProvider extends StandardClasspathProvider {
	
	/**
	 * @see IRuntimeClasspathProvider#computeUnresolvedClasspath(ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException {
		boolean useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, true);
		IRuntimeClasspathEntry[] entries = null;
		if (useDefault) {
			// the default source lookup path is the same as the classpath
			entries = super.computeUnresolvedClasspath(configuration);
		} else {
			// recover persisted source path
			entries = recoverRuntimePath(configuration, IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH);
		}
		return entries;

	}
	

	/**
	 * @see IRuntimeClasspathProvider#resolveClasspath(IRuntimeClasspathEntry[], ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) throws CoreException {
		boolean includeJRE = false;
		IJavaProject pro = JavaRuntime.getJavaProject(configuration);
		includeJRE = pro == null;
		// omit JRE from source lookup path if the runtime JRE is the same as the build JRE
		// (so we retrieve source from the workspace, and not an external jar)
		if (!includeJRE) {
			IVMInstall buildVM = JavaRuntime.getVMInstall(pro);
			IVMInstall runVM = JavaRuntime.computeVMInstall(configuration);
			includeJRE = !buildVM.equals(runVM);
		}
		if (!includeJRE) {
			// remove the JRE entry
			List list = new ArrayList(entries.length);
			for (int i = 0; i < entries.length; i++) {
				switch (entries[i].getType()) {
					case IRuntimeClasspathEntry.VARIABLE:
						if (!entries[i].getVariableName().equals(JavaRuntime.JRELIB_VARIABLE)) {
							list.add(entries[i]);
						}
						break;
					case IRuntimeClasspathEntry.CONTAINER:
						if (!entries[i].getVariableName().equals(JavaRuntime.JRE_CONTAINER)) {
							list.add(entries[i]);
						}
						break;						
					default:
						list.add(entries[i]);
						break;
				}
			}
			entries = (IRuntimeClasspathEntry[]) list.toArray(new IRuntimeClasspathEntry[list.size()]);
		}
		return super.resolveClasspath(entries, configuration);
	}

}
