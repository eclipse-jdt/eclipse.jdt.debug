/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
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

    /**
     * Constructor
     * @param name
     */
    public IOConsoleTests(String name) {
        super(name);
    }

    /**
     * Tests that the pattern matcher will find a specific pattern
     * @throws Exception
     */
    public void testPatternMatchListener() throws Exception {
        MessageConsole console = new MessageConsole("Test console", null);
        fMatchCount = 0;
        console.addPatternMatchListener(this);
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        consoleManager.addConsoles(new IConsole[]{console});
        try {
            MessageConsoleStream stream = console.newMessageStream();
            stream.print("one foo bar");
            stream.println();
            stream.print("two foo bar");

            long endTime = System.currentTimeMillis() + 1500;
            while (!fDisconnected && System.currentTimeMillis() < endTime) {
                synchronized(this) {
                    wait(500);
                }
            }

            assertEquals("Should be two foo's", 2, fMatchCount);
        } finally {
            consoleManager.removeConsoles(new IConsole[]{console});
        }
    }

    /**
     * @see org.eclipse.ui.console.IPatternMatchListener#getPattern()
     */
    @Override
	public String getPattern() {
        return "foo";
    }

    /**
     * @see org.eclipse.ui.console.IPatternMatchListener#getLineQualifier()
     */
    @Override
	public String getLineQualifier() {
    	return "foo";
    }

    /**
     * @see org.eclipse.ui.console.IPatternMatchListenerDelegate#matchFound(org.eclipse.ui.console.PatternMatchEvent)
     */
    @Override
	public synchronized void matchFound(PatternMatchEvent event) {
        fMatchCount++;
    }

	/**
	 * @see org.eclipse.ui.console.IPatternMatchListener#getCompilerFlags()
	 */
	@Override
	public int getCompilerFlags() {
		return 0;
	}

    /**
     * @see org.eclipse.ui.console.IPatternMatchListenerDelegate#connect(org.eclipse.ui.console.TextConsole)
     */
    @Override
	public void connect(TextConsole console) {}

    /**
     * @see org.eclipse.ui.console.IPatternMatchListenerDelegate#disconnect()
     */
    @Override
	public synchronized void disconnect() {
        fDisconnected = true;
        notifyAll();
    }
}

