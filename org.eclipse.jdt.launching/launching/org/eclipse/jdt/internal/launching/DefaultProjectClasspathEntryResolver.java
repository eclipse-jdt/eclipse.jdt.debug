/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Resolver for a project classpath entry.
 */
public class DefaultProjectClasspathEntryResolver extends DefaultEntryResolver {
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(org.eclipse.jdt.launching.IRuntimeClasspathEntry, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
		IJavaProject javaProject = JavaRuntime.getJavaProject(configuration);
		if (javaProject != null) {
			checkCycles(javaProject);
		}
		return super.resolveRuntimeClasspathEntry(entry, configuration);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(org.eclipse.jdt.launching.IRuntimeClasspathEntry, org.eclipse.jdt.core.IJavaProject)
	 */
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
		checkCycles(project);
		return super.resolveRuntimeClasspathEntry(entry, project);
	}
		
	/**
	 * Checks for cycles in referenced projects.
	 *   
	 * @param project project to test for cycles in buildpath
	 * @exception CoreException if cycles exist
	 */
	private void checkCycles(IJavaProject project) throws CoreException {
		if (project.hasClasspathCycle(project.getRawClasspath())) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0, LaunchingMessages.getString("DefaultProjectClasspathEntryResolver.0"), null)); //$NON-NLS-1$
		}
	}
		
}
