package org.eclipse.jdt.internal.debug.ui.console;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.console.IConsole;

/**
 * A hyperlink from a stack trace line of the form "*(Native Method)"
 */
public class JavaNativeStackTraceHyperlink extends JavaStackTraceHyperlink {

	/**
     * @see JavaStackTraceHyperlink#JavaStackTraceHyperlink(IConsole, int, int)
	 */
	public JavaNativeStackTraceHyperlink(IConsole console, int offset, int length) {
		super(console, offset, length);
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceHyperlink#getLineNumber()
	 */
	protected int getLineNumber() throws CoreException {
		return -1;
	}

}
