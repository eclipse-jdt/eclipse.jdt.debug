/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.sourcelookup.containers.ClasspathContainerSourceContainer;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.swt.widgets.Shell;

/**
 * Used to choose a classpath container.
 * 
 * @since 3.0
 */
public class ClasspathContainerSourceContainerBrowser extends AbstractSourceContainerBrowser {
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#createSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		return editLibraries(shell, director, null);
		// SourceLookupMessages.getString("ClasspathContainerSourceContainerBrowser.0")
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.ISourceContainerBrowser#canEditSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	@Override
	public boolean canEditSourceContainers(ISourceLookupDirector director, ISourceContainer[] containers) {
		return containers.length == 1;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.ISourceContainerBrowser#editSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	@Override
	public ISourceContainer[] editSourceContainers(Shell shell, ISourceLookupDirector director, ISourceContainer[] containers) {
		ClasspathContainerSourceContainer sourceContainer = (ClasspathContainerSourceContainer)containers[0];
		IPath containerPath = (sourceContainer).getPath();
		IClasspathEntry classpathEntry = JavaCore.newContainerEntry(containerPath);
		return editLibraries(shell, director, classpathEntry);
		//, SourceLookupMessages.getString("ClasspathContainerSourceContainerBrowser.1")
	}
	
	/**
	 * Create or edit a container classpath entry.
	 * 
	 * @param shell shell to open dialog on
	 * @param director source lookup director
	 * @param classpathEntry entry to edit, or <code>null</code> if creating
	 * @param title dialog title
	 * @return new or replacement source containers
	 */
	private ISourceContainer[] editLibraries(Shell shell, ISourceLookupDirector director, IClasspathEntry classpathEntry) {
		IJavaProject project = null;
		ILaunchConfiguration configuration = director.getLaunchConfiguration();
		if (configuration != null) {
			try {
				project = JavaRuntime.getJavaProject(configuration);
			} catch (CoreException e) {
			}
		}
		IClasspathEntry[] edits = null;
		IClasspathEntry[] created = null;
		if (classpathEntry == null) {
			edits = new IClasspathEntry[0];
			created = BuildPathDialogAccess.chooseContainerEntries(shell, project, edits);
		} else {
			edits = new IClasspathEntry[]{classpathEntry};
			IClasspathEntry edit = BuildPathDialogAccess.configureContainerEntry(shell, classpathEntry, project, edits);
			if (edit != null) {
				created = new IClasspathEntry[]{edit};
			}
		}
		if (created != null) {
			ISourceContainer[] newContainers = new ISourceContainer[created.length];
			for (int i = 0; i < created.length; i++) {
				IClasspathEntry entry = created[i];
				ClasspathContainerSourceContainer container = new ClasspathContainerSourceContainer(entry.getPath());
				container.init(director);
				newContainers[i] = container;
			}
			return newContainers;
		}			
		return new ISourceContainer[0];
	}	
}
