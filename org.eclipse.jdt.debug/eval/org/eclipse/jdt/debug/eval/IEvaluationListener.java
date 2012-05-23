/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.eval;

/**
 * Evaluation results are reported to evaluation listeners on the completion of
 * an evaluation. The evaluation may fail but a result will be supplied
 * indicating the problems.
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @see IEvaluationResult
 * @since 2.0
 */

public interface IEvaluationListener {

	/**
	 * Notifies this listener that an evaluation has completed, with the given
	 * result.
	 * 
	 * @param result
	 *            The result from the evaluation
	 * @see IEvaluationResult
	 */
	public void evaluationComplete(IEvaluationResult result);
}
