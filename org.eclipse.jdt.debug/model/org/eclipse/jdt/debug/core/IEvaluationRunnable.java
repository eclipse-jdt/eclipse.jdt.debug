package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
 
/**
 * A runnable that represents one logical evaluation
 * to be run in a target thread. 
 * <p>
 * Clients are intended to imlpement this interface.
 * </p>
 * 
 * @see org.eclipse.jdt.debug.core.IJavaThread#runEvaluation(IEvaluationRunnable, IProgressMonitor, int)
 * @since 2.0
 */ 
public interface IEvaluationRunnable {
	
	/**
	 * Runs this evaluation in the specified thread, reporting
	 * progress to the given progress monitor.
	 * 
	 * @param thread the thread in which to run the evaluation
	 * @param monitor progress monitor (may be <code>null</code>)
	 * @exception DebugException if an exception occurs during
	 *  the evaluation
	 */
	public abstract void run(IJavaThread thread, IProgressMonitor monitor) throws DebugException;

}
