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
 * An execution environment describes the version and or capabilities of
 * a Java runtime environment or <code>IVMInstall</code>.
 * <p>
 * An execution environment is contributed in plug-in XML via the
 * <code>org.eclipse.jdt.launching.executionEnvironments</code> extension
 * point.
 * </p>
 * @since 3.2
 */
public interface IExecutionEnvironment {
	
	/**
	 * Returns a unique identifier for this execution environment.
	 * Corresponds to the <code>id</code> attribute in plug-in XML.
	 * 
	 * @return unique identifier of this execution environment
	 */
	public String getId();
	
	/**
	 * Returns a brief human-readable description of this environment.
	 * 
	 * @return brief human-readable description of this environment.
	 */
	public String getDescription();
	
	/**
	 * Returns a collection of vm installs compatible with this environment,
	 * possibly empty.
	 * 
	 * @return a collection of vm installs compatible with this environment,
	 *  possibly empty.
	 */
	public IVMInstall[] getCompatibleVMs();
	
	/**
	 * Returns whether the specified vm install is strictly compatible with 
	 * this environment. Returns <code>true</code> to indicate the vm install
	 * is strictly compatible with this environment and <code>false</code> to indicate
	 * the vm install represents a superset of this environment.
	 * 
	 * @param vm vm install
	 * @return whether the vm install is strictly compatible with this environment
	 */
	public boolean isStrictlyCompatible(IVMInstall vm);
	
	/**
	 * Returns the vm that is used by default for this execution environment,
	 * or <code>null</code> if none.
	 * 
	 * @return default vm for this environment or <code>null</code> if none
	 */
	public IVMInstall getDefaultVM();
	
	/**
	 * Sets the vm to use by default for this execution environment.
	 * 
	 * @param vm vm to use by default for this execution environment,
	 *  or <code>null</code> to clear the default setting
	 * @exception IllegalArgumentException if the given vm is not compatible with
	 *  this environment
	 */
	public void setDefaultVM(IVMInstall vm);
}
