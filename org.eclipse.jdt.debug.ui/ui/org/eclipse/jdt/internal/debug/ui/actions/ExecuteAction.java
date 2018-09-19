/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;

public class ExecuteAction extends EvaluateAction {

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.EvaluateAction#displayResult(org.eclipse.jdt.debug.eval.IEvaluationResult)
	 */
	@Override
	protected void displayResult(final IEvaluationResult result) {
		if (result.hasErrors()) {
			final Display display = JDIDebugUIPlugin.getStandardDisplay();
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (display.isDisposed()) {
						return;
					}
					reportErrors(result);
					evaluationCleanup();
				}
			});
		} else {
			evaluationCleanup();
		}
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.EvaluateAction#run()
	 */
	@Override
	protected void run() {
		IWorkbenchPart part= getTargetPart();
		if (part instanceof JavaSnippetEditor) {
			((JavaSnippetEditor)part).evalSelection(JavaSnippetEditor.RESULT_RUN);
			return;
		}
		super.run();
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.EvaluateAction#getDataDisplay()
	 */
	@Override
	protected IDataDisplay getDataDisplay() {
		return super.getDirectDataDisplay();
	}

}
