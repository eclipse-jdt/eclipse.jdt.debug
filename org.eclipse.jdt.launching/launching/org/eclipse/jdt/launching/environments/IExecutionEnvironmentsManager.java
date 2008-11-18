/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.environments;


/**
 * Manager for execution environments. The singleton manager is available
 * via <code>JavaRuntime.getExecutionEnvironmentsManager()</code>.
 * @since 3.2
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
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
		
}
