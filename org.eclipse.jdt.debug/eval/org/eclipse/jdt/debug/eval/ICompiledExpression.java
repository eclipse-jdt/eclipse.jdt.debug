/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.eval;


import org.eclipse.jdt.core.dom.Message;

/**
 * A compiled expression can be compiled once and evaluated multiple times
 * in a runtime context.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @see org.eclipse.jdt.debug.eval.IAstEvaluationEngine
 * @since 2.0
 */


public interface ICompiledExpression {
	
	/**
	 * Returns the source snippet from which this compiled expression was created.
	 * 
	 * @return the source snippet from which this compiled expression was created
	 */
	public String getSnippet();
	
	/**
	 * Returns whether this compiled expression has any compilation errors.
	 * 
	 * @return whether this compiled expression has any compilation errors
	 */
	public boolean hasErrors();
	
	/**
	 * Returns any errors which occurred while creating this compiled expression.
	 * 
	 * @return any errors which occurred while creating this compiled expression
	 * @deprecated use getErrorMessages()
	 */
	public Message[] getErrors();
	
	/**
	 * Returns an array of problem messages. Each message describes a problem that
	 * occurred while while creating this compiled expression.
	 *
	 * @return error messages, or an empty array if no errors occurred
	 * @since 2.1
	 */
	public String[] getErrorMessages();
	
}

