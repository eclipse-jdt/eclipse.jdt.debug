/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.debug.ui.console.JavaStackTraceConsoleFactory;

/**
 * Opens the Java Stack Trace Console. Contributed as a command so that it can be found via Quick Access.
 */
public class OpenJavaStackTraceConsoleHandler extends AbstractHandler {
	private JavaStackTraceConsoleFactory fConsoleFactory;

	@Override
	public Object execute(ExecutionEvent event) {
		if (fConsoleFactory == null) {
			fConsoleFactory = new JavaStackTraceConsoleFactory();
		}
		fConsoleFactory.openConsole();
		return null;
	}
}
