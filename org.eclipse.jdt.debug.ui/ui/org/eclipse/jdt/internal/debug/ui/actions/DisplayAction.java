package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Displays the result of an evaluation in the display view
 */
public class DisplayAction extends EvaluateAction implements IValueDetailListener {
	
	/**
	 * The debug model presentation used for computing toString
	 */
	private IDebugModelPresentation fPresentation= DebugUITools.newDebugModelPresentation(JDIDebugModel.getPluginIdentifier());
	/**
	 * The result of a toString evaluation returned asynchronously by the
	 * debug model.
	 */
	private String fResult;
	
	protected boolean reportErrors(IEvaluationResult result) {
		boolean severeProblems= super.reportErrors(result);
		if (severeProblems) {
			IDataDisplay dataDisplay= getDataDisplay();
			if (dataDisplay != null) {
				dataDisplay.displayExpressionValue(ActionMessages.getString("DisplayAction.(evaluation_failed)_1")); //$NON-NLS-1$
			}
		}
		return severeProblems;
	}
	
	/**
	 * @see EvaluateAction#displayResult(IEvaluationResult)
	 */
	protected void displayResult(IEvaluationResult result) {
		IJavaValue value= result.getValue();
		IJavaThread thread= result.getThread();
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
				resultString= MessageFormat.format(ActionMessages.getString("DisplayAction.result_pattern"), new Object[] { resultString, evaluateToString(value, thread) }); //$NON-NLS-1$
			}
		} catch(DebugException x) {
			reportError(x);
		}
		
		IDataDisplay dataDisplay= getDataDisplay();
		if (dataDisplay != null) {
			dataDisplay.displayExpressionValue(resultString);
		}
	}
	
	/**
	 * Returns the result of evaluating 'toString' on the given
	 * value.
	 * 
	 * @param value object or primitive data type the 'toString'
	 *  is required for
	 * @param thread the thread in which to evaluate 'toString'
	 * @return the result of evaluating toString
	 * @exception DebugException if an exception occurs during the
	 *  evaluation.
	 */
	protected synchronized String evaluateToString(IJavaValue value, IJavaThread thread) throws DebugException {
		fPresentation.computeDetail(value, this);
		try {
			wait(20000);
		} catch (InterruptedException e) {
			return ActionMessages.getString("DisplayAction.toString_interrupted"); //$NON-NLS-1$
		}
		return fResult;
	}
	
	/**
	 * @see IValueDetailListener#detailComputed(IValue, String)
	 */
	public synchronized void detailComputed(IValue value, final String result) {
		fResult= result;
		this.notifyAll();	
	}
	
	protected void run() {
		IWorkbenchPart part= getTargetPart();
		if (part instanceof JavaSnippetEditor) {
			((JavaSnippetEditor)part).evalSelection(JavaSnippetEditor.RESULT_DISPLAY);
			return;
		}
		super.run();	
	}
}