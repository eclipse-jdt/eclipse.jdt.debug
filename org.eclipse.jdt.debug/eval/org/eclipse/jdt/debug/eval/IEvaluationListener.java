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
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
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
	void evaluationComplete(IEvaluationResult result);
}