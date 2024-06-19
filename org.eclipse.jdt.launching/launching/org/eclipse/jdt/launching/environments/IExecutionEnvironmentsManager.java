/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
package org.eclipse.jdt.launching.environments;

import java.util.List;

import org.eclipse.jdt.core.JavaCore;

/**
 * Manager for execution environments. The singleton manager is available
 * via <code>JavaRuntime.getExecutionEnvironmentsManager()</code>.
 * @since 3.2
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IExecutionEnvironmentsManager {

	/**
	 * Returns all registered execution environments sorted by their id.
	 *
	 * @see IExecutionEnvironment#getId()
	 *
	 * @return all registered execution environments sorted by their id
	 */
	public IExecutionEnvironment[] getExecutionEnvironments();

	/**
	 * Returns all execution environments supported by Java projects, <b>reverse</b> sorted by their id.
	 *
	 * @see IExecutionEnvironment#getId()
	 * @see JavaCore#isJavaSourceVersionSupportedByCompiler(String)
	 *
	 * @return all registered execution environments sorted by their id
	 * @since 3.23
	 */
	public List<IExecutionEnvironment> getSupportedExecutionEnvironments();

	/**
	 * Returns the execution environment associated with the given
	 * identifier or <code>null</code> if none.
	 *
	 * @param id execution environment identifier
	 * @return execution environment or <code>null</code>
	 */
	public IExecutionEnvironment getEnvironment(String id);

}
