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

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTrackerExtension;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.testplugin.ConsoleLineTracker;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;

/**
 * Tests console line tracker.
 */
public class LineTrackerTests extends AbstractDebugTest implements IConsoleLineTrackerExtension {
	
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
	
	protected void setUp() {
		fStarted = false;
		fStopped = false;
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
			// there are 10 lines and one "empty line" (i.e. the last "new line")
			assertEquals("Wrong number of lines output", 11, fLinesRead.size());
			for (int i = 0; i < 10; i++) {
				assertEquals("Line " + i + " not equal", fLines[i], fLinesRead.get(i));			
			}
			assertEquals("Should be an empty last line", "", fLinesRead.get(10));
		} finally {
			ConsoleLineTracker.setDelegate(null);
			terminateAndRemove(fTarget);
		}
	} 
	
	/**
	 * This program prints the final line without a new line
	 * @throws Exception
	 */
	public void testNoPrintln() throws Exception {
		ConsoleLineTracker.setDelegate(this);
		fTarget = null;
		try {
			fTarget = launchAndTerminate("OneToTenPrint");
			int attempts = 0;
			while (!fStopped) {
				assertTrue("did not get output within 30 seconds", attempts < 300);
				attempts++;
				Thread.sleep(100);
			}
			// just 10 lines this time
			assertEquals("Wrong number of lines", 10, fLinesRead.size());
			for (int i = 0; i < 10; i++) {
				assertEquals("Line " + i + " not equal", fLines[i], fLinesRead.get(i));			
			}
		} finally {
			ConsoleLineTracker.setDelegate(null);
			terminateAndRemove(fTarget);
		}
	} 
	
	/**
	 * Test 10,000 lines of output.
	 * 
	 * @throws Exception
	 */
	public void testFlood() throws Exception {
		ConsoleLineTracker.setDelegate(this);
		ILaunch launch = null;
		try {
			ILaunchConfiguration config = getLaunchConfiguration("FloodConsole");
			assertNotNull("Could not locate launch configuration", config);
			launch = config.launch(ILaunchManager.RUN_MODE, null);
			
			int attempts = 0;
			while (!fStopped) {
				assertTrue("did not get output within 60 seconds", attempts < 600);
				attempts++;
				Thread.sleep(100);
			}
			// Should be 10,000 lines
			assertEquals("Wrong number of lines", 10000, fLinesRead.size());
		} finally {
			ConsoleLineTracker.setDelegate(null);
			launch.getProcesses()[0].terminate();
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
		}
	} 	
	
	public void testHyperLink() throws Exception {
		ConsoleLineTracker.setDelegate(this);
		launchAndTerminate("ThrowsNPE");
		getHyperlink(0, ConsoleLineTracker.getDocument());
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	public void init(IConsole console) {
		fConsole = console;
		fStarted = true;
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
	 */
	public void lineAppended(IRegion line) {
		if (fStarted) {
			try {
				String text = fConsole.getDocument().get(line.getOffset(), line.getLength());
				fLinesRead.add(text);
			} catch (BadLocationException e) {
			}
		}
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#streamClosed()
	 */
	public void consoleClosed() {
		if (fStarted) {
			fStopped = true;
		}		
	}

}
