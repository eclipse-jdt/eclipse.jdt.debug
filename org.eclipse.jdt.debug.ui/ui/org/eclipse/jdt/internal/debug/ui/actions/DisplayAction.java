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
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;
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
	
	private Object fLock= new Object();
	
	/**
	 * The debug model presentation used for computing toString
	 */
	private IDebugModelPresentation fPresentation= DebugUITools.newDebugModelPresentation(JDIDebugModel.getPluginIdentifier());
	/**
	 * The result of a toString evaluation returned asynchronously by the
	 * debug model.
	 */
	private String fResult;
	
	/**
	 * Reports errors to the user via the normal means (@see EvaluateAction#reportErrors(IEvaluationResult))
	 * and displays a failed evaluation message in the display view.
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
		final IDataDisplay dataDisplay= getDataDisplay();
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
		
		if (dataDisplay != null) {
			final String finalString= resultString;
			final Display display= JDIDebugUIPlugin.getStandardDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					if (display.isDisposed()) {
						return;
					}
					dataDisplay.displayExpressionValue(finalString);
				}
			});
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
	protected String evaluateToString(IJavaValue value) throws DebugException {
		fResult= null;
		fPresentation.computeDetail(value, this);
		synchronized (fLock) {
			if (fResult == null) {
				try {
					fLock.wait(20000);
				} catch (InterruptedException e) {
					return ActionMessages.getString("DisplayAction.toString_interrupted"); //$NON-NLS-1$
				}
			}
		}
		return fResult;
	}
	
	/**
	 * @see IValueDetailListener#detailComputed(IValue, String)
	 */
	public void detailComputed(IValue value, final String result) {
		fResult= result;
		synchronized (fLock) {
			fLock.notifyAll();
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
}