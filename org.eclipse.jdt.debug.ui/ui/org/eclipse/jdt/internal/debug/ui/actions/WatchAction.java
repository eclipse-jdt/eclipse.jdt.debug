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
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaWatchExpression;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Create a watch item from a selected variable
 */
public class WatchAction extends InspectAction {
		
	public void run() {
		Object selectedObject= getSelectedObject();
		if (selectedObject instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection)selectedObject;
			Iterator elements = selection.iterator();
			while (elements.hasNext()) {
				try {
					createWatchExpression(((IJavaVariable)elements.next()).getName());
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e);
					return;
				}
			}
			showExpressionView();
		} else if (selectedObject instanceof String) {
			createWatchExpression((String) selectedObject);
			showExpressionView();
		}
	}

	private void createWatchExpression(String snippet) {
		JavaWatchExpression expression = new JavaWatchExpression(snippet);
		IThread thread = getStackFrameContext().getThread();
		if (thread instanceof IJavaThread) {
			expression.evaluateExpression((IJavaThread)thread);
		}
		DebugPlugin.getDefault().getExpressionManager().addExpression(expression);				
	}

}
