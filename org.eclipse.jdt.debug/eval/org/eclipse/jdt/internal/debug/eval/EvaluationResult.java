package org.eclipse.jdt.internal.debug.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
 
/**
 * The result of an evaluation.
 * 
 * @see org.eclipse.jdt.debug.eval.IEvaluationResult
 */
public class EvaluationResult implements IEvaluationResult {
	
	/**
	 * The result of an evaluation, possibly <code>null</code>
	 */
	private IJavaValue fValue;
	
	/**
	 * Thread in which the associated evaluation was
	 * executed.
	 */
	private IJavaThread fThread;
	
	/**
	 * Evaluation engine that created this result
	 */
	private IEvaluationEngine fEngine;
	
	/**
	 * Source that was evaluated.
	 */
	private String fSnippet;
	
	/**
	 * Exception that occurred during evaluation,
	 * or <code>null</code> if none.
	 */
	private DebugException fException;
	
	/**
	 * List of <code>Message</code>s describing compilation
	 * problems.
	 */
	private List fErrors;

	/**
	 * Constructs a new evaluation result for the given
	 * engine, thread, and code snippet.
	 */
	public EvaluationResult(IEvaluationEngine engine, String snippet, IJavaThread thread) {
		setEvaluationEngine(engine);
		setThread(thread);
		setSnippet(snippet);
		fErrors= new ArrayList();
	}

	/**
	 * @see IEvaluationResult#getValue()
	 */
	public IJavaValue getValue() {
		return fValue;
	}
	
	/**
	 * Sets the result of an evaluation, possibly
	 * <code>null</code>.
	 * 
	 * @param value result of an evaluation, possibly
	 * 	<code>null</code>
	 */
	public void setValue(IJavaValue value) {
		fValue = value;
	}	

	/**
	 * @see IEvaluationResult#hasProblems()
	 */
	public boolean hasErrors() {
		return getErrors().length > 0 || getException() != null;
	}

	/**
	 * @see IEvaluationResult#getProblems()
	 */
	public Message[] getErrors() {
		return (Message[])fErrors.toArray(new Message[fErrors.size()]);
	}
	/**
	 * @see IEvaluationResult#getSnippet()
	 */
	public String getSnippet() {
		return fSnippet;
	}
	
	/**
	 * Sets the code snippet that was evaluated.
	 * 
	 * @param snippet the source code that was evaluated
	 */
	private void setSnippet(String snippet) {
		fSnippet = snippet;
	}

	/**
	 * @see IEvaluationResult#getException()
	 */
	public DebugException getException() {
		return fException;
	}
	
	/**
	 * Sets an exception that occurred while attempting
	 * the associated evaluation.
	 * 
	 * @param e exception
	 */
	public void setException(DebugException e) {
		fException = e;
	}

	/**
	 * @see IEvaluationResult#getThread()
	 */
	public IJavaThread getThread() {
		return fThread;
	}
	
	/**
	 * Sets the thread this result was generated
	 * from.
	 * 
	 * @param thread thread in which the associated
	 *   evaluation was executed
	 */
	private void setThread(IJavaThread thread) {
		fThread= thread;
	}

	/**
	 * @see IEvaluationResult#getEvaluationEngine()
	 */
	public IEvaluationEngine getEvaluationEngine() {
		return fEngine;
	}
	
	/**
	 * Sets the evaluation that created this result.
	 * 
	 * @param engine the evaluation that created this result
	 */
	private void setEvaluationEngine(IEvaluationEngine engine) {
		fEngine = engine;
	}
	
	/**
	 * Adds the given problem with associated kind and
	 * source fragment to this result.
	 * 
	 * @param marker marker describing compilation error/warning
	 * @param kind 	 the kind of problem, indicating if a problem is
	 *   about a global variable, a code snippet, an import, or a
	 *   package declaration. The value is one of <code>ICodeSnippetRequestor.VARIABLE</code>, 
	 *   <code>ICodeSnippetRequestor.CODE_SNIPPET</code>, <code>ICodeSnippetRequestor.IMPORT</code>
	 *   or <code>ICodeSnippetRequestor.PACKAGE</code>.
	 * @param fragment 	 the source fragment for the problem. If a problem
	 *   is about a global variable, the corresponding source fragment
	 *   is the name of the variable. If a problem is about a code snippet,
	 *   the source fragment is the code snippet. If a problem is about an
	 *   import, the source fragment is the import. If a problem is about a
	 *   package declaration, the source fragment is the package declaration.
	 */
	public void addError(Message message) {
		fErrors.add(message);
	}
}

