package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Displays the result of an evaluation in the display view
 */
public class DisplayAction extends EvaluateAction implements IValueDetailListener {
	
	/**
	 * The result of a toString evaluation returned asynchronously by the
	 * debug model.
	 */
	private String fResult;
	
	/**
	 * Reports errors to the user via the normal means (@see EvaluateAction#reportErrors(IEvaluationResult))
	 * and displays a failed evaluation message in the data display.
	 */
	protected void reportErrors(IEvaluationResult result) {
		String message= getErrorMessage(result);
		IDataDisplay dataDisplay= getDataDisplay();
		if (dataDisplay != null) {
			if (message.length() != 0) {
				dataDisplay.displayExpressionValue(MessageFormat.format(ActionMessages.getString("DisplayAction.(evaluation_failed)_Reason"), new String[] {message})); //$NON-NLS-1$
			} else {
				dataDisplay.displayExpressionValue(ActionMessages.getString("DisplayAction.(evaluation_failed)_1")); //$NON-NLS-1$
			}
		}
	}
	
	protected String getErrorMessage(Message[] errors) {
		String message= ""; //$NON-NLS-1$
		for (int i= 0; i < errors.length; i++) {
			Message error= errors[i];
			//more than a warning
			String msg= error.getMessage();
			if (i == 0) {
				message= "\t\t" + msg; //$NON-NLS-1$
			} else {
				message= MessageFormat.format(ActionMessages.getString("DisplayAction.error.problem_append_pattern"), new Object[] { message, msg }); //$NON-NLS-1$
			}
		}
		return message;
	}
	
	/**
	 * @see EvaluateAction#displayResult(IEvaluationResult)
	 */
	protected void displayResult(IEvaluationResult result) {
		IJavaValue value= result.getValue();
		String resultString= " "; //$NON-NLS-1$
		try {
			String sig= null;
			IJavaType type= value.getJavaType();
			if (type != null) {
				sig= type.getSignature();
			}
			if ("V".equals(sig)) { //$NON-NLS-1$
				resultString= ActionMessages.getString("DisplayAction.no_result_value"); //$NON-NLS-1$
			} else {
				if (sig != null) {
					resultString= MessageFormat.format(ActionMessages.getString("DisplayAction.type_name_pattern"), new Object[] { value.getReferenceTypeName() }); //$NON-NLS-1$
				}
				resultString= MessageFormat.format(ActionMessages.getString("DisplayAction.result_pattern"), new Object[] { resultString, evaluateToString(value) }); //$NON-NLS-1$
			}
		} catch(DebugException x) {
			resultString= getExceptionMessage(x);
		}
	
		final String finalString= resultString;
		final Display display= JDIDebugUIPlugin.getStandardDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed()) {
					IDataDisplay dataDisplay= getDataDisplay();
					if (dataDisplay != null) {
						dataDisplay.displayExpressionValue(finalString);
					}
				}
				evaluationCleanup();
			}
		});
	}
	
	/**
	 * Returns the result of evaluating 'toString' on the given
	 * value.
	 * 
	 * @param value object or primitive data type the 'toString'
	 *  is required for
	 * @return the result of evaluating toString
	 * @exception DebugException if an exception occurs during the
	 *  evaluation.
	 */
	protected String evaluateToString(IJavaValue value) throws DebugException {
		setResult(null);
		getDebugModelPresentation().computeDetail(value, this);
		synchronized (this) {
			if (getResult() == null) {
				try {
					wait(20000);
				} catch (InterruptedException e) {
					return ActionMessages.getString("DisplayAction.toString_interrupted"); //$NON-NLS-1$
				}
			}
		}
		return getResult();
	}
	
	/**
	 * @see IValueDetailListener#detailComputed(IValue, String)
	 */
	public void detailComputed(IValue value, String result) {
		setResult(result);
		synchronized (this) {
			notifyAll();
		}
	}
	
	protected void run() {
		IWorkbenchPart part= getTargetPart();
		if (part instanceof JavaSnippetEditor) {
			((JavaSnippetEditor)part).evalSelection(JavaSnippetEditor.RESULT_DISPLAY);
			return;
		}
		super.run();	
	}
	
	protected String getResult() {
		return fResult;
	}

	protected void setResult(String result) {
		fResult = result;
	}
}