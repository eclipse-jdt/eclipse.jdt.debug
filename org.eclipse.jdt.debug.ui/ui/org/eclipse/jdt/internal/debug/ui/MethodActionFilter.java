package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IActionFilter;

public class MethodActionFilter implements IActionFilter {

	/**
	 * @see org.eclipse.ui.IActionFilter#testAttribute(Object, String, String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		if (name.equals("MethodActionFilter") //$NON-NLS-1$
			&& value.equals("isNotAbstract")) { //$NON-NLS-1$
			if (target instanceof IMethod) {
				IMethod method = (IMethod) target;
				try {
					return !Flags.isAbstract(method.getFlags());
				} catch (JavaModelException e) {
					JDIDebugUIPlugin.log(e);
				}
				
			}
		}
		return false;
	}
}
