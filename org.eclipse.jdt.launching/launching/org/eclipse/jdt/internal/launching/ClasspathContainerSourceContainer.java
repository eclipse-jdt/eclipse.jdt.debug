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

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;
import org.eclipse.debug.internal.core.sourcelookup.containers.CompositeSourceContainer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

/**
 * A source container for a classpath container.
 * 
 * @since 3.0
 */
public class ClasspathContainerSourceContainer extends CompositeSourceContainer {
	
	/**
	 * Associated classpath container.
	 */
	private IClasspathContainer fContainer;
	
	/**
	 * Unique identifier for Java project source container type
	 * (value <code>org.eclipse.jdt.launching.sourceContainer.classpathContainer</code>).
	 */
	public static final String TYPE_ID = LaunchingPlugin.getUniqueIdentifier() + ".sourceContainer.classpathContainer";   //$NON-NLS-1$
	
	/**
	 * Constructs a new source container for the given classpath container.
	 * 
	 * @param container classpath container
	 */
	public ClasspathContainerSourceContainer(IClasspathContainer container) {
		fContainer = container;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return fContainer.getDescription();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return SourceLookupUtils.getSourceContainerType(TYPE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.containers.CompositeSourceContainer#createSourceContainers()
	 */
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		IClasspathEntry[] entries = fContainer.getClasspathEntries();
		ISourceContainer[] containers = new ISourceContainer[entries.length];
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			switch (entry.getEntryKind()) {
				case IClasspathEntry.CPE_PROJECT:
					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(entry.getPath().segment(0));
					containers[i] = new JavaProjectSourceContainer(JavaCore.create(project));
					break;
				case IClasspathEntry.CPE_LIBRARY:
					// TODO:
					break;
				default:
					abort(MessageFormat.format("Classpath container does not resolve to projects and/or libraries: {0}", new String[]{fContainer.getPath().toOSString()}), null);
					break;
			}
		}
		return containers;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#dispose()
	 */
	public void dispose() {
		super.dispose();
		fContainer = null;
	}
}
