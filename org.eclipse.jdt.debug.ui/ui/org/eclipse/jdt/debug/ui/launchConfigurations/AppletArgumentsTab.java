package org.eclipse.jdt.debug.ui.launchConfigurations;

import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.launcher.AppletWorkingDirectoryBlock;
import org.eclipse.jdt.internal.debug.ui.launcher.WorkingDirectoryBlock;
import org.eclipse.ui.help.WorkbenchHelp;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
public class AppletArgumentsTab extends JavaArgumentsTab {

	/**
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab#createWorkingDirBlock()
	 */
	protected WorkingDirectoryBlock createWorkingDirBlock() {
		return new AppletWorkingDirectoryBlock();
	}

	/**
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab#setHelpContextId()
	 */
	protected void setHelpContextId() {
		WorkbenchHelp.setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_APPLET_ARGUMENTS_TAB);		
	}
			
}
