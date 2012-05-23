/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

/**
 * Adds a Java package to the set of active step filters.
 */
public class AddPackageStepFilterAction extends AbstractAddStepFilterAction {

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.AbstractAddStepFilterAction#generateStepFilterPattern(org.eclipse.jdt.debug.core.IJavaStackFrame)
	 */
	@Override
	protected String generateStepFilterPattern(IJavaStackFrame frame) {
		String typeName;
		try {
			typeName = frame.getDeclaringTypeName();
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
