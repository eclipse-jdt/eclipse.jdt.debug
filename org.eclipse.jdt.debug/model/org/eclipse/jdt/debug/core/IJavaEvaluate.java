package org.eclipse.jdt.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.eval.IEvaluationContext;

/**
 * The capability to perform an evaluation, given a Java project context.
 * This interface is supported by Java stack frames and Java threads. 
 * An evaluation performed in a stack frame has access to local variables
 * and "this". A Java project or an evaluation context may be provided as a context
 * for an evaluition. When a Java project is specified, a new evaluation context
 * is created for that project.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IJavaEvaluationListener
 * @see IJavaEvaluationResult
 * @see IJavaValue
 * @see IEvaluationContext
 */


public interface IJavaEvaluate {

	/**
	 * Evaluates the given expression, in the context of the
	 * specified Java project and this Java debug element, reporting the result
	 * to the given listener. The thread associated with this element must be
	 * currently suspended and not already in an evaluation. The evaluation
	 * will be performed in the associated thread, effectively resuming the thread until
	 * the evaluation completes, or is inadvertently suspended (by a breakpoint,
	 * exception, etc.). An evaluation may complete with errors. In this case
	 * the evaluation result will contain problem markers describing the
	 * reason for the failure.
	 *
	 * @param expression the expression to evaluate
	 * @param listener the object that will be notified of the evaluation result
	 * @param project the Java project context to use when evaluating/compiling
	 *    the expression
	 * @exception DebugException on failure. Reasons include:<ul>
	 * <li>TARGET_REQUEST_FAILED - The request failed in the target</li>
	 * <li>NOT_SUPPORTED - The capability is not supported by the target</li>
	 * </ul>
	 * @see IJavaEvaluationListener
	 */
	void evaluate(String expression, IJavaEvaluationListener listener, IJavaProject project) throws DebugException;

      /**
	 * Evaluates the given expression, in the context of this Java debug
	 * element and the given evaluation context, reporting the result
	 * to the given listener. The thread associated with this element must be
	 * currently suspended and not already in an evaluation. The evaluation
	 * will be performed in the associated thread, effectively resuming the
	 * thread until the evaluation completes, or is inadvertently suspended
	 * (by a breakpoint, exception, etc.). An evaluation may complete with errors.
	 * In this case the evaluation result will contain problem markers describing
	 * the reason for the failure.
	 *
	 * @param expression the expression to evaluate
	 * @param listener the object that will be notified of the evaluation result
	 * @param context the evaluation context to use when evaluating/compiling
	 *    the expression
	 * @exception DebugException on failure. Reasons include:<ul>
	 * <li>TARGET_REQUEST_FAILED - The request failed in the target</li>
	 * <li>NOT_SUPPORTED - The capability is not supported by the target</li>
	 * </ul>
	 * @see IJavaEvaluationListener
	 * @see IEvaluationContext
	 */
	void evaluate(String expression, IJavaEvaluationListener listener, IEvaluationContext context) throws DebugException;

	/**
	 * Returns whether this element can currently perform an evaluation.
	 * An evaluation can only be performed if the thread associated with
	 * this element is suspended, and is not currently in an evaluation (i.e
	 * nested evaluations are not supported).
	 *
	 * @return whether this element can currently perform an evaluation
	 */
	boolean canPerformEvaluation();
}