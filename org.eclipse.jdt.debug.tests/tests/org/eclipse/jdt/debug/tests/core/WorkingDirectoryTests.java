/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * Tests working directories.
 */
public class WorkingDirectoryTests extends AbstractDebugTest {

	public WorkingDirectoryTests(String name) {
		super(name);
	}

	public void testDefaultWorkingDirectory() throws Exception {
		String typeName = "WorkingDirectoryTest";
		createLineBreakpoint(19, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			IVariable var = thread.findVariable("dir");
			String dir = var.getValue().getValueString();
			IPath path = new Path(dir);
			assertEquals("default working dir should be the project directory.", get14Project().getProject().getLocation(), path);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Sets the working directory attribute of the test launch config
	 * @param path
	 */
	protected void setWorkingDirectory(IPath path) throws CoreException {
		ILaunchConfiguration configuration = getLaunchConfiguration("WorkingDirectoryTest");
		ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
		String dir = null;
		if (path != null) {
			dir = path.toString();
		}
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, dir);
		workingCopy.doSave();
	}


	public void testWorkspaceRelativeWorkingDirectory() throws Exception {
		String typeName = "WorkingDirectoryTest";
		createLineBreakpoint(19, typeName);
		IPath wd = get14Project().getProject().getFolder("src").getFullPath().makeRelative();
		setWorkingDirectory(wd);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			IVariable var = thread.findVariable("dir");
			String dir = var.getValue().getValueString();
			IPath path = new Path(dir);
			assertEquals("working dir should be the src directory.", get14Project().getProject().getFolder("src").getLocation(), path);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			setWorkingDirectory(null);
		}
	}

	public void testAbsoluteWorkingDirectory() throws Exception {
		String typeName = "WorkingDirectoryTest";
		createLineBreakpoint(19, typeName);
		IPath wd = get14Project().getProject().getFolder("src").getLocation();
		setWorkingDirectory(wd);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			IVariable var = thread.findVariable("dir");
			String dir = var.getValue().getValueString();
			IPath path = new Path(dir);
			assertEquals("working dir should be the src directory.", get14Project().getProject().getFolder("src").getLocation(), path);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			setWorkingDirectory(null);
		}
	}
}
