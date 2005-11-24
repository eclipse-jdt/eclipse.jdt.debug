/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.environments;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.launching.IVMInstall;

/**
 * Analyzes vm installs for compatibility with execution environments.
 * <p>
 * An execution environment analyzer is contributed in plug-in XML via
 * the <code>org.eclipse.jdt.launching.executionEnvironments</code> 
 * extension point.
 * </p>
 * @since 3.2
 */
public interface IExecutionEnvironmentAnalyzer {
	
	/**
	 * Analyzes the given vm install and returns a collection of execution
	 * environments compatible with it or an empty collection if none.
	 * 
	 * @param vm vm install to analyze
	 * @param monitor progress monitor
	 * @return execution environments compatible with the specified vm install,
	 *  possibly empty
	 * @throws CoreException if an exception occurrs analyzing the vm install
	 */
	public IExecutionEnvironment[] analyze(IVMInstall vm, IProgressMonitor monitor) throws CoreException;

}
