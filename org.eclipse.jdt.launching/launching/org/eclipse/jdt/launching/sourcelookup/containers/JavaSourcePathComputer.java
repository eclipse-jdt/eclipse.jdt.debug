/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.sourcelookup.containers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Computes a default source lookup path for Java applications.
 * The source path provider associated with a launch configuration is consulted
 * to compute a source lookup path. The source path provider is determined
 * by the <code>ATTR_SOURCE_PATH_PROVIDER</code> launch configuration attribute,
 * which defaults to the <code>StandardSourcePathProvider</code> when unspecified.
 * The source path provider computes a collection of <code>IRuntimeClasspathEntry</code>'s
 * which are translated to source containers (<code>ISourceContainer</code>).
 * <p>
 * Clients may subclass this class. 
 * </p>
 * @since 3.0
 * 
 */
public class JavaSourcePathComputer implements ISourcePathComputerDelegate {
	
	/**
	 * Unique identifier for the local Java source path computer
	 * (value <code>org.eclipse.jdt.launching.sourceLookup.javaSourcePathComputer</code>).
	 */
	public static final String ID = "org.eclipse.jdt.launching.sourceLookup.javaSourcePathComputer"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourcePathComputer#getId()
	 * 
     * No longer used.
	 */
	public String getId() {
		return ID;
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedSourceLookupPath(configuration);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveSourceLookupPath(entries, configuration);
		return JavaRuntime.getSourceContainers(resolved);
	}
	
}
