package org.eclipse.jdt.internal.debug.ui.display;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.IHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Displays the result of an evaluation in the display view
 */
public class DisplayAction extends EvaluateAction {
	
	public DisplayAction() {
		setText(DisplayMessages.getString("Display.label")); //$NON-NLS-1$
		setToolTipText(DisplayMessages.getString("Display.tooltip")); //$NON-NLS-1$
		setDescription(DisplayMessages.getString("Display.description")); //$NON-NLS-1$
		JavaDebugImages.setToolImageDescriptors(this, "disp_sbook.gif"); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, new Object[] { IHelpContextIds.DISPLAY_ACTION });	
	}
	
	/**
	 * @see IEvaluationListener#evaluationComplete(IEvaluationResult)
	 */
	public void evaluationComplete(final IEvaluationResult result) {
		
		final IJavaValue value= result.getValue();
		
		if (result.hasProblems() || value != null) {
			Display display= Display.getDefault();
			if (display.isDisposed()) {
				return;
			}
			display.asyncExec(new Runnable() {
				public void run() {
					if (result.hasProblems()) {
						boolean severeProblems= reportProblems(result);
						if (severeProblems) {
							IDataDisplay dataDisplay= getDataDisplay();
							if (dataDisplay != null) {
								dataDisplay.displayExpressionValue(DisplayMessages.getString("DisplayAction.(evaluation_failed)_1")); //$NON-NLS-1$
							}
						}
					}
					if (value != null) {
						insertResult(value, result.getThread());
					}
				}
			});
		}
	}
	
	protected void insertResult(IJavaValue result, IJavaThread thread) {
		
		String resultString= " "; //$NON-NLS-1$
		try {
			String sig= null;
			IJavaType type= result.getJavaType();
			if (type != null) {
				sig= type.getSignature();
			}
			if ("V".equals(sig)) { //$NON-NLS-1$
				resultString= DisplayMessages.getString("Display.no_result_value"); //$NON-NLS-1$
			} else {
				if (sig != null) {
					resultString= MessageFormat.format(DisplayMessages.getString("Display.type_name_pattern"), new Object[] { result.getReferenceTypeName() }); //$NON-NLS-1$
				}
				resultString= MessageFormat.format(DisplayMessages.getString("Display.result_pattern"), new Object[] { resultString, evaluateToString(result, thread) }); //$NON-NLS-1$
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
	 * Hook to let snippet editor use it's action
	 */
	protected Class getAdapterClass() {
		return IDisplayAction.class;
	}
	
	/**
	 * Returns the result of evaluating 'toString' on the given
	 * value.
	 * 
	 * @param value object or primitive data type the 'toString'
	 *  is required for
	 * @param thread the thread in which to evaluate 'toString'
	 * @return the result of evaluating toString
	 * @exception DebugException if an exception occurrs during the
	 *  evaluation.
	 */
	protected String evaluateToString(IJavaValue value, IJavaThread thread) throws DebugException {
		if (value instanceof IJavaObject) {
			IJavaValue result = ((IJavaObject)value).sendMessage("toString","()Ljava/lang/String;", null, thread, false); //$NON-NLS-1$ //$NON-NLS-2$
			return result.getValueString();
		} else {
			return value.getValueString();
		}
	}
}