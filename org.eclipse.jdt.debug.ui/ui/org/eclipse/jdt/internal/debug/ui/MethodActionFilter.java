/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;


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
			&& value.equals("isAbstract")) { //$NON-NLS-1$
			if (target instanceof IMethod) {
				IMethod method = (IMethod) target;
				try {
					return Flags.isAbstract(method.getFlags());
				} catch (JavaModelException e) {
					JDIDebugUIPlugin.log(e);
				}
				
			}
		}
		return false;
	}
}
