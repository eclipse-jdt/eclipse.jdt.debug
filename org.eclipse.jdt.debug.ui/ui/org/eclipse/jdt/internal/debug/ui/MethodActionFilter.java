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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.ui.IActionFilter;

public class MethodActionFilter implements IActionFilter {

	/**
	 * @see org.eclipse.ui.IActionFilter#testAttribute(Object, String, String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		if (name.equals("MethodActionFilter") //$NON-NLS-1$
			&& value.equals("isBinaryMethod")) { //$NON-NLS-1$
			if (target instanceof IMethod) {
				IMethod method = (IMethod) target;
				return method.isBinary();
			}
		}
		return false;
	}
}
