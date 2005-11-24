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
import org.eclipse.jdt.internal.launching.environments.Environments;
import org.eclipse.jdt.launching.IVMInstall;

/**
 * Utility class for execution environments.
 * 
 * @since 3.2
 */
public class ExecutionEnvironments {
	
	/**
	 * Returns all registered execution environments.
	 * 
	 * @return all registered execution environments
	 */
	public static IExecutionEnvironment[] getExecutionEnvironments() {
		return Environments.getExecutionEnvironments();
	}
	
	/**
	 * Returns the execution environment associated with the given
	 * identifier or <code>null</code> if none.
	 * 
	 * @param id execution environment identifier 
	 * @return execution environment or <code>null</code>
	 */
	public static IExecutionEnvironment getEnvironment(String id) {
		return Environments.getEnvironment(id);
	}
	
	/**
	 * Returns the exeuctuion environments associated with the specified
	 * vm install, possibly an empty collection.
	 * 
	 * @param vm vm install
	 * @return exeuctuion environments associated with the specified
	 * vm install, possibly an empty collection
	 */
	public static IExecutionEnvironment[] getEnvironments(IVMInstall vm) {
		return Environments.getEnvironments(vm);
	}

	/**
	 * Returns the vm installs that are compatible with the given 
	 * execution environment, possibly an empty collection.
	 * 
	 * @param environment execution environment
	 * @return vm installs that are compatible with the given 
	 * execution environment, possibly an empty collection
	 */
	public static IVMInstall[] getVMInstalls(IExecutionEnvironment environment) {
		return Environments.getVMInstalls(environment);
	}
	
	/**
	 * Returns all registered exeuction environment analyzers.
	 * 
	 * @return all registered exeuction environment analyzers
	 */
	public static IExecutionEnvironmentAnalyzer[] getAnalyzers() { 
		return Environments.getAnalyzers();
	}
	
	/**
	 * Recomputes the environments compatible with the given vm install.
	 * 
	 * @param vm
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public static IExecutionEnvironment[] analyze(IVMInstall vm, IProgressMonitor monitor) throws CoreException {
		return Environments.analyze(vm, monitor);
	}	
	
}
