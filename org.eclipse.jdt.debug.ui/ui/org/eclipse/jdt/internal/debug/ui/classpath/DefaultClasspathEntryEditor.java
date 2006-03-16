/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.classpath;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.DefaultProjectClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

public class DefaultClasspathEntryEditor implements IClasspathEditor {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEditor#canEdit(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.jdt.launching.IRuntimeClasspathEntry[])
	 */
	public boolean canEdit(ILaunchConfiguration configuration, IRuntimeClasspathEntry[] entries) {
		return entries.length == 1 && entries[0] instanceof DefaultProjectClasspathEntry;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEditor#edit(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.jdt.launching.IRuntimeClasspathEntry[])
	 */
	public IRuntimeClasspathEntry[] edit(Shell shell, ILaunchConfiguration configuration, IRuntimeClasspathEntry[] entries) {
		DefaultClasspathEntryDialog dialog = new DefaultClasspathEntryDialog(shell, entries[0]);
		if (dialog.open() == Window.OK) {
			return entries;
		}
		return null;
	}

}
