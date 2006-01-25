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
package org.eclipse.jdt.internal.debug.ui.launcher;

import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;

/**
 * Common function for Java launch tabs display runtime classpath entries.
 * 
 * @since 3.2
 */
public abstract class AbstractJavaClasspathTab extends JavaLaunchTab implements IEntriesChangedListener {
			
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IEntriesChangedListener#entriesChanged(org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer)
	 */
	public void entriesChanged(IClasspathViewer viewer) {
		setDirty(true);
		updateLaunchConfigurationDialog();
	}
}

