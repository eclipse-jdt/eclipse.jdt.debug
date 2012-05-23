/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com>	- Bug 221973 Make WorkingDirectoryBlock from JDT a Debug API class
 *******************************************************************************/
package org.eclipse.jdt.debug.ui.launchConfigurations;

import org.eclipse.debug.ui.WorkingDirectoryBlock;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.launcher.AppletWorkingDirectoryBlock;
import org.eclipse.ui.PlatformUI;

/**
 * A launch configuration tab that displays and edits program arguments,
 * VM arguments, and working directory launch configuration attributes,
 * for an applet.
 * <p>
 * This class may be instantiated.
 * </p>
 * @since 2.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class AppletArgumentsTab extends JavaArgumentsTab {

	/**
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab#createWorkingDirBlock()
	 */
	@Override
	protected WorkingDirectoryBlock createWorkingDirBlock() {
		return new AppletWorkingDirectoryBlock();
	}

	/**
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab#setHelpContextId()
	 */
	@Override
	protected void setHelpContextId() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_APPLET_ARGUMENTS_TAB);		
	}
			
}
