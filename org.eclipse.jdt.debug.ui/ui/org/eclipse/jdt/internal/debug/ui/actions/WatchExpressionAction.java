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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher;

/**
 * Generic abstract class for the actions associated to the java watch
 * expressions.
 */
public abstract class WatchExpressionAction extends ObjectActionDelegate {

	/**
	 * Finds the currently selected stack frame in the UI.
	 * Stack frames from a scrapbook launch are ignored.
	 */
	protected IJavaThread getThreadContext() {
		IAdaptable context = DebugUITools.getDebugContext();
		if (context instanceof IJavaThread) {
			return (IJavaThread)context;
		}
		if (context != null) {
			IJavaStackFrame frame = (IJavaStackFrame) context.getAdapter(IJavaStackFrame.class);
			if (frame != null) {
				if (frame.getLaunch().getAttribute(ScrapbookLauncher.SCRAPBOOK_LAUNCH) == null) {
					return (IJavaThread)frame.getThread();
				}
			}
		}
		return null;
	}
}
