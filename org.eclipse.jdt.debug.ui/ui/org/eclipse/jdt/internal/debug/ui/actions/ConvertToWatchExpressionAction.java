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

import java.util.Iterator;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.jdt.internal.debug.ui.JavaWatchExpression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Convert one or more expressions to the equivalent watch expressions.
 * Refresh and re-evaluate the expressions if possible.
 */
public class ConvertToWatchExpressionAction extends WatchExpressionAction {

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection= getCurrentSelection();
		IExpressionManager expressionManager= DebugPlugin.getDefault().getExpressionManager();
		for (Iterator iter= selection.iterator(); iter.hasNext();) {
			IExpression expression= (IExpression) iter.next();
			// create the new watch expression
			JavaWatchExpression watchExpression= new JavaWatchExpression(expression.getExpressionText());
			expressionManager.removeExpression(expression);
			expressionManager.addExpression(watchExpression);
			// refresh and re-evaluate
			watchExpression.evaluateExpression(getStackFrameContext());
		}
	}

}
