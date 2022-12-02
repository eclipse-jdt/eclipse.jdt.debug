/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

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
				@Override
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
					@Override
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
				@Override
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
		ForceReturnRunnable runnable = new ForceReturnRunnable(stackFrame);
		if (stackFrame != null) {
			IWorkbench workbench = PlatformUI.getWorkbench();
			try {
				workbench.getProgressService().busyCursorWhile(runnable);
			} catch (InvocationTargetException e) {
				Status status = new Status(IStatus.WARNING, JDIDebugUIPlugin.getUniqueIdentifier(), "Force return failed", e); //$NON-NLS-1$
				JDIDebugUIPlugin.log(status);
				return;
			} catch (InterruptedException e) {
				// e.g. user cancelled the operation via the modal dialog
				return;
			}
			if (runnable.forceReturnDone) {
				return;
			}
		}
		fTargetFrame = stackFrame;
		// perform evaluation otherwise
		super.run();
	}


	private static class ForceReturnRunnable implements IRunnableWithProgress {

		private final IJavaStackFrame stackFrame;
		private boolean forceReturnDone;

		private ForceReturnRunnable(IJavaStackFrame stackFrame) {
			this.stackFrame = stackFrame;
		}

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			try {
				if (stackFrame != null && stackFrame.isSuspended() && !monitor.isCanceled()) {
					String returnType = Signature.getReturnType(stackFrame.getSignature());
					if (Signature.SIG_VOID.equals(returnType)) {
						// no evaluation required for void methods
						stackFrame.forceReturn(((IJavaDebugTarget) stackFrame.getDebugTarget()).voidValue());
						forceReturnDone = true;
					}
				}
			} catch (DebugException e) {
				JDIDebugUIPlugin.statusDialog(e.getStatus());
				forceReturnDone = true;
			}
		}
	}
}
