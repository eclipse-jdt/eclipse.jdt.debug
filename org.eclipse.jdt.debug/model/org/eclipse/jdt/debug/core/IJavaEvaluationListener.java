package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Evaluation results are reported to evaluation listeners.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IValue
 * @see IJavaEvaluationResult
 */

public interface IJavaEvaluationListener {
	
	/**
	 * Notifies this listener that an evaluation is complete, with the
	 * given result.
	 * 
	 * @see IJavaEvaluationResult
	 */
	void evaluationComplete(IJavaEvaluationResult result);
	
}