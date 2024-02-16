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

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;

/**
 * A Utilities class.
 */
public class JavaLaunchingUtils {

	public static final int MAX_LAUNCH_CONFIG_NAME_LENGTH = 10;

	/**
	 * Returns the argument file for a launch within the given temp directory
	 *
	 * @param processTempFilesDir
	 *            The processing file directory
	 * @param optionalString
	 *            Optional string to be formatted and used for creating file
	 * @return Created file
	 */
	public static File createFileForArgument(String timeStamp, File processTempFilesDir, String launchConfigName, String optionalString) {
		if (launchConfigName.length() > MAX_LAUNCH_CONFIG_NAME_LENGTH) {
			launchConfigName = launchConfigName.substring(0, MAX_LAUNCH_CONFIG_NAME_LENGTH);
		}
		String child = String.format(org.eclipse.jdt.internal.launching.LaunchingPlugin.LAUNCH_TEMP_FILE_PREFIX
				+ optionalString, launchConfigName, timeStamp);
		File argumentsFile = new File(processTempFilesDir, child);
		return argumentsFile;
	}

	public static String getLaunchTimeStamp(ILaunch launch) {
		String timeStamp = launch.getAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP);
		if (timeStamp == null) {
			timeStamp = Long.toString(System.currentTimeMillis());
		}
		return timeStamp;
	}

}
