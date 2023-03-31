/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
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
package org.eclipse.jdt.internal.launching;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;

/**
 * A Utilities class.
 *
 */
public class JavaLaunchingUtils {


	/**
	 * Returns the argument file for a launch within the given temp directory
	 *
	 * @param launch
	 *            The launch
	 * @param processTempFilesDir
	 *            The processing file directory
	 * @return Created file
	 */
	public static File createFileForArgument(ILaunch launch, File processTempFilesDir) throws CoreException {
		try {
			String timeStamp = getLaunchTimeStamp(launch);
			File argumentsFile = new File(processTempFilesDir, String.format(org.eclipse.jdt.internal.launching.LaunchingPlugin.LAUNCH_TEMP_FILE_PREFIX
					+ "%s-args-%s.txt", getLaunchConfigurationName(launch), timeStamp)); //$NON-NLS-1$
			return argumentsFile;
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR, "Cannot create argument file", e)); //$NON-NLS-1$
		}
	}

	public static String getLaunchTimeStamp(ILaunch launch) {
		String timeStamp = launch.getAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP);
		if (timeStamp == null) {
			timeStamp = Long.toString(System.currentTimeMillis());
		}
		return timeStamp;
	}

	public static String getLaunchConfigurationName(ILaunch launch) {
		return launch.getLaunchConfiguration().getName();
	}
}
