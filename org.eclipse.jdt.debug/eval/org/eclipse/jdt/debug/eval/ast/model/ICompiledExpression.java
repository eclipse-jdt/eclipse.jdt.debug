package org.eclipse.jdt.debug.eval.ast.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.Message;

/**
 * A compiled expression can be run against an evaluation context.  
 * Expressions are compiled from source snippets, and can be run
 * more than once (avoiding re-compiles), against contexts from the
 * same location in a program (i.e. same receiving type, class, and
 * locals).
 * <p>
 * Clients are <b>not</b> intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */


public interface ICompiledExpression {
	
	/**
	 * Runs this compiled expression in the given context, and retuns
	 * the result.
	 * 
	 * @param context evaluation context
	 */
	IValue evaluate(IRuntimeContext context);
	
	/**
	 * Returns the source snippet from which this compiled expression was created
	 */
	String getSnippet();
	
	/**
	 * Returns whether this compiled expression has any compilation errors
	 */
	boolean hasErrors();
	
	/**
	 * Returns any errors which occurred while creating this compiled expression.
	 */
	Message[] getErrors();
	
	/**
	 * Returns the runtime exception that occurred while evaluating this expression
	 * or <code>null</code> if no exception occurred.
	 */
	CoreException getException();

}

