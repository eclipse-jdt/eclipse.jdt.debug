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
package org.eclipse.jdt.internal.debug.ui.console;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.ui.console.TextConsole;

/**
 * A hyperlink from a stack trace line of the form "*(Native Method)"
 */
public class JavaNativeStackTraceHyperlink extends JavaStackTraceHyperlink {

	public JavaNativeStackTraceHyperlink(TextConsole console) {
		super(console);
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceHyperlink#getLineNumber()
	 */
	@Override
	protected int getLineNumber(String linkText) {
		return -1;
	}

	@Override
	protected String getTypeName(String linkText) throws CoreException {
		String typeName;
    	int index = linkText.indexOf('(');
		if (index >= 0) {
			typeName = linkText.substring(0, index);
			// remove the method name
			index = typeName.lastIndexOf('.');
			int innerClassIndex = typeName.lastIndexOf('$', index);
			if (innerClassIndex != -1)
				index = innerClassIndex;
			if (index >= 0) {
				typeName = typeName.substring(0, index);
			}
			return typeName;
		}
		
        IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.JavaStackTraceHyperlink_Unable_to_parse_type_name_from_hyperlink__5, null); 
        throw new CoreException(status);
	}
}
