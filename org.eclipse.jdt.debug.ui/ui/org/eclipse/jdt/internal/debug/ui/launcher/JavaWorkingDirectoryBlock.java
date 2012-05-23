/*******************************************************************************
 *  Copyright (c) 2008, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com>
 *      - Bug 214696 Expose WorkingDirectoryBlock as API
 *      - Bug 221973 Make WorkingDirectoryBlock from JDT a Debug API class
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.WorkingDirectoryBlock;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * A UI block allowing a working directory to be specified for a launch
 * configuration.
 * 
 * @since 3.4
 */
public class JavaWorkingDirectoryBlock extends WorkingDirectoryBlock {

	/**
	 * Constructs a new working directory block.
	 */
	public JavaWorkingDirectoryBlock() {
		super(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
				IJavaDebugHelpContextIds.WORKING_DIRECTORY_BLOCK);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.WorkingDirectoryBlock#getProject(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	protected IProject getProject(ILaunchConfiguration configuration)
			throws CoreException {
		IJavaProject project = JavaRuntime.getJavaProject(configuration);
		return project == null ? null : project.getProject();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.WorkingDirectoryBlock#log(org.eclipse.core.runtime.CoreException)
	 */
	@Override
	protected void log(CoreException e) {
		setErrorMessage(e.getMessage());
	}

}
