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
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;

/**
 * Provides links for stack traces in J9 output
 */
public class J9ConsoleTracker implements IConsoleLineTracker {
	
	/**
	 * The console associated with this line tracker 
	 */
	private IConsole fConsole;
	
	private StringMatcher fJ9Matcher;

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	public void init(IConsole console) {
		fConsole = console;
		fJ9Matcher = new StringMatcher("*.*(*)*", false, false); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
	 */
	public void lineAppended(IRegion line) {
		try {
			int offset = line.getOffset();
			int length = line.getLength();
			String text = fConsole.getDocument().get(offset, length);
			int index = -1;
			if (fJ9Matcher.match(text)) {
				// find the last space in the line
				index = text.lastIndexOf(' ');
				if (index >= 0) {
					int linkOffset = offset + index + 1;
					int linkLength = length - index - 1;
					IConsoleHyperlink link = null;
					link = new J9StackTraceHyperlink(fConsole);
					fConsole.addLink(link, linkOffset, linkLength);
				}				
			}
		} catch (BadLocationException e) {
		}
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
	 */
	public void dispose() {
		fConsole = null;
		fJ9Matcher = null;
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#streamClosed()
	 */
	public void consoleClosed() {
	}

}
