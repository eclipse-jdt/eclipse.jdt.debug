package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

/**
 * Adds a Java package to the set of active step filters.
 */
public class AddPackageStepFilterAction extends AbstractAddStepFilterAction {

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.AbstractAddStepFilterAction#generateStepFilterPattern(org.eclipse.jdt.debug.core.IJavaStackFrame)
	 */
	protected String generateStepFilterPattern(IJavaStackFrame frame) {
		String typeName;
		try {
			typeName = frame.getReceivingTypeName();
		} catch (DebugException de) {
			return null;
		}
		
		// Check for default package, which is not supported by JDI
		int lastDot = typeName.lastIndexOf('.');
		if (lastDot < 0) {
			return null;
		} 
				
		// Append ".*" to the pattern to form a package name	
		String packageName = typeName.substring(0, lastDot + 1);
		packageName += '*';
		return packageName;
	}

}
