package org.eclipse.jdt.debug.testplugin;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
 
import org.eclipse.core.runtime.CoreException;
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
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException {
		return new IRuntimeClasspathEntry[0];
	}

	/**
	 * @see IRuntimeClasspathProvider#resolveClasspath(IRuntimeClasspathEntry[], ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) throws CoreException {
		return new IRuntimeClasspathEntry[0];
	}

}
