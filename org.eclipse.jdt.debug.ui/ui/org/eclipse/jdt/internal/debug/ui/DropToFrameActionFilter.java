package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.ui.IActionFilter;

public class DropToFrameActionFilter implements IActionFilter {

	/**
	 * @see org.eclipse.ui.IActionFilter#testAttribute(Object, String, String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		if (name.equals("DropToFrameActionFilter") //$NON-NLS-1$
			&& value.equals("supportsDropToFrame")) { //$NON-NLS-1$
			if (target instanceof IJavaStackFrame) {
				IJavaStackFrame frame = (IJavaStackFrame) target;
				return frame.supportsDropToFrame();
			}
		}
		return false;
	}
}
