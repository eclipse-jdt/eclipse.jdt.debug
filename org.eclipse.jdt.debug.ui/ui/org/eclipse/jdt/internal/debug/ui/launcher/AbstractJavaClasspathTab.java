/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	@Override
	public void entriesChanged(IClasspathViewer viewer) {
		setDirty(true);
		updateLaunchConfigurationDialog();
	}
}

