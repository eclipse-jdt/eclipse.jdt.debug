/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action for force return from a method.
 * 
 * @since 3.3
 */
public class ForceReturnAction extends EvaluateAction {
	
	private IJavaStackFrame fTargetFrame = null;

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.EvaluateAction#displayResult(org.eclipse.jdt.debug.eval.IEvaluationResult)
	 */
	@Override
	protected void displayResult(final IEvaluationResult result) {
		evaluationCleanup();
		
		final Display display = JDIDebugUIPlugin.getStandardDisplay();
		// error with evaluation
		if (result.hasErrors()) {
			display.asyncExec(new Runnable() {
				public void run() {
					if (display.isDisposed()) {
						return;
					}
					reportErrors(result);
				}
			});
			return;
		} 

		// force return with the result
		try {
			IJavaStackFrame frame = fTargetFrame;
			IJavaValue value = result.getValue();
			frame.forceReturn(value);
			if (!Signature.SIG_VOID.equals(value)) {
				display.asyncExec(new Runnable() {
					public void run() {
						if (display.isDisposed()) {
							return;
						}
						IWorkbenchPart part = getTargetPart();
						InspectPopupDialog dialog = new InspectPopupDialog(getShell(), getPopupAnchor(getStyledText(part)), null, new JavaInspectExpression(result));
						dialog.open();
					}
				});
			}
		} catch (DebugException e) {
			final IStatus status = e.getStatus();
			display.asyncExec(new Runnable() {
				public void run() {
					if (display.isDisposed()) {
						return;
					}
					JDIDebugUIPlugin.statusDialog(status);
				}
			});
		}
	}

	@Override
	protected void run() {		
		IJavaStackFrame stackFrame= getStackFrameContext();
		if (stackFrame != null) {
			try {
				String returnType = Signature.getReturnType(stackFrame.getSignature());
				if (Signature.SIG_VOID.equals(returnType)) {
					// no evaluation required for void methods
					stackFrame.forceReturn(((IJavaDebugTarget)stackFrame.getDebugTarget()).voidValue());
					return;
				}
			} catch (DebugException e) {
				JDIDebugUIPlugin.statusDialog(e.getStatus());
				return;
			}
		}
		fTargetFrame = stackFrame;
		// perform evaluation otherwise
		super.run();
	}
	
	
	
	

}
