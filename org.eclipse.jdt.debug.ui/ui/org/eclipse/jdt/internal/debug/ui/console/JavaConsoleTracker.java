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


import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	private Pattern fJavaQualifiedNamePattern;
	private boolean fInTrace = false;
	String fPrevText = null;
	private IRegion fPrevLine = null;

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	public void init(IConsole console) {
		fConsole = console;
		fJavaMatcher = new StringMatcher("*(*.java:*)", false, false); //$NON-NLS-1$
		fNativeMatcher = new StringMatcher("*(Native Method)", false, false); //$NON-NLS-1$
		fJavaQualifiedNamePattern = Pattern.compile("([$_A-Za-z][$_A-Za-z0-9]*[.])*[$_A-Za-z][$_A-Za-z0-9]*Exception"); //$NON-NLS-1$
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
				if (!fInTrace) {
					fInTrace = true;
					// look for exception name
					Matcher m = fJavaQualifiedNamePattern.matcher(fPrevText);
					if (m.find()) {
						int start = m.start();
						int end = m.end();
						int size = end - start;
						IConsoleHyperlink link = new JavaExceptionHyperLink(fConsole, fPrevText.substring(start, end));
						start += fPrevLine.getOffset();
						fConsole.addLink(link, start, size);
					}
				}
				int linkOffset = offset + index + 1;
				int linkLength = length - index - 1;
				IConsoleHyperlink link = null;
				if (standardMatch) {
					link = new JavaStackTraceHyperlink(fConsole);
				} else {
					link = new JavaNativeStackTraceHyperlink(fConsole);
				}	
				fConsole.addLink(link, linkOffset, linkLength);			
			} else {
				if (fInTrace) {
					fInTrace = false;
				}
			}
			fPrevText = text;
			fPrevLine = line;
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
