package org.eclipse.jdt.debug.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Evaluation results are reported to evaluation listeners 
 * on the completion of an evaluation.  The evaluation
 * may fail but a result will be supplied indicating the
 * problems.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IEvaluationResult
 * @since 2.0
 */

public interface IEvaluationListener {
	
	/**
	 * Notifies this listener that an evaluation has completed, with the
	 * given result.
	 * 
	 * @param result The result from the evaluation
	 * @see IEvaluationResult
	 */
	public void evaluationComplete(IEvaluationResult result);
}