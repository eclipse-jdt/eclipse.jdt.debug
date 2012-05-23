/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.CompositeSourceContainer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * A source container for a classpath container.
 * <p>
 * This class may be instantiated.
 * </p>
 * 
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */

public class ClasspathContainerSourceContainer extends CompositeSourceContainer {
	
	/**
	 * Associated classpath container path.
	 */
	private IPath fContainerPath;
	/**
	 * Unique identifier for Java project source container type
	 * (value <code>org.eclipse.jdt.launching.sourceContainer.classpathContainer</code>).
	 */
	public static final String TYPE_ID = LaunchingPlugin.getUniqueIdentifier() + ".sourceContainer.classpathContainer";   //$NON-NLS-1$
		
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
			return getPath().lastSegment();
		} 
		return container.getDescription();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.containers.CompositeSourceContainer#createSourceContainers()
	 */
	@Override
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		IRuntimeClasspathEntry entry = JavaRuntime.newRuntimeContainerClasspathEntry(getPath(), IRuntimeClasspathEntry.USER_CLASSES);
		IRuntimeClasspathEntry[] entries = JavaRuntime.resolveSourceLookupPath(new IRuntimeClasspathEntry[]{entry}, getDirector().getLaunchConfiguration());
		return JavaRuntime.getSourceContainers(entries);
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
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClasspathContainerSourceContainer) {
			return getPath().equals(((ClasspathContainerSourceContainer)obj).getPath());
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getPath().hashCode();
	}
	
	/**
	 * Returns the associated container or <code>null</code> if unavailable.
	 * 
	 * @return classpath container or <code>null</code>
	 * @throws CoreException if unable to retrieve container
	 */
	public IClasspathContainer getClasspathContainer() throws CoreException {
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
