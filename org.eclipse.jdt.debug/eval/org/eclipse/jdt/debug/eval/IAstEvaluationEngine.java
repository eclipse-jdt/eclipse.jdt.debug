package org.eclipse.jdt.debug.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * An evaluation engine that performs evaluations by
 * interpretting abstract syntax trees. An AST evalutaion engine
 * is capable of creating compiled expressions that can be
 * evaluated multiple times in a given runtime context.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @since 2.0
 */ 
public interface IAstEvaluationEngine extends IEvaluationEngine {

	/**
	 * Asynchronously evaluates the given expression in the context of
	 * the specified stack frame, reporting the result back to the given listener.
	 * The thread is resumed from the location at which it
	 * is currently suspended to perform the evaluation. When the evaluation
	 * completes, the thread will be suspened at this original location.
	 * Compilation and runtime errors are reported in the evaluation result.
	 * 
	 * @param expression expression to evaluate
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
	public void evaluateExpression(ICompiledExpression expression, IJavaStackFrame frame, IEvaluationListener listener) throws DebugException;

	/**
	 * Asynchronously evaluates the given expression in the context of
	 * the specified type, reporting the result back to the given listener.
	 * The expression is evaluated in the context of the Java
	 * project this evaluation engine was created on.  If the
	 * expression is determined to have no errors, the expression
	 * is evaluated in the thread associated with the given
	 * stack frame. When the evaluation completes, the thread
	 * will be suspened at this original location.
	 * Compilation and runtime errors are reported in the evaluation result.
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
	public void evaluateExpression(ICompiledExpression expression, IJavaObject object, IJavaThread thread, IEvaluationListener listener) throws DebugException;

	/**
	 * Synchronously generates a compiled expression from the given expression
	 * in the context of the specified stack frame. The generated expression
	 * can be stored and evaluated later in a valid runtime context.
	 * Compilation errors are reported in the returned compiled expression.
	 * 
	 * @param expression expression to compile
	 * @param frame the context in which to compile the expression
	 * @exception DebugException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The associated thread is not currently suspended</li>
	 * <li>The stack frame is not contained in the debug target
	 *   associated with this evaluation engine</li>
	 * </ul>
	 */
	public ICompiledExpression getCompiledExpression(String expression, IJavaStackFrame frame) throws DebugException;
	
	/**
	 * Synchronously generates a compiled expression from the given expression
	 * in the context of the specified object. The generated expression
	 * can be stored and evaluated later in a valid runtime context.
	 * Compilation errors are reported in the returned compiled expression.
	 * 
	 * @param expression expression to compile
	 * @param object the context in which to compile the expression
	 * @exception DebugException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The associated thread is not currently suspended</li>
	 * <li>The stack frame is not contained in the debug target
	 *   associated with this evaluation engine</li>
	 * </ul>
	 */
	public ICompiledExpression getCompiledExpression(String expression, IJavaObject object) throws DebugException;

	
}

