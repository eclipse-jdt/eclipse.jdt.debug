/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.internal.launching.DefaultProjectClasspathEntry;

/**
 * Default implementation of source lookup path computation and resolution.
 * <p>
 * This class may be subclassed.
 * </p>
 * @since 2.0
 */
public class StandardSourcePathProvider extends StandardClasspathProvider {
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathProvider#computeUnresolvedClasspath(org.eclipse.debug.core.ILaunchConfiguration)
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

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathProvider#resolveClasspath(org.eclipse.jdt.launching.IRuntimeClasspathEntry[], org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) throws CoreException {
		List all = new ArrayList(entries.length);
		for (int i = 0; i < entries.length; i++) {
			switch (entries[i].getType()) {
				case IRuntimeClasspathEntry.PROJECT:
					// a project resolves to itself for source lookup (rather than the class file output locations)
					all.add(entries[i]);
					break;
				case IRuntimeClasspathEntry.OTHER:
					IRuntimeClasspathEntry2 entry = (IRuntimeClasspathEntry2)entries[i];
					if (entry.getTypeId().equals(DefaultProjectClasspathEntry.TYPE_ID)) {
						// add the resolved children of the project
						IRuntimeClasspathEntry[] children = entry.getRuntimeClasspathEntries(configuration);
						IRuntimeClasspathEntry[] res = JavaRuntime.resolveSourceLookupPath(children, configuration);
						for (int j = 0; j < res.length; j++) {
							all.add(res[j]);
						}
					}
					break;
				default:
					IRuntimeClasspathEntry[] resolved =JavaRuntime.resolveRuntimeClasspathEntry(entries[i], configuration);
					for (int j = 0; j < resolved.length; j++) {
						all.add(resolved[j]);
					}
					break;
			}
		}
		return (IRuntimeClasspathEntry[])all.toArray(new IRuntimeClasspathEntry[all.size()]);
	}

}
