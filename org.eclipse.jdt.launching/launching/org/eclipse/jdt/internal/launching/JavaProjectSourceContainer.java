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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;
import org.eclipse.debug.internal.core.sourcelookup.containers.CompositeSourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Java project source container. Searches for source in a project's
 * source folders.
 * 
 * @since 3.0
 */
public class JavaProjectSourceContainer extends CompositeSourceContainer {
		
	// Java project
	private IJavaProject fProject;
	
	/**
	 * Constructs a source container on the given Java project.
	 * 
	 * @param project project to look for source in
	 */
	public JavaProjectSourceContainer(IJavaProject project) {
		fProject = project;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return fProject.getElementName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return SourceLookupUtils.getSourceContainerType(JavaProjectSourceContainerTypeDelegate.TYPE_ID);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#dispose()
	 */
	public void dispose() {
		super.dispose();
		fProject = null;
	}
	
	/**
	 * Returns the Java project associated with this source container.
	 * 
	 * @return Java project
	 */
	public IJavaProject getJavaProject() {
		return fProject;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IAdaptable.class.equals(adapter)) {
			return getJavaProject();
		}
		return super.getAdapter(adapter);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.containers.CompositeSourceContainer#createSourceContainers()
	 */
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		List containers = new ArrayList();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IClasspathEntry[] entries = fProject.getRawClasspath();
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			switch (entry.getEntryKind()) {
				case IClasspathEntry.CPE_SOURCE:
					IPath path = entry.getPath();
					IResource resource = root.findMember(path);
					if (resource instanceof IContainer) {
						containers.add(new FolderSourceContainer((IContainer)resource, false));
					}
					break;
			}
		}
		return (ISourceContainer[]) containers.toArray(new ISourceContainer[containers.size()]);
	}
}
