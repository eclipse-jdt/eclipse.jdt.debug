package org.eclipse.jdt.debug.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
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
	 */
	public Message[] getErrors();
	
}

