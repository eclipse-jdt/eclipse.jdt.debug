/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.display;

 
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IEvaluationResult;

/**
 * An implementation of an expression produced from the
 * inspect action. An inspect expression removes
 * itself from the expression manager when its debug
 * target terminates.
 */
public class JavaInspectExpression extends PlatformObject implements IErrorReportingExpression, IDebugEventSetListener {
	
	/**
	 * The value of this expression
	 */
	private IJavaValue fValue;
	
	/**
	 * The code snippet for this expression.
	 */
	private String fExpression;
	
	private IEvaluationResult fResult;

	/**
	 * Constucts a new inspect result for the given
	 * expression and resulting value. Starts listening
	 * to debug events such that this element will remove
	 * itself from the expression manager when its debug
	 * target terminates.
	 * 
	 * @param expression code snippet
	 * @param value value of the expression
	 */
	public JavaInspectExpression(String expression, IJavaValue value) {
		fValue = value;
		fExpression = expression;
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	/**
	 * Constucts a new inspect result for the given
	 * evaluation result, which provides a snippet, value,
	 * and error messages, if any.
	 * 
	 * @param result the evaluation result
	 */
	public JavaInspectExpression(IEvaluationResult result) {
		this(result.getSnippet(), result.getValue());
		fResult= result;
	}
	
	/**
	 * @see IExpression#getExpressionText()
	 */
	public String getExpressionText() {
		return fExpression;
	}

	/**
	 * @see IExpression#getValue()
	 */
	public IValue getValue() {
		return fValue;
	}

	/**
	 * @see IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		IValue value= getValue();
		if (value != null) {
			return getValue().getDebugTarget();
		}
		if (fResult != null) {
			return fResult.getThread().getDebugTarget();
		}
		// An expression should never be created with a null value *and*
		// a null result.
		return null;
	}

	/**
	 * @see IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return getDebugTarget().getModelIdentifier();
	}

	/**
	 * @see IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return getDebugTarget().getLaunch();
	}

	/**
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.TERMINATE && event.getSource().equals(getDebugTarget())) {
				DebugPlugin.getDefault().getExpressionManager().removeExpression(this);
			}
		}
	}

	/**
	 * @see IExpression#dispose()
	 */
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);		
	}

	/**
	 * @see org.eclipse.debug.core.model.IErrorReportingExpression#hasErrors()
	 */
	public boolean hasErrors() {
		return fResult.hasErrors();
	}

	/**
	 * @sSee org.eclipse.debug.core.model.IErrorReportingExpression#getErrorMessages()
	 */	
	public String[] getErrorMessages() {	
		return fResult.getErrorMessages();
	}
}
