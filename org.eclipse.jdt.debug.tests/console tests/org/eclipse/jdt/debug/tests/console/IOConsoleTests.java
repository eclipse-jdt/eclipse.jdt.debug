/*******************************************************************************
 *  Copyright (c) 2000, 2007 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Philippe Marschall <philippe.marschall@netcetera.ch> - Bug 76936
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.console;

import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

/**
 * IOConsoleTests for the Automated suite
 */
public class IOConsoleTests extends AbstractDebugTest implements IPatternMatchListener {
    
    private int fMatchCount;
    private boolean fDisconnected = false;
	private IConsoleManager fConsoleManager;
	private MessageConsole fConsole;

    /**
     * Constructor
     * @param name
     */
    public IOConsoleTests(String name) {
        super(name);
    }
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fMatchCount = 0;
		fConsole = new MessageConsole("Test console", null);
		fConsole.addPatternMatchListener(this);
		fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
		fConsoleManager.addConsoles(new IConsole[] { fConsole });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void tearDown() throws Exception {
		fConsoleManager.removeConsoles(new IConsole[] { fConsole });
		super.tearDown();
	}

	/**
	 * Tests the escape characters backspace, carriage return and line feed
	 * 
	 * @throws Exception
	 */
	public void testControlCharacters() throws Exception {
		MessageConsoleStream stream = fConsole.newMessageStream();
		stream.println("line 1");
		stream.println("fail the test\rwinn"); // winn the test
		stream.println("fail not\b\b\b"); // fail not
		stream.println("win not\b\b\byes"); // win yes
		stream.println("win not\b\b\b\b\b\b\b\b\b\b\b\byes"); // win yes
		stream.println("line\findented"); // line
											// indented
		stream.println("line\f\b\bindented"); // line
												// indented
		
		stream.flush();
		waitUpTo1500Millis();
		
		// no newlines at start or end
		stream.print("x\b1\nx\b2\nx\b3");
		
		waitUpTo1500Millis();
		stream.flush();
		
		// newlines at start and end
		stream.print("\nx\b1\n\nx\b2\nx\b3\n");

		waitUpTo1500Millis();

		// @formatter:off
		assertEquals("line 1\n"
				+ "winn the test\n"
				+ "fail not\n"
				+ "win yes\n"
				+ "yes not\n"
				+ "line\n"
				+ "    indented\n"
				+ "line\n"
				+ "  indented\n"
				+ "1\n2\n3"
				+ "\n1\n\n2\n3\n",
				fConsole.getDocument().get());
		// @formatter:on
	}

	/**
	 * Tests overwriting the last displayed character with backspace
	 * 
	 * @throws Exception
	 */
	public void testOverwriteLastCharacter() throws Exception {
		MessageConsoleStream stream = fConsole.newMessageStream();
		stream.print("abc");
		stream.flush();
		waitUpTo1500Millis();
		assertEquals("abc", fConsole.getDocument().get());
		stream.print("\b1");
		waitUpTo1500Millis();

		assertEquals("ab1", fConsole.getDocument().get());
	}

	/**
	 * Tests overwriting the last displayed line with carriage return
	 * 
	 * @throws Exception
	 */
	public void testOverwriteLastLine() throws Exception {
		MessageConsoleStream stream = fConsole.newMessageStream();
		stream.print("abc");
		stream.flush();
		waitUpTo1500Millis();
		stream.print("\rxxx");
		waitUpTo1500Millis();

		assertEquals("xxx", fConsole.getDocument().get());
	}

    /**
     * Tests that the pattern matcher will find a specific pattern
     * @throws Exception
     */
    public void testPatternMatchListener() throws Exception {
		MessageConsoleStream stream = fConsole.newMessageStream();
		stream.print("one foo bar");
		stream.println();
		stream.print("two foo bar");

		waitUpTo1500Millis();

		assertEquals("Should be two foo's", 2, fMatchCount);
    }

	private void waitUpTo1500Millis() throws InterruptedException {
		long endTime = System.currentTimeMillis() + 1500;
		while (!fDisconnected && System.currentTimeMillis() < endTime) {
			synchronized (this) {
				wait(500);
			}
		}
	}

    /**
     * @see org.eclipse.ui.console.IPatternMatchListener#getPattern()
     */
    public String getPattern() {
        return "foo";
    }
    
    /**
     * @see org.eclipse.ui.console.IPatternMatchListener#getLineQualifier()
     */
    public String getLineQualifier() {
    	return "foo";
    }

    /**
     * @see org.eclipse.ui.console.IPatternMatchListenerDelegate#matchFound(org.eclipse.ui.console.PatternMatchEvent)
     */
    public synchronized void matchFound(PatternMatchEvent event) {
        fMatchCount++;
    }

	/**
	 * @see org.eclipse.ui.console.IPatternMatchListener#getCompilerFlags()
	 */
	public int getCompilerFlags() {
		return 0;
	}
	
    /**
     * @see org.eclipse.ui.console.IPatternMatchListenerDelegate#connect(org.eclipse.ui.console.TextConsole)
     */
    public void connect(TextConsole console) {}

    /**
     * @see org.eclipse.ui.console.IPatternMatchListenerDelegate#disconnect()
     */
    public synchronized void disconnect() {
        fDisconnected = true;
        notifyAll();
    }
}
    
