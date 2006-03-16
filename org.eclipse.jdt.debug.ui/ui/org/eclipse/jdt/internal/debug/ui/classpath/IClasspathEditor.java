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
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.swt.widgets.Shell;

/**
 * Edits classpath entries.
 * 
 * @since 3.2
 *
 */
public interface IClasspathEditor {
	
	/**
	 * Returns whether the given classpath entry can be edited.
	 * 
	 * @param configuration launch configuration
	 * @param entries runtime classpath entry
	 * @return whether the entries can be edited
	 */
	public boolean canEdit(ILaunchConfiguration configuration, IRuntimeClasspathEntry[] entries);
	
	/**
	 * Returns replacement entries for the given entries, after editing them,
	 * or <code>null</code> if the edit was cancelled.
	 * 
	 * @param shell shell on which to open dialogs
	 * @param configuration launch configuration
	 * @param entries runtime classpath entries
	 * @return replacement entries or <code>null</code> if cancelled
	 */
	public IRuntimeClasspathEntry[] edit(Shell shell, ILaunchConfiguration configuration, IRuntimeClasspathEntry[] entries);

}
