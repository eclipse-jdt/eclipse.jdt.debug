/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IActionFilter;

public class MemberActionFilter implements IActionFilter {

	/**
	 * @see org.eclipse.ui.IActionFilter#testAttribute(Object, String, String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		if (name.equals("MemberActionFilter")) { //$NON-NLS-1$
			if (target instanceof IMember) {
				IMember member = (IMember) target;
				if (value.equals("isAbstract")) { //$NON-NLS-1$
					try {
						return Flags.isAbstract(member.getFlags());
					} catch (JavaModelException e) {
					}
				}
				if (value.equals("isRemote")) { //$NON-NLS-1$
					return !member.getJavaProject().getProject().exists();
				}
				if(value.equals("isInterface")) { //$NON-NLS-1$
					IType type = member.getDeclaringType();
					try {
						return type != null && type.isInterface();
					} 
					catch (JavaModelException e) {JDIDebugUIPlugin.log(e);}  
				}
			}
		}
		return false;
	}
}
