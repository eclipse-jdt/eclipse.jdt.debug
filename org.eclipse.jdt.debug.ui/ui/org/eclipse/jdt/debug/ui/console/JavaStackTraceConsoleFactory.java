/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
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
package org.eclipse.jdt.debug.ui.console;

import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsole;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;

/**
 * Creates a new console into which users can paste stack traces and follow the hyperlinks.
 *
 * @since 3.8
 *
 */
public class JavaStackTraceConsoleFactory implements IConsoleFactory {
	private IConsoleManager fConsoleManager = null;
	private JavaStackTraceConsole fConsole = null;

	public JavaStackTraceConsoleFactory() {
		fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
		fConsoleManager.addConsoleListener(new IConsoleListener() {
			@Override
			public void consolesAdded(IConsole[] consoles) {
			}

			@Override
			public void consolesRemoved(IConsole[] consoles) {
				for (int i = 0; i < consoles.length; i++) {
					if (consoles[i] == fConsole) {
						fConsole.saveDocument();
						fConsole = null;
						break;
					}
				}
			}
		});
	}

	/**
	 * Opens the console (creating a new one if not previously initialized).
	 */
	@Override
	public void openConsole() {
		openConsole(null);
	}

	/**
	 * Opens the console (creating a new one if not previously initialized). If the passed string is not <code>null</code>, the text of the console is
	 * set to the string.
	 *
	 * @param initialText
	 *            text to put in the console or <code>null</code>.
	 */
	public void openConsole(String initialText) {
		if (fConsole == null) {
			fConsole = new JavaStackTraceConsole();
			if (initialText != null) {
				fConsole.getDocument().set(initialText);
			} else {
				fConsole.initializeDocument();
			}
			fConsoleManager.addConsoles(new IConsole[] { fConsole });
		} else if (initialText != null) {
			fConsole.getDocument().set(initialText);
		}
		fConsoleManager.showConsoleView(fConsole);
	}


}
