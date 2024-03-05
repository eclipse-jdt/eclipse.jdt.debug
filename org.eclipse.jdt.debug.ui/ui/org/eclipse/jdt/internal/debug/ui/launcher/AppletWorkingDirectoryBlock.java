/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - Bug 214696 Expose WorkingDirectoryBlock as API
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.JavaRuntime;


public class AppletWorkingDirectoryBlock extends JavaWorkingDirectoryBlock {

	/**
	 * @see org.eclipse.debug.ui.WorkingDirectoryBlock#setDefaultWorkingDir
	 */
	@Override
	protected void setDefaultWorkingDir() {
		String outputDir = JavaRuntime.getProjectOutputDirectory(getLaunchConfiguration());
		if (outputDir != null) {
			outputDir = new Path(outputDir).makeRelative().toOSString();
			setDefaultWorkingDirectoryText("${workspace_loc:"  + outputDir + "}");  //$NON-NLS-1$//$NON-NLS-2$
		} else {
			super.setDefaultWorkingDir();
		}
	}

}
