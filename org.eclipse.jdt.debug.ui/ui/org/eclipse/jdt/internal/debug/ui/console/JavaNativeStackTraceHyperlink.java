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


import org.eclipse.debug.ui.console.IConsole;

/**
 * A hyperlink from a stack trace line of the form "*(Native Method)"
 */
public class JavaNativeStackTraceHyperlink extends JavaStackTraceHyperlink {

	/**
     * @see JavaStackTraceHyperlink#JavaStackTraceHyperlink(IConsole, int, int)
	 */
	public JavaNativeStackTraceHyperlink(IConsole console) {
		super(console);
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceHyperlink#getLineNumber()
	 */
	protected int getLineNumber() {
		return -1;
	}

}
