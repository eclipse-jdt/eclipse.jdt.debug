/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
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
		IJavaStackFrame stackFrame= getStackFrameContext();
		if (stackFrame != null) {
			IThread thread = stackFrame.getThread();
			if (thread instanceof IJavaThread) {
				expression.evaluateExpression((IJavaThread)thread);
			}
		}
		DebugPlugin.getDefault().getExpressionManager().addExpression(expression);				
	}

}
