/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.ui.console;

/**
 * Creates a new console into which users can paste stack traces and follow the hyperlinks. creates a public API for
 * org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsoleFactory
 * 
 * @since 3.8
 * 
 */
public class JavaStackTraceConsoleFactory {
	private org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsoleFactory javaStackTraceConsoleFactory;

	public JavaStackTraceConsoleFactory() {
		javaStackTraceConsoleFactory = new org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsoleFactory();
	}

	/**
	 * Invokes openConsole() from org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsoleFactory
	 */
	public void openConsole() {
		javaStackTraceConsoleFactory.openConsole();
	}

	/**
	 * Invokes openConsole(String) from org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsoleFactory
	 * 
	 * @param initialText
	 *            text to put in the console or <code>null</code>.
	 */
	public void openConsole(String initialText) {
		javaStackTraceConsoleFactory.openConsole(initialText);
	}
	
	

}
