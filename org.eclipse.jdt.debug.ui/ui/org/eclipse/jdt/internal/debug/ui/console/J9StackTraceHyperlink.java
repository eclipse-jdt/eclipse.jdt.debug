package org.eclipse.jdt.internal.debug.ui.console;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

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
			IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, "Unable to parse type name from hyperlink.", null);
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
