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

/**
 * An execution environment describes the version and or capabilities of
 * a Java runtime environment or <code>IVMInstall</code>.
 * <p>
 * An execution environment is contributed in plug-in XLM via the
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
}
