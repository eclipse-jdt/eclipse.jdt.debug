/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.ui.launchConfigurations;

import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.launcher.AppletWorkingDirectoryBlock;
import org.eclipse.jdt.internal.debug.ui.launcher.WorkingDirectoryBlock;
import org.eclipse.ui.PlatformUI;

/**
 * A launch configuration tab that displays and edits program arguments,
 * VM arguments, and working directory launch configuration attributes,
 * for applets.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * @since 2.1
 */
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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_APPLET_ARGUMENTS_TAB);		
	}
			
}
