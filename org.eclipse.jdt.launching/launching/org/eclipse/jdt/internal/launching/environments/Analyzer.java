/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.environments;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.environments.CompatibleEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentAnalyzerDelegate;

/**
 * Contributed analyzer.
 * 
 * @since 3.2
 *
 */
class Analyzer implements IExecutionEnvironmentAnalyzerDelegate {
	
	private IConfigurationElement fElement;
	
	private IExecutionEnvironmentAnalyzerDelegate fDelegate;
	
	Analyzer(IConfigurationElement element) {
		fElement = element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironmentAnalyzer#analyze(org.eclipse.jdt.launching.IVMInstall, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public CompatibleEnvironment[] analyze(IVMInstall vm, IProgressMonitor monitor) throws CoreException {
		return getDelegate().analyze(vm, monitor);
	}

	/**
	 * Instantiates and returns the contributed analyzer.
	 * 
	 * @return analyzer
	 * @throws CoreException if an error occurs
	 */
	private IExecutionEnvironmentAnalyzerDelegate getDelegate() throws CoreException {
		if (fDelegate == null) {
			fDelegate = (IExecutionEnvironmentAnalyzerDelegate) fElement.createExecutableExtension("class");  //$NON-NLS-1$
		}
		return fDelegate;
	}

	/**
	 * Returns the id of this delegate
	 * @return id
	 */
	public String getId() {
		return fElement.getAttribute("id"); //$NON-NLS-1$
	}

}
