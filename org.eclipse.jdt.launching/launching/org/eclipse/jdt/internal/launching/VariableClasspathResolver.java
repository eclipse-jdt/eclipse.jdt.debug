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

package org.eclipse.jdt.internal.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;


public class VariableClasspathResolver implements IRuntimeClasspathEntryResolver {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(org.eclipse.jdt.launching.IRuntimeClasspathEntry, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
		return resolveRuntimeClasspathEntry(entry);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(org.eclipse.jdt.launching.IRuntimeClasspathEntry, org.eclipse.jdt.core.IJavaProject)
	 */
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
		return resolveRuntimeClasspathEntry(entry);
	}

	private IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry) throws CoreException{
		String variableString = ((VariableClasspathEntry)entry).getVariableString();
		String strpath = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(variableString);
		IPath path = new Path(strpath).makeAbsolute();
		IRuntimeClasspathEntry archiveEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(path);
		return new IRuntimeClasspathEntry[] { archiveEntry };	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver#resolveVMInstall(org.eclipse.jdt.core.IClasspathEntry)
	 */
	public IVMInstall resolveVMInstall(IClasspathEntry entry) throws CoreException {
		return null;
	}
}
