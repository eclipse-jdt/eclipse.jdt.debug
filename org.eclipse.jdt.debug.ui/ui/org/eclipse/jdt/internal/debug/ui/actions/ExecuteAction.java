package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
import org.eclipse.ui.IWorkbenchPart;

public class ExecuteAction extends EvaluateAction {

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.EvaluateAction#displayResult(org.eclipse.jdt.debug.eval.IEvaluationResult)
	 */
	protected void displayResult(IEvaluationResult result) {
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.EvaluateAction#run()
	 */
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
	protected IDataDisplay getDataDisplay() {
		return super.getDirectDataDisplay();
	}

}
