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
package org.eclipse.jdt.internal.debug.ui.contentassist;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;

/**
 * Provides a context for context assist in the Java debugger.
 * 
 * @since 3.2
 */
public interface IJavaDebugContentAssistContext {
	
	/**
	 * Returns the type in which to perform completions.
	 * 
	 * @return type in which to perform completions
	 * @throws CoreException if a type cannot be resolved
	 */
	public IType getType() throws CoreException;

	/**
	 * Returns the position within this context's type's source where the snippet
	 * on which completions are being performed is inserted. This position must not
	 * be in comments. Returns -1, if the position is not known.
	 * 
	 * @return position within source where completions are performed or -1
	 * @throws CoreException if an exception occurs determining the position
	 */
	public int getInsertionPosition() throws CoreException;
	
	/**
	 * Returns an array (possibly empty) of local variable information.
	 * If the result is not empty, two arrays are returned. The first
	 * array contains the names of local variables visible at the current
	 * scope, and the second array contains the associated fully qualified
	 * type names of the local variables.
	 * <p>
	 * Local variable information can be optionally be provided when an insertion
	 * position is unknown, but local variable information is known.
	 * </p>
	 * 
	 * @return arrays of variable names and fully qualified type names of local variables
	 *   visible at the current scope
	 * @throws CoreException if an exception occurs determining local variable
	 *  information
	 */
	public String[][] getLocalVariables() throws CoreException;
	
	/**
	 * Returns whether the current scope is in a static context.
	 * 
	 * @return whether the current scope is in a static context
	 * @throws CoreException if an exception occurs while determining scope
	 */
	public boolean isStatic() throws CoreException;
	
	/**
	 * Returns the snippet on which code completion is should be performed, given the
	 * snippet that is currently being edited. Allows implementations to perform any
	 * special pre-processing on the snippet.
	 * 
	 * @param snippet the snippet in source viewer on which completion is being performed
	 * @return the snippet on which to perform code completion
	 * @throws CoreException
	 */
	public String getSnippet(String snippet) throws CoreException;
}
