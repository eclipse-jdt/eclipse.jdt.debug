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

import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jdt.launching.sourcelookup.containers.ClasspathVariableSourceContainer;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.swt.widgets.Shell;

/**
 * Used to choose a classpath variable.
 * 
 * @since 3.0
 */
public class ClasspathVariableSourceContainerBrowser extends AbstractSourceContainerBrowser {
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
		ClasspathVariableSourceContainer container = (ClasspathVariableSourceContainer) containers[0];
		IPath path = BuildPathDialogAccess.configureVariableEntry(shell, container.getPath(), new IPath[]{container.getPath()});
		if (path != null) {			
			containers = new ISourceContainer[1];
			containers[0] = new ClasspathVariableSourceContainer(path);
			return containers;
		}
		return new ISourceContainer[0];
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#createSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		IPath[] paths = BuildPathDialogAccess.chooseVariableEntries(shell, new IPath[0]);
		if (paths != null) {			
			ISourceContainer[] containers = new ISourceContainer[paths.length];
			for (int i = 0; i < containers.length; i++) {
				containers[i] = new ClasspathVariableSourceContainer(paths[i]);
			}
			return containers;
		}
		return new ISourceContainer[0];
	}
}
