/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.jdt.internal.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Default resolver for a contributed classpath entry
 */
public class DefaultEntryResolver implements IRuntimeClasspathEntryResolver {
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(org.eclipse.jdt.launching.IRuntimeClasspathEntry, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
		IRuntimeClasspathEntry2 entry2 = (IRuntimeClasspathEntry2)entry;
		IRuntimeClasspathEntry[] entries;
		entries = entry2.getRuntimeClasspathEntries(configuration);
		List<IRuntimeClasspathEntry> resolved = new ArrayList<>();
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry[] temp = JavaRuntime.resolveRuntimeClasspathEntry(entries[i], configuration);
			for (int j = 0; j < temp.length; j++) {
				resolved.add(temp[j]);
			}
		}
		return resolved.toArray(new IRuntimeClasspathEntry[resolved.size()]);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(org.eclipse.jdt.launching.IRuntimeClasspathEntry, org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
		return resolveRuntimeClasspathEntry(entry, project, false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(org.eclipse.jdt.launching.IRuntimeClasspathEntry,
	 * org.eclipse.jdt.core.IJavaProject, boolean)
	 */
	@Override
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project, boolean excludeTestCode) throws CoreException {
		IRuntimeClasspathEntry2 entry2 = (IRuntimeClasspathEntry2)entry;
		IRuntimeClasspathEntry[] entries = entry2.getRuntimeClasspathEntries(excludeTestCode);
		List<IRuntimeClasspathEntry> resolved = new ArrayList<>();
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry[] temp = JavaRuntime.resolveRuntimeClasspathEntry(entries[i], project, excludeTestCode);
			for (int j = 0; j < temp.length; j++) {
				resolved.add(temp[j]);
			}
		}
		return resolved.toArray(new IRuntimeClasspathEntry[resolved.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver#resolveVMInstall(org.eclipse.jdt.core.IClasspathEntry)
	 */
	@Override
	public IVMInstall resolveVMInstall(IClasspathEntry entry) throws CoreException {
		return null;
	}
}
