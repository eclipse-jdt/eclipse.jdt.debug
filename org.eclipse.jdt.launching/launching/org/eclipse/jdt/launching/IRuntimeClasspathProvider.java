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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * A classpath provider computes an unresolved classpath for a launch
 * configuration, and resolves classpath entries for a launch configuration.
 * A classpath provider is defined as an extension of type 
 * <code>org.eclipse.jdt.launching.classpathProvider</code>.
 * 
 * @since 2.0
 */
public interface IRuntimeClasspathProvider {
	
	/**
	 * Computes and returns an unresolved classpath for the given launch configuration.
	 * Variable and container entries are not resolved.
	 * 
	 * @param configuration launch configuration
	 * @return unresolved path
	 * @exception CoreException if unable to compute a path
	 */
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException;
	
	/**
	 * Returns the resolved path corresponding to the given path, in the context of the
	 * given launch configuration. Variable and container entries are resolved. The returned
	 * (resolved) path may not have the same number of entries as the given (unresolved)
	 * path.
	 * 
	 * @param entries entries to resolve
	 * @param configuration launch configuration context to resolve in
	 * @return resolved path
	 * @exception CoreException if unable to resolve a path
	 */
	public IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) throws CoreException;

}
