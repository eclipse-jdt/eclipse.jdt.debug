package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;

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
 * @see IJavaEvaluate
 * @deprecated evaluation API will no longer be supported. 
 */

public interface IJavaEvaluationResult {
	
	/**
	 * Returns the value representing the result of the
	 * evaluation, or <code>null</code> if no result is available.
	 *
	 * @return the resulting value
	 * @deprecated evaluation API will no longer be supported. 	
	 */
	IJavaValue getValue();
	
	/**
	 * Returns whether this evaluation had any problems
	 * or if an exception occurred while performing the
	 * evaluation.
	 *
	 * @return whether there were any problems.
	 * @see #getProblems
	 * @see #getException
	 * @deprecated evaluation API will no longer be supported. 
	 */
	boolean hasProblems();
	
	/**
	 * Returns an array of problem markers. Each marker describes a problems that
	 * occurred with the evaluation. If a problem regards the type of a variable,
	 * the marker source line number is -1. If the problem regards the name
	 * of a variable, the marker source line number is 0. Otherwise the marker
	 * source line number is relative to the initializer code.
	 *
	 * @return problems, or an empty array if no problems
	 * @deprecated evaluation API will no longer be supported. 	
	 */
	IMarker[] getProblems();
	
	/**
	 * Returns the source fragment for a corresponding problem. If a problem is
	 * about a global variable, the corresponding source fragment
	 * is the name of the variable. If a problem is about a code snippet,
	 * the source fragment is the code snippet. If a problem is about an import,
	 * the source fragment is the import. If a problem is about a
	 * package declaration, the source fragment is the package declaration.
	 * 
	 * @param problem A problem marker returned by <code>getProblems</code>.
	 * @return A source fragment for the problem.
	 * @deprecated evaluation API will no longer be supported. 	
	 */
	String getSourceFragment(IMarker problem);

	/**
	 * Returns the kind of a corresponding problem, indicating if a problem is
	 * about a global variable, a code snippet, an import or a package declaration.
	 * The returned values is one of <code>ICodeSnippetRequestor.VARIABLE</code>, 
	 * <code>ICodeSnippetRequestor.CODE_SNIPPET</code>, <code>ICodeSnippetRequestor.IMPORT</code>
	 * or <code>ICodeSnippetRequestor.PACKAGE</code>.
	 * 
	 * @param problem A problem marker returned by <code>getProblems</code>.
	 * @return a source fragment for the problem
	 * @see org.eclipse.jdt.core.eval.ICodeSnippetRequestor
	 * @deprecated evaluation API will no longer be supported. 	
	 */
	int getKind(IMarker problem);
	
	/**
	 * Returns the snippet that was evaluated.
	 *
	 * @return The string code snippet.
	 * @deprecated evaluation API will no longer be supported. 	
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
	 * @deprecated evaluation API will no longer be supported. 	
	 */
	Throwable getException();
	
	/**
	 * Returns the thread in which the evaluation was performed.
	 * 
	 * @return The thread in which the evaluation was performed
	 * @deprecated evaluation API will no longer be supported. 	
	 */
	IJavaThread getThread();
}