package org.eclipse.jdt.internal.debug.ui;

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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher;
import org.eclipse.ui.IActionFilter;

public class JavaWatchExpressionActionFilter implements IActionFilter {

	public boolean testAttribute(Object target, String name, String value) {
		if (target instanceof JavaWatchExpression) {
			if (name.equals("ReevaluateWatchExpressionActionFilter") && value.equals("availableJavaDebugTarget")) { //$NON-NLS-1$ //$NON-NLS-2$
				return availableJavaDebugTarget();
			}
		}
		return false;
	}
	
	private boolean availableJavaDebugTarget() {
		IAdaptable context = DebugUITools.getDebugContext();
		if (context instanceof IThread) {
			try {
				context = ((IThread)context).getTopStackFrame();
			} catch (DebugException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		if (context != null) {
			IJavaStackFrame frame = (IJavaStackFrame) context.getAdapter(IJavaStackFrame.class);
			if (frame != null) {
				if (frame.getLaunch().getAttribute(ScrapbookLauncher.SCRAPBOOK_LAUNCH) == null) {
					return true;
				}
			}
		}
		return false;
	}

}
