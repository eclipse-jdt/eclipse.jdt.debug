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
package org.eclipse.jdt.debug.tests.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.testplugin.ConsoleLineTracker;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;

/**
 * Tests console line tracker.
 */
public class LineTrackerTests extends AbstractDebugTest implements IConsoleLineTracker {
	
	protected String[] fLines = new String[] {
		"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"
	};
	
	protected IJavaDebugTarget fTarget;
	
	protected List fLinesRead = new ArrayList();
	
	protected boolean fStarted = false;
	
	protected boolean fStopped = false;
	
	protected IConsole fConsole = null;
	
	public LineTrackerTests(String name) {
		super(name);
	}
	
	public void testSimpleLineCounter() throws Exception {
		ConsoleLineTracker.setDelegate(this);
		fTarget = null;
		try {
			fTarget = launchAndTerminate("OneToTen");
			int attempts = 0;
			while (!fStopped) {
				assertTrue("did not get output within 30 seconds", attempts < 300);
				attempts++;
				Thread.sleep(100);
			}
			assertEquals("Should be " + fLines.length + " lines", fLines.length, fLinesRead.size());
			for (int i = 0; i < fLines.length; i++) {
				assertEquals("Line " + i + " not equal", fLines[i], fLinesRead.get(i));			
			}
		} finally {
			ConsoleLineTracker.setDelegate(null);
			terminateAndRemove(fTarget);
		}
	} 

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
	 */
	public void dispose() {
		if (fStarted) {
			fStopped = true;
		}
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	public void init(IConsole console) {
		if (console.getProcess().getLaunch().getLaunchConfiguration().getName().equals("OneToTen")) {
			fConsole = console;
			fStarted = true;
		}
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
	 */
	public void lineAppended(IRegion line) {
		if (fStarted) {
			try {
				String text = fConsole.getDocument().get(line.getOffset(), line.getLength());
				fLinesRead.add(text);
				if (fLinesRead.size() == 10) {
					// terminate and remove the program so it gets disposed
					terminateAndRemove(fTarget);
				}
			} catch (BadLocationException e) {
			}
		}
	}

}
