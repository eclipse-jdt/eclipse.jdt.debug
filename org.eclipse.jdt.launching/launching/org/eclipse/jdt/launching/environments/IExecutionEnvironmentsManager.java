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

import org.eclipse.jdt.launching.IVMInstall;

/**
 * Manager for execution environments. The singleton manager is available
 * via <code>JavaRuntime.getExecutionEnvironmentsManager()</code>.
 * 
 * @since 3.2
 */
public interface IExecutionEnvironmentsManager {

	/**
	 * Returns all registered execution environments.
	 * 
	 * @return all registered execution environments
	 */
	public IExecutionEnvironment[] getExecutionEnvironments();
	
	/**
	 * Returns the execution environment associated with the given
	 * identifier or <code>null</code> if none.
	 * 
	 * @param id execution environment identifier 
	 * @return execution environment or <code>null</code>
	 */
	public IExecutionEnvironment getEnvironment(String id);
	
	/**
	 * Returns the exeuctuion environments associated with the specified
	 * vm install, possibly an empty collection.
	 * 
	 * @param vm vm install
	 * @return exeuctuion environments associated with the specified
	 * vm install, possibly an empty collection
	 */
	public IExecutionEnvironment[] getEnvironments(IVMInstall vm);

	/**
	 * Returns the vm installs that are compatible with the given 
	 * execution environment, possibly an empty collection.
	 * 
	 * @param environment execution environment
	 * @return vm installs that are compatible with the given 
	 * execution environment, possibly an empty collection
	 */
	public IVMInstall[] getVMInstalls(IExecutionEnvironment environment);
		
}
