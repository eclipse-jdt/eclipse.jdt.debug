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
package org.eclipse.jdt.internal.debug.ui.actions;


import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IValueDetailListener;
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
public class DisplayAction extends EvaluateAction {

	/**
	 * @see EvaluateAction#displayResult(IEvaluationResult)
	 */
	protected void displayResult(final IEvaluationResult evaluationResult) {
		if (evaluationResult.hasErrors()) {
			final Display display = JDIDebugUIPlugin.getStandardDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					if (display.isDisposed()) {
						return;
					}
					reportErrors(evaluationResult);
					evaluationCleanup();
				}
			});
			return;
		} 		
		
		final String snippet= evaluationResult.getSnippet();
		IJavaValue resultValue= evaluationResult.getValue();
		try {
			String sig= null;
			IJavaType type= resultValue.getJavaType();
			if (type != null) {
				sig= type.getSignature();
			}
			if ("V".equals(sig)) { //$NON-NLS-1$
				displayStringResult(snippet, ActionMessages.getString("DisplayAction.no_result_value")); //$NON-NLS-1$
			} else {
				final String resultString;
				if (sig != null) {
					resultString= MessageFormat.format(ActionMessages.getString("DisplayAction.type_name_pattern"), new Object[] { resultValue.getReferenceTypeName() }); //$NON-NLS-1$
				} else {
					resultString= ""; //$NON-NLS-1$
				}
				getDebugModelPresentation().computeDetail(resultValue, new IValueDetailListener() {
					public void detailComputed(IValue value, String result) {
						displayStringResult(snippet, MessageFormat.format(ActionMessages.getString("DisplayAction.result_pattern"), new Object[] { resultString, result})); //$NON-NLS-1$
					}
				});
			}
		} catch (DebugException x) {
			displayStringResult(snippet, getExceptionMessage(x));
		}
	}

	protected void displayStringResult(final String snippet,final String resultString) {
		final IDataDisplay directDisplay= getDirectDataDisplay();
		final Display display= JDIDebugUIPlugin.getStandardDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed()) {
					IDataDisplay dataDisplay= getDataDisplay();
					if (dataDisplay != null) {
						if (directDisplay == null) {
							dataDisplay.displayExpression(snippet);
						}
						dataDisplay.displayExpressionValue(resultString);
					}
				}
				evaluationCleanup();
			}
		});
	}

	protected void run() {
		IWorkbenchPart part= getTargetPart();
		if (part instanceof JavaSnippetEditor) {
			((JavaSnippetEditor) part).evalSelection(JavaSnippetEditor.RESULT_DISPLAY);
			return;
		}
		super.run();
	}

}
