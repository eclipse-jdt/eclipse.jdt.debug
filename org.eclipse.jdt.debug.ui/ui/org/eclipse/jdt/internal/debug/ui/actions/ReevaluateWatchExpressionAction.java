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

import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.JavaWatchExpression;
import org.eclipse.jface.action.IAction;

/**
 * Ask to re-evaluate one or more watch expressions in the context of the
 * currently selected thread.
 */
public class ReevaluateWatchExpressionAction extends WatchExpressionAction {

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IJavaThread thread= getThreadContext();
		for (Iterator iter= getCurrentSelection().iterator(); iter.hasNext();) {
			((JavaWatchExpression) iter.next()).evaluateExpression(thread);
		}
	}

}
