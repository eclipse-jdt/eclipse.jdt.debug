package org.eclipse.jdt.internal.debug.eval.ast;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.jdt.debug.eval.ast.model.IThread;
import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * A proxy to a Java debug model thread
 */
public class EvaluationThread extends EvaluationElement implements IThread {
	
	/**
	 * Underlying Java debug model thread
	 */
	private IJavaThread fJavaThread;
	
	/**
	 * Constructs an evaluation thread on the given
	 * underlying Java debug model thread.
	 * 
	 * @param thread underlying Java debug model thread
	 * @return a thread used for evaluation
	 */
	protected EvaluationThread(IJavaThread thread) {
		setJavaThread(thread);
	}

	/**
	 * Returns the underlying Java debug model thread.
	 * 
	 * @return the underlying Java debug model thread
	 */
	protected IJavaThread getJavaThread() {
		return fJavaThread;
	}

	/**
	 * Sets the underlying Java debug model thread.
	 * 
	 * @param javaThread the underlying Java debug model thread
	 */
	private void setJavaThread(IJavaThread javaThread) {
		fJavaThread = javaThread;
	}

	/**
	 * @see EvaluationElement#getUnderlyingModelObject()
	 */
	protected Object getUnderlyingModelObject() {
		return getJavaThread();
	}

}

