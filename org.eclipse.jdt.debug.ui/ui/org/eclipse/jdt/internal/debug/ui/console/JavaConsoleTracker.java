package org.eclipse.jdt.internal.debug.ui.console;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;

/**
 * Provides links for stack traces
 */
public class JavaConsoleTracker implements IConsoleLineTracker {
	
	/**
	 * The console associated with this line tracker 
	 */
	private IConsole fConsole;
	
	private StringMatcher fJavaMatcher;
	private StringMatcher fNativeMatcher;

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	public void init(IConsole console) {
		fConsole = console;
		fJavaMatcher = new StringMatcher("*(*.java:*)", false, false);
		fNativeMatcher = new StringMatcher("*(Native Method)", false, false);
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
	 */
	public void lineAppended(IRegion line) {
		try {
			int offset = line.getOffset();
			int length = line.getLength();
			String text = fConsole.getDocument().get(offset, length);
			boolean standardMatch = false;
			int index = -1;
			if (fJavaMatcher.match(text)) {
				standardMatch = true;
				// find the last space in the line
				index = text.lastIndexOf(' ');
			} else if (fNativeMatcher.match(text)) {
				// find the second last space in the line
				index = text.lastIndexOf(' ', text.length() - 15);
			}
			if (index >= 0) {
				int linkOffset = offset + index + 1;
				int linkLength = length - index - 1;
				IConsoleHyperlink link = null;
				if (standardMatch) {
					link = new JavaStackTraceHyperlink(fConsole, linkOffset, linkLength);
				} else {
					link = new JavaNativeStackTraceHyperlink(fConsole, linkOffset, linkLength);
				}	
				fConsole.addLink(link);			
			}
		} catch (BadLocationException e) {
		}
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
	 */
	public void dispose() {
		fConsole = null;
		fJavaMatcher = null;
		fNativeMatcher = null;
	}

}
