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
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;
import org.eclipse.debug.internal.core.sourcelookup.containers.CompositeSourceContainer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * A source container for a classpath container.
 * 
 * @since 3.0
 */
public class ClasspathContainerSourceContainer extends CompositeSourceContainer {
	
	/**
	 * Associated classpath container path.
	 */
	private IPath fContainerPath;
		
	/**
	 * Constructs a new source container for the given classpath container.
	 * 
	 * @param containerPath classpath container path
	 */
	public ClasspathContainerSourceContainer(IPath containerPath) {
		fContainerPath = containerPath;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		IClasspathContainer container = null;
		try {
			container = getClasspathContainer();
		} catch (CoreException e) {
		}
		if (container == null) {
			return getPath().toString();
		} else {
			return container.getDescription();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return SourceLookupUtils.getSourceContainerType(ClasspathContainerSourceContainerTypeDelegate.TYPE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.containers.CompositeSourceContainer#createSourceContainers()
	 */
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		IRuntimeClasspathEntry entry = JavaRuntime.newRuntimeContainerClasspathEntry(getPath(), IRuntimeClasspathEntry.USER_CLASSES);
		IRuntimeClasspathEntry[] entries = JavaRuntime.resolveSourceLookupPath(new IRuntimeClasspathEntry[]{entry}, getDirector().getLaunchConfiguration());
		return JavaApplicationSourcePathComputer.translate(entries, true);
	}
	
	/**
	 * Returns the classpath container's path
	 * 
	 * @return classpath container's path
	 */
	public IPath getPath() {
		return fContainerPath;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ClasspathContainerSourceContainer) {
			return getPath().equals(((ClasspathContainerSourceContainer)obj).getPath());
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getPath().hashCode();
	}
	
	/**
	 * Returns the associated container or <code>null</code> if unavailable.
	 * 
	 * @return classpath container or <code>null</code>
	 * @throws CoreException if unable to retrieve container
	 */
	protected IClasspathContainer getClasspathContainer() throws CoreException {
		ISourceLookupDirector director = getDirector();
		if (director != null) {
			ILaunchConfiguration configuration = director.getLaunchConfiguration();
			if (configuration != null) {
				IJavaProject project = JavaRuntime.getJavaProject(configuration);
				if (project != null) {
					return JavaCore.getClasspathContainer(getPath(), project);
				}
			}
		}
		return null;
	}
	
}
