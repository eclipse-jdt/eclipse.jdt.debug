/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching;


import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Default implementation for classpath provider.
 * <p>
 * This class may be sub-classed.
 * </p>
 * @since 2.0
 */
public class StandardClasspathProvider implements IRuntimeClasspathProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathProvider#computeUnresolvedClasspath(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException {
		boolean useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
		if (useDefault) {
			IJavaProject proj = JavaRuntime.getJavaProject(configuration);
			IRuntimeClasspathEntry jreEntry = JavaRuntime.computeJREEntry(configuration);
			if (proj == null) {
				//no project - use default libraries
				if (jreEntry == null) {
					return new IRuntimeClasspathEntry[0];
				}
				return new IRuntimeClasspathEntry[]{jreEntry};				
			}
			IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(proj);
			// replace project JRE with config's JRE
			IRuntimeClasspathEntry projEntry = JavaRuntime.computeJREEntry(proj);
			if (jreEntry != null && projEntry != null) {
				if (!jreEntry.equals(projEntry)) {
					for (int i = 0; i < entries.length; i++) {
						IRuntimeClasspathEntry entry = entries[i];
						if (entry.equals(projEntry)) {
							entries[i] = jreEntry;
							return entries;
						}
					}
				}
			}
			return entries;
		}
		// recover persisted classpath
		return recoverRuntimePath(configuration, IJavaLaunchConfigurationConstants.ATTR_CLASSPATH);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathProvider#resolveClasspath(org.eclipse.jdt.launching.IRuntimeClasspathEntry[], org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) throws CoreException {
		// use an ordered set to avoid duplicates
		Set<IRuntimeClasspathEntry> all = new LinkedHashSet<IRuntimeClasspathEntry>(entries.length);
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry[] resolved =JavaRuntime.resolveRuntimeClasspathEntry(entries[i], configuration);
			for (int j = 0; j < resolved.length; j++) {
				all.add(resolved[j]);
			}
		}
		return all.toArray(new IRuntimeClasspathEntry[all.size()]);
	}
	
	/**
	 * Returns a collection of runtime classpath entries that are defined in the
	 * specified attribute of the given launch configuration. When present,
	 * the attribute must contain a list of runtime classpath entry mementos.
	 * 
	 * @param configuration launch configuration
	 * @param attribute attribute name containing the list of entries
	 * @return collection of runtime classpath entries that are defined in the
	 *  specified attribute of the given launch configuration
	 * @exception CoreException if unable to retrieve the list
	 */
	protected IRuntimeClasspathEntry[] recoverRuntimePath(ILaunchConfiguration configuration, String attribute) throws CoreException {
		List<String> entries = configuration.getAttribute(attribute, Collections.EMPTY_LIST);
		IRuntimeClasspathEntry[] rtes = new IRuntimeClasspathEntry[entries.size()];
		Iterator<String> iter = entries.iterator();
		int i = 0;
		while (iter.hasNext()) {
			rtes[i] = JavaRuntime.newRuntimeClasspathEntry(iter.next());
			i++;
		}
		return rtes;		
	}	

}
