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

import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.launching.ClasspathContainerSourceContainer;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.ClasspathContainerWizard;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Used to choose a classpath container.
 * 
 * @since 3.0
 */
public class ClasspathContainerSourceContainerBrowser implements ISourceContainerBrowser {
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#createSourceContainers(org.eclipse.swt.widgets.Shell)
	 */
	public ISourceContainer[] createSourceContainers(Shell shell) {
		ClasspathContainerWizard wizard = new ClasspathContainerWizard((IClasspathEntry)null, (IJavaProject)null, new IClasspathEntry[0]);
		
		wizard.setWindowTitle(SourceLookupMessages.getString("ClasspathContainerSourceContainerBrowser.0")); //$NON-NLS-1$
		WizardDialog dialog= new WizardDialog(shell, wizard);
		PixelConverter converter= new PixelConverter(shell);
		
		dialog.setMinimumPageSize(converter.convertWidthInCharsToPixels(40), converter.convertHeightInCharsToPixels(20));
		dialog.create();
		if (dialog.open() == Window.OK) {
			IClasspathEntry created= wizard.getNewEntry();
			if (created != null) {	
				return new ISourceContainer[] {new ClasspathContainerSourceContainer(created.getPath())};
			}
		}			
		return new ISourceContainer[0];		
	}
}
