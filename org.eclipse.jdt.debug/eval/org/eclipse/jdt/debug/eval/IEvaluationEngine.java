package org.eclipse.jdt.debug.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.eval.ast.model.ICompiledExpression;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * An evaluation engine performs an evalutaion of a
 * code snippet in a specified thread of a debug target.
 * An evaluation engine is associated with a specific
 * debug target and Java project at creation.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IEvaluationResult
 * @see IEvaluationListener
 * @since 2.0
 */

public interface IEvaluationEngine {
	/**
	 * Asynchronously evaluates the given snippet in the specified
	 * target thread, reporting the result back to the given listener.
	 * The snippet is evaluated in the context of the Java
	 * project this evaluation engine was created on. If the
	 * snippet is determined to be a valid expression, the expression
	 * is evaluated in the specified thread, which resumes its
	 * execution from the location at which it is currently suspended.
	 * When the evaluation completes, the thread will be suspened
	 * at this original location.
	 * 
	 * @param snippet code snippet to evaluate
	 * @param thread the thread in which to run the evaluation,
	 *   which must be suspended
	 * @param listener the listener that will receive notification
	 *   when/if the evalaution completes
	 * @exception DebugException if this method fails.  Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The specified thread is not currently suspended</li>
	 * <li>The specified thread is not contained in the debug target
	 *   associated with this evaluation engine</li>
	 * <li>The specified thread is suspended in the middle of
	 *  an evaluation that has not completed. It is not possible
	 *  to perform nested evaluations</li>
	 * </ul>
	 */
	void evaluate(String snippet, IJavaThread thread, IEvaluationListener listener, long timeout) throws DebugException;
	/**
	 * Asynchronously evaluates the given snippet in the context of
	 * the specified stack frame, reporting the result back to the given listener.
	 * The snippet is evaluated in the context of the Java
	 * project this evaluation engine was created on. If the
	 * snippet is determined to be a valid expression, the expression
	 * is evaluated in the thread associated with the given
	 * stack frame. The thread is resumed from the location at which it
	 * is currently suspended. When the evaluation completes, the thread
	 * will be suspened at this original location.
	 * 
	 * @param snippet code snippet to evaluate
	 * @param frame the stack frame context in which to run the
	 *   evaluation.
	 * @param listener the listener that will receive notification
	 *   when/if the evalaution completes
	 * @exception DebugException if this method fails.  Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The associated thread is not currently suspended</li>
	 * <li>The stack frame is not contained in the debug target
	 *   associated with this evaluation engine</li>
	 * <li>The associated thread is suspended in the middle of
	 *  an evaluation that has not completed. It is not possible
	 *  to perform nested evaluations</li>
	 * </ul>
	 */
	void evaluate(String snippet, IJavaStackFrame frame, IEvaluationListener listener, long timeout) throws DebugException;
	/**
	 * Asynchronously evaluates the given snippet in the context of
	 * the specified type, reporting the result back to the given listener.
	 * The snippet is evaluated in the context of the Java
	 * project this evaluation engine was created on. If the
	 * snippet is determined to be a valid expression, the expression
	 * is evaluated in the thread associated with the given
	 * stack frame. The thread is resumed from the location at which it
	 * is currently suspended. When the evaluation completes, the thread
	 * will be suspened at this original location.
	 * 
	 * @param snippet code snippet to evaluate
	 * @param thisContext the 'this' context for the evaluation
	 * @param thread the thread in which to run the evaluation,
	 *   which must be suspended
	 * @param listener the listener that will receive notification
	 *   when/if the evalaution completes
	 * @exception DebugException if this method fails.  Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The associated thread is not currently suspended</li>
	 * <li>The stack frame is not contained in the debug target
	 *   associated with this evaluation engine</li>
	 * <li>The associated thread is suspended in the middle of
	 *  an evaluation that has not completed. It is not possible
	 *  to perform nested evaluations</li>
	 * </ul>
	 */
	void evaluate(String snippet, IJavaObject thisContext, IJavaThread thread, IEvaluationListener listener, long timeout) throws DebugException;

	/**
	 * Asynchronously evaluates the given expression in the context of
	 * the specified stack frame, reporting the result back to the given listener.
	 * The expression is evaluated in the context of the Java
	 * project this evaluation engine was created on. If the
	 * expression is determined to have no errors, the expression
	 * is evaluated in the thread associated with the given
	 * stack frame. The thread is resumed from the location at which it
	 * is currently suspended. When the evaluation completes, the thread
	 * will be suspened at this original location.
	 * 
	 * @param snippet code snippet to evaluate
	 * @param frame the stack frame context in which to run the
	 *   evaluation.
	 * @param listener the listener that will receive notification
	 *   when/if the evalaution completes
	 * @exception DebugException if this method fails.  Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The associated thread is not currently suspended</li>
	 * <li>The stack frame is not contained in the debug target
	 *   associated with this evaluation engine</li>
	 * <li>The associated thread is suspended in the middle of
	 *  an evaluation that has not completed. It is not possible
	 *  to perform nested evaluations</li>
	 * </ul>
	 */
	void evaluateExpression(ICompiledExpression expression, IJavaStackFrame frame, IEvaluationListener listener, long timeout) throws DebugException;
	/**
	 * Asynchronously evaluates the given expression in the specified
	 * target thread, reporting the result back to the given listener.
	 * The expression is evaluated in the context of the Java
	 * project this evaluation engine was created on. If the
	 * expression is determined to have no errors, the expression
	 * is evaluated in the thread associated with the given
	 * stack frame. When the evaluation completes, the thread will be suspened
	 * at this original location.
	 * 
	 * @param expression the expression to evaluate
	 * @param thread the thread in which to run the evaluation,
	 *   which must be suspended
	 * @param listener the listener that will receive notification
	 *   when/if the evalaution completes
	 * @exception DebugException if this method fails.  Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The specified thread is not currently suspended</li>
	 * <li>The specified thread is not contained in the debug target
	 *   associated with this evaluation engine</li>
	 * <li>The specified thread is suspended in the middle of
	 *  an evaluation that has not completed. It is not possible
	 *  to perform nested evaluations</li>
	 * </ul>
	 */
	void evaluateExpression(ICompiledExpression expression, IJavaThread thread, IEvaluationListener listener, long timeout) throws DebugException;
	/**
	 * Asynchronously evaluates the given expression in the context of
	 * the specified type, reporting the result back to the given listener.
	 * The expression is evaluated in the context of the Java
	 * project this evaluation engine was created on.  If the
	 * expression is determined to have no errors, the expression
	 * is evaluated in the thread associated with the given
	 * stack frame. When the evaluation completes, the thread
	 * will be suspened at this original location.
	 * 
	 * @param expression the expression to evaluate
	 * @param thisContext the 'this' context for the evaluation
	 * @param thread the thread in which to run the evaluation,
	 *   which must be suspended
	 * @param listener the listener that will receive notification
	 *   when/if the evalaution completes
	 * @exception DebugException if this method fails.  Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The associated thread is not currently suspended</li>
	 * <li>The stack frame is not contained in the debug target
	 *   associated with this evaluation engine</li>
	 * <li>The associated thread is suspended in the middle of
	 *  an evaluation that has not completed. It is not possible
	 *  to perform nested evaluations</li>
	 * </ul>
	 */
	void evaluateExpression(ICompiledExpression expression, IJavaObject object, IJavaThread thread, IEvaluationListener listener, long timeout) throws DebugException;

	/**
	 * Synchronously generates a compiled expression from the given snippet
	 * in the context of the specified stack frame. The generated expression
	 * can be stored and evaluated later in a valid runtime context.
	 */
	ICompiledExpression getCompiledExpression(String snippet, IJavaStackFrame frame) throws DebugException;
	/**
	 * Synchronously generates a compiled expression from the given snippet
	 * in the context of the specified stack frame. The generated expression
	 * can be stored and evaluated later in a valid runtime context.
	 */
	ICompiledExpression getCompiledExpression(String snippet, IJavaThread thread) throws DebugException;
	/**
	 * Synchronously generates a compiled expression from the given snippet
	 * in the context of the specified stack frame. The generated expression
	 * can be stored and evaluated later in a valid runtime context.
	 */
	ICompiledExpression getCompiledExpression(String snippet, IJavaObject object, IJavaThread thread) throws DebugException;

	/**
	 * Returns the Java project in which snippets are
	 * compliled.
	 * 
	 * @return Java project context
	 */
	IJavaProject getJavaProject();
	
	/**
	 * Returns the debug target for which evaluations
	 * are executed.
	 * 
	 * @return Java debug target
	 */
	IJavaDebugTarget getDebugTarget();
	
	/**
	 * Disposes this evaluation engine. An engine cannot
	 * be used after it has been disposed.
	 */
	void dispose();
	
}

