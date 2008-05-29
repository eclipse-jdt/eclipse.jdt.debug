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
 * The result of analyzing a vm install for compatibility with an execution
 * environment.
 * <p>
 * An environment analyzer delegate creates instances of this class to describe
 * the environments a vm install is compatible with. A result describes
 * a compatible environment for a vm install and whether the vm install is strictly
 * compatible with the environment or whether the vm install represents
 * a superset of the environment (that is, represents more than is minimally required
 * to be compatible with  an environment).
 * </p>
 * <p>
 * Clients may instantiate this class.
 * </p>
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CompatibleEnvironment {

	private IExecutionEnvironment fEnvironment;
	private boolean fIsStrictlyCompatible;
	
	/**
	 * Constructs a new compatible environment result from an execution environment
	 * analysis.
	 * 
	 * @param environment the environment a vm install is compatible with
	 * @param strict whether the vm install is strictly compatible with the
	 *  environment or represents a superset of the environment
	 */
	public CompatibleEnvironment(IExecutionEnvironment environment, boolean strict) {
		fEnvironment = environment;
		fIsStrictlyCompatible = strict;
	}
	
	/**
	 * Returns an environment compatible with the vm being analyzed.
	 *  
	 * @return compatible execution environment
	 */
	public IExecutionEnvironment getCompatibleEnvironment() {
		return fEnvironment;
	}
	
	/**
	 * Returns whether the analyzed vm install is strictly compatible with the compatible
	 * environment or represents a superset of the environment. Returning <code>true</code>
	 * indicates the analyzed vm install is strictly contained within the environment. Returning
	 * <code>false</code> indicates that the analyzed vm install represents more a superset of
	 * the environment.
	 * 
	 * @return whether the analyzed vm install is strictly contained within the environment
	 */
	public boolean isStrictlyCompatbile() {
		return fIsStrictlyCompatible;
	}
}
