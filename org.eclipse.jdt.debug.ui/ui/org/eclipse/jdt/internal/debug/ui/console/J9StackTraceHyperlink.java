/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;

/**
 * A hyperlink from a J9 stack trace line of the form "*.*(*)*"
 */
public class J9StackTraceHyperlink extends JavaStackTraceHyperlink {

	/**
     * @see JavaStackTraceHyperlink#JavaStackTraceHyperlink(IConsole, int, int)
	 */
	public J9StackTraceHyperlink(IConsole console) {
		super(console);
	}

	/**
	 * @see JavaStackTraceHyperlink#getTypeName()
	 */
	protected String getTypeName() throws CoreException {
		String linkText = getLinkText();
		int index = linkText.lastIndexOf('(');
		if (index >= 0) {
			String typeName = linkText.substring(0, index);
			// remove the method name
			index = typeName.lastIndexOf('.');
			if (index >= 0) {
				typeName = typeName.substring(0, index);
			}
			// replace slashes with dots
			return typeName.replace('/', '.');
		} else {
			IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.getString("J9StackTraceHyperlink.Unable_to_parse_type_name_from_hyperlink._1"), null); //$NON-NLS-1$
			throw new CoreException(status);
		}
	}
	
	/**
	 * @see org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceHyperlink#getLineNumber()
	 */
	protected int getLineNumber() throws CoreException {
		return -1;
	}

}
