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
import org.eclipse.jdt.internal.launching.ClasspathContainerSourceContainer;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.ClasspathContainerWizard;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
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
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		return editSourceContainers(shell, director, null, SourceLookupMessages.getString("ClasspathContainerSourceContainerBrowser.0")); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.ISourceContainerBrowser#canEditSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public boolean canEditSourceContainers(ISourceLookupDirector director, ISourceContainer[] containers) {
		return containers.length == 1;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.ISourceContainerBrowser#editSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public ISourceContainer[] editSourceContainers(Shell shell, ISourceLookupDirector director, ISourceContainer[] containers) {
		ClasspathContainerSourceContainer sourceContainer = (ClasspathContainerSourceContainer)containers[0];
		IPath containerPath = (sourceContainer).getPath();
		IClasspathEntry classpathEntry = JavaCore.newContainerEntry(containerPath);
		return editSourceContainers(shell, director, classpathEntry, SourceLookupMessages.getString("ClasspathContainerSourceContainerBrowser.1")); //$NON-NLS-1$
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
	private ISourceContainer[] editSourceContainers(Shell shell, ISourceLookupDirector director, IClasspathEntry classpathEntry, String title) {
		IJavaProject project = null;
		ILaunchConfiguration configuration = director.getLaunchConfiguration();
		if (configuration != null) {
			try {
				project = JavaRuntime.getJavaProject(configuration);
			} catch (CoreException e) {
			}
		}
		ClasspathContainerWizard wizard = new ClasspathContainerWizard(classpathEntry, project, new IClasspathEntry[0]);
		
		wizard.setWindowTitle(title);
		WizardDialog dialog= new WizardDialog(shell, wizard);
		PixelConverter converter= new PixelConverter(shell);
		
		dialog.setMinimumPageSize(converter.convertWidthInCharsToPixels(40), converter.convertHeightInCharsToPixels(20));
		dialog.create();
		if (dialog.open() == Window.OK) {
			IClasspathEntry[] created= wizard.getNewEntries();
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
		}			
		return new ISourceContainer[0];
	}	
}
