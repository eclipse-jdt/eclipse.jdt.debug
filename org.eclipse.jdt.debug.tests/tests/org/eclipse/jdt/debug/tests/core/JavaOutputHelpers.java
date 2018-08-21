/*******************************************************************************
 * Copyright (c) 2017 Igor Fedorenko
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Igor Fedorenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.core.runtime.Platform;

public class JavaOutputHelpers {

	/**
	 * Workaround https://bugs.openjdk.java.net/browse/JDK-8022291, which results in extra java output line like below printed to stderr of java
	 * process on OSX.
	 *
	 * <pre>
	 * objc[62928]: Class JavaLaunchHelper is implemented in both /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/bin/java and /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/libinstrument.dylib. One of the two will be used. Which one is undefined.
	 * </pre>
	 */
	public static boolean isKnownExtraneousOutput(String txt) {
		if (!Platform.OS_MACOSX.equals(Platform.getOS())) {
			return false;
		}

		return txt.startsWith("objc[") && txt.contains("Class JavaLaunchHelper is implemented in both");
	}

}
