/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.sourcelookup.containers;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.CompositeSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;

/**
 * Java project source container. Searches for source in a project's
 * source folders.
 * <p>
 * This class may be instantiated; this class is not intended to be
 * subclassed. 
 * </p>
 * 
 * @since 3.0
 */
public class JavaProjectSourceContainer extends CompositeSourceContainer {
		
	// Java project
	private IJavaProject fProject;
	// Source folders
	private ISourceContainer[] fSourceFolders;
	// Generic project container
	private ISourceContainer[] fOthers;
	
	/**
	 * Unique identifier for Java project source container type
	 * (value <code>org.eclipse.jdt.launching.sourceContainer.javaProject</code>).
	 */
	public static final String TYPE_ID = LaunchingPlugin.getUniqueIdentifier() + ".sourceContainer.javaProject";   //$NON-NLS-1$
	
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
		return getSourceContainerType(TYPE_ID);
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
	 * @see org.eclipse.debug.internal.core.sourcelookup.containers.CompositeSourceContainer#createSourceContainers()
	 */
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		List containers = new ArrayList();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (fProject.getProject().isOpen()) {
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
		}
		// cache the Java source folders to search for ".java" files in
		fSourceFolders = (ISourceContainer[]) containers.toArray(new ISourceContainer[containers.size()]);
		ISourceContainer theProject = new ProjectSourceContainer(fProject.getProject(), false);
		fOthers = new ISourceContainer[] {theProject};
		containers.add(theProject);
		return (ISourceContainer[]) containers.toArray(new ISourceContainer[containers.size()]);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof JavaProjectSourceContainer) {
			return getJavaProject().equals(((JavaProjectSourceContainer)obj).getJavaProject());
		}
		return super.equals(obj);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getJavaProject().hashCode();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
	 */
	public Object[] findSourceElements(String name) throws CoreException {
		// force container initialzation
		getSourceContainers();
		
		if (name.endsWith(".java")) { //$NON-NLS-1$
			// only look in source folders
			Object[] objects = findSourceElements(name, fSourceFolders);
			List filtered = null;
			for (int i = 0; i < objects.length; i++) {
				Object object = objects[i];
				if (object instanceof IResource) {
					if (!getJavaProject().isOnClasspath((IResource)object)) {
						if (filtered == null) {
							filtered = new ArrayList(objects.length);
							for (int j = 0; j < objects.length; j++) {
								filtered.add(objects[j]);
							}
						}
						filtered.remove(object);
					}
				}
			}
			if (filtered == null) {
				return objects;
			}
			return filtered.toArray();			
		} 
		// look elsewhere if non a ".java" file
		return findSourceElements(name, fOthers);
	}
	public void dispose() {
		fSourceFolders = null;
		fOthers = null;
		super.dispose();
	}
}
