package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaWatchExpression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;

/**
 * Create a watch item from a selected variable
 */
public class WatchVariableAction extends InspectAction implements IViewActionDelegate, IActionDelegate2 {
		
	private IStructuredSelection fSelection = null;

	/**
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
		fSelection = null;
		super.dispose();
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (fSelection != null) {
			Iterator elements = fSelection.iterator();
			while (elements.hasNext()) {
				IJavaVariable variable = (IJavaVariable)elements.next();
				JavaWatchExpression expression = null;
				try {
					expression = new JavaWatchExpression(variable.getName());
					IStackFrame frame = getStackFrameContext();
					if (frame instanceof IJavaStackFrame) {
						expression.evaluateExpression((IJavaStackFrame)frame);
					}
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e);
					return;
				}
				DebugPlugin.getDefault().getExpressionManager().addExpression(expression);				
			}
			if (getTargetPart().getSite().getId().equals(IDebugUIConstants.ID_EXPRESSION_VIEW)) {
				return;
			}
			showExpressionView();
		}
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = null;
		if (selection instanceof IStructuredSelection) {
			fSelection = (IStructuredSelection)selection;
		}
		super.selectionChanged(action, selection);
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

}
