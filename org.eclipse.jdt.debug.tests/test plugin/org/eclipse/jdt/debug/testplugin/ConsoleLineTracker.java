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
package org.eclipse.jdt.debug.testplugin;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * Simple console line tracker extension point that delegates messages 
 */
public class ConsoleLineTracker implements IConsoleLineTracker {
	
	/**
	 * Forwards messages to the delegate when not <code>null</code> 
	 */
	private static IConsoleLineTracker fDelegate;
	private static IConsole fConsole;
	
	/**
	 * Sets the delegate, possilby <code>null</code>
	 *  
	 * @param tracker
	 */
	public static void setDelegate(IConsoleLineTracker tracker) {
		fDelegate = tracker;
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
	 */
	public void dispose() {
		if (fDelegate != null) {
			fDelegate.dispose();
		}
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	public synchronized void init(IConsole console) {
		fConsole= console;
		if (fDelegate != null) {
			fDelegate.init(console);
		}
	}
	
	public static IDocument getDocument() {
		return fConsole.getDocument();
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
	 */
	public void lineAppended(IRegion line) {
		if (fDelegate != null) {
			fDelegate.lineAppended(line);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#streamClosed()
	 */
	public void streamClosed() {
	}
	
}
