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
package org.eclipse.jdt.internal.launching;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathProvider;

/**
 * Proxy to a runtime classpath provider extension.
 */
public class RuntimeClasspathProvider implements IRuntimeClasspathProvider {

	private final IConfigurationElement fConfigurationElement;

	private IRuntimeClasspathProvider fDelegate;

	/**
	 * Constructs a new resolver on the given configuration element
	 * @param element the element
	 */
	public RuntimeClasspathProvider(IConfigurationElement element) {
		fConfigurationElement = element;
	}

	/**
	 * Returns the resolver delegate (and creates if required)
	 * @return the provider
	 * @throws CoreException if an error occurs
	 */
	protected IRuntimeClasspathProvider getProvider() throws CoreException {
		if (fDelegate == null) {
			fDelegate = (IRuntimeClasspathProvider)fConfigurationElement.createExecutableExtension("class"); //$NON-NLS-1$
		}
		return fDelegate;
	}

	public String getIdentifier() {
		return fConfigurationElement.getAttribute("id"); //$NON-NLS-1$
	}
	/**
	 * @see IRuntimeClasspathProvider#computeUnresolvedClasspath(ILaunchConfiguration)
	 */
	@Override
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException {
		return getProvider().computeUnresolvedClasspath(configuration);
	}

	/**
	 * @see IRuntimeClasspathProvider#resolveClasspath(IRuntimeClasspathEntry[], ILaunchConfiguration)
	 */
	@Override
	public IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) throws CoreException {
		return getProvider().resolveClasspath(entries, configuration);
	}

}
