/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Attempts to terminate an evaluation running in an IJavaThread.
 */
public class TerminateEvaluationAction implements IObjectActionDelegate, IDebugEventSetListener {
	
	private IJavaThread fThread;
	private boolean fTerminated;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		if (fThread == null) {
			return;
		}
		DebugPlugin.getDefault().addDebugEventListener(this);
		Thread timerThread= new Thread(new Runnable() {
			public void run() {
				fTerminated= false;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					return;
				}
				if (!fTerminated) {
					fTerminated= true;
					final Display display= JDIDebugUIPlugin.getStandardDisplay();
						display.asyncExec(new Runnable() {
							public void run() {
								MessageDialog dialog = new MessageDialog(display.getActiveShell(), ActionMessages.TerminateEvaluationActionTerminate_Evaluation_1, null, 
									ActionMessages.TerminateEvaluationActionAttempts_to_terminate_an_evaluation_can_only_stop_a_series_of_statements__The_currently_executing_statement__such_as_a_method_invocation__cannot_be_interrupted__2, MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0); 
								dialog.setBlockOnOpen(false);
								dialog.open();
							}
					});
				}
			}
		});
        timerThread.setDaemon(true);
		timerThread.start();
		try {
			fThread.terminateEvaluation();
		} catch (DebugException exception) {
			JDIDebugUIPlugin.statusDialog(exception.getStatus()); 
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection)selection;
			if (ss.isEmpty() || ss.size() > 1) {
				return;
			}
			Object element= ss.getFirstElement();
			if (element instanceof IJavaThread) {
				setThread((IJavaThread)element);
			}
		}
	}
	
	public void setThread(IJavaThread thread) {
		fThread= thread;
	}

	public void handleDebugEvents(DebugEvent[] events) {
		DebugEvent event;
		for (int i= 0, numEvents= events.length; i < numEvents; i++) {
			event= events[i];
			if ((event.getKind() & DebugEvent.SUSPEND)  != 0 && event.getSource() instanceof IJavaThread && event.isEvaluation()) {
				fTerminated= true;
			}
		}
		DebugPlugin.getDefault(). removeDebugEventListener(this);
	}

}
