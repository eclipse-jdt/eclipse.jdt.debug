package org.eclipse.jdt.debug.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * The result of an evaluation. An evaluation result may
 * contain problems and/or a result value.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IJavaValue
 * @since 2.0
 */

public interface IEvaluationResult {
	
	/**
	 * Returns the value representing the result of the
	 * evaluation. Returns <code>null</code> if the
	 * associated evaluation failed, or if the result
	 * of the evaluation was <code>null</code>. If
	 * the associated evaluation failed, there will
	 * be problems, or an exception in this result.
	 *
	 * @return the resulting value, possibly
	 * <code>null</code>
	 */
	IJavaValue getValue();
	
	/**
	 * Returns whether this evaluation had any problems
	 * or if an exception occurred while performing the
	 * evaluation.
	 *
	 * @return whether there were any problems.
	 * @see #getErrors()
	 * @see #getException()
	 */
	boolean hasErrors();
	
	/**
	 * Returns an array of problem messages. Each message describes a problem that
	 * occurred while compiling the snippet.
	 *
	 * @return compilation error messages, or an empty array if no errors occurred
	 */
	Message[] getErrors();
	
	/**
	 * Adds the given error to the collection of compilation errors
	 * associated with this result.
	 */
	void addError(Message error);
	
	/**
	 * Returns the snippet that was evaluated.
	 *
	 * @return The string code snippet.
	 */
	String getSnippet();
	
	/**
	 * Returns any exception that occurred while performing the evaluation
	 * or <code>null</code> if an exception did not occur.
	 * The exception will be a debug exception or a debug exception
	 * that wrappers a JDI exception that indicates a problem communicating
	 * with the target or with actually performing some action in the target.
	 *
	 * @return The exception that occurred during the evaluation
	 * @see com.sun.jdi.InvocationException
	 * @see org.eclipse.debug.core.DebugException
	 */
	DebugException getException();
	
	/**
	 * Returns the thread in which the evaluation was performed.
	 * 
	 * @return The thread in which the evaluation was performed
	 */
	IJavaThread getThread();
	
	/**
	 * Returns the evaluation engine used to evaluate the original
	 * snippet.
	 * 
	 * @return The evaluation engine used to evaluate the
	 *  original snippet
	 */
	IEvaluationEngine getEvaluationEngine();	
}