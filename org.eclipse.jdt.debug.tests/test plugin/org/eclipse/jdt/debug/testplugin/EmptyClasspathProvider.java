/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.debug.testplugin;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathProvider;

/**
 * Extension to test classpath providers extension point
 */
public class EmptyClasspathProvider implements IRuntimeClasspathProvider {

	/**
	 * @see IRuntimeClasspathProvider#computeUnresolvedClasspath(ILaunchConfiguration)
	 */
	@Override
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) {
		return new IRuntimeClasspathEntry[0];
	}

	/**
	 * @see IRuntimeClasspathProvider#resolveClasspath(IRuntimeClasspathEntry[], ILaunchConfiguration)
	 */
	@Override
	public IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) {
		return new IRuntimeClasspathEntry[0];
	}

}
