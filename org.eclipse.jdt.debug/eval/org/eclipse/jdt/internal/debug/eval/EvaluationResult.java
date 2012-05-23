/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval;

import java.util.ArrayList;
import java.util.Iterator;
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
	 * Thread in which the associated evaluation was executed.
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
	 * Exception that occurred during evaluation, or <code>null</code> if none.
	 */
	private DebugException fException;

	/**
	 * List of <code>String</code>s describing compilation problems.
	 */
	private List<String> fErrors;

	/**
	 * Whether the evaluation was terminated.
	 */
	private boolean fTerminated = false;

	/**
	 * Constructs a new evaluation result for the given engine, thread, and code
	 * snippet.
	 */
	public EvaluationResult(IEvaluationEngine engine, String snippet,
			IJavaThread thread) {
		setEvaluationEngine(engine);
		setThread(thread);
		setSnippet(snippet);
		fErrors = new ArrayList<String>();
	}

	/**
	 * @see IEvaluationResult#getValue()
	 */
	public IJavaValue getValue() {
		return fValue;
	}

	/**
	 * Sets the result of an evaluation, possibly <code>null</code>.
	 * 
	 * @param value
	 *            result of an evaluation, possibly <code>null</code>
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
	 * @deprecated
	 */
	@Deprecated
	public Message[] getErrors() {
		Message[] messages = new Message[fErrors.size()];
		int i = 0;
		for (Iterator<String> iter = fErrors.iterator(); iter.hasNext();) {
			messages[i++] = new Message(iter.next(), -1);
		}
		return messages;
	}

	/**
	 * @see org.eclipse.jdt.debug.eval.IEvaluationResult#getErrorMessages()
	 */
	public String[] getErrorMessages() {
		return fErrors.toArray(new String[fErrors.size()]);
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
	 * @param snippet
	 *            the source code that was evaluated
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
	 * Sets an exception that occurred while attempting the associated
	 * evaluation.
	 * 
	 * @param e
	 *            exception
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
	 * Sets the thread this result was generated from.
	 * 
	 * @param thread
	 *            thread in which the associated evaluation was executed
	 */
	private void setThread(IJavaThread thread) {
		fThread = thread;
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
	 * @param engine
	 *            the evaluation that created this result
	 */
	private void setEvaluationEngine(IEvaluationEngine engine) {
		fEngine = engine;
	}

	/**
	 * Adds the given message to the list of error messages.
	 */
	public void addError(String message) {
		fErrors.add(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.eval.IEvaluationResult#isTerminated()
	 */
	public boolean isTerminated() {
		return fTerminated;
	}

	/**
	 * Sets whether terminated.
	 * 
	 * @param terminated
	 *            whether terminated
	 */
	public void setTerminated(boolean terminated) {
		fTerminated = terminated;
	}
}
