package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaWatchExpression;
import org.eclipse.jdt.internal.debug.ui.WatchExpressionDialog;
import org.eclipse.jdt.internal.ui.dialogs.StatusDialog;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Open a watch expression dialog and add the created watch expression to the
 * expression view.
 */
public class AddWatchExpressionAction extends WatchExpressionAction implements IViewActionDelegate {

	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		// create a watch expression
		JavaWatchExpression watchExpression= new JavaWatchExpression(""); //$NON-NLS-1$
		// open the watch expression dialog
		if (new WatchExpressionDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), watchExpression, false).open() == StatusDialog.OK) {
			// if OK is selected, add the expression to the expression view and try to evaluate the expression.
			DebugPlugin.getDefault().getExpressionManager().addExpression(watchExpression);
			watchExpression.evaluateExpression(getThreadContext(), true);
		}
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
