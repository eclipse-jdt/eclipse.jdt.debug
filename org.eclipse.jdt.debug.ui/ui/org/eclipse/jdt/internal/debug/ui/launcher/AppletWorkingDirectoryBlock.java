package org.eclipse.jdt.internal.debug.ui.launcher;

import org.eclipse.jdt.launching.JavaRuntime;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
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
