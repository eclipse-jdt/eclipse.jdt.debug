/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;

import org.eclipse.jdt.launching.JavaRuntime;

 
public class AppletWorkingDirectoryBlock extends WorkingDirectoryBlock {

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.WorkingDirectoryBlock#setDefaultWorkingDir()
	 */
	protected void setDefaultWorkingDir() {
		String outputDir = JavaRuntime.getProjectOutputDirectory(getLaunchConfiguration());
		if (outputDir != null) {
			fWorkspaceDirText.setText(outputDir);
			fLocalDirButton.setSelection(false);
			fWorkspaceDirButton.setSelection(true);
		} else {
			super.setDefaultWorkingDir();
		}		
	}

}
