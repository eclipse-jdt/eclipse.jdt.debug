/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
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

/**
 * IOConsoleTests for the Automated suite
 */
public class IOConsoleTests extends AbstractDebugTest implements IPatternMatchListener {
    
    private int fMatchCount;

    public IOConsoleTests(String name) {
        super(name);
    }
    
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
            Thread.sleep(1500); // ensure the console doc gets updated
            assertEquals("Should be two foo's", 2, fMatchCount);
        } finally {
            consoleManager.removeConsoles(new IConsole[]{console});
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IPatternMatchListener#getPattern()
     */
    public String getPattern() {
        return "foo";
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IPatternMatchListener#matchFound(org.eclipse.ui.console.PatternMatchEvent)
     */
    public void matchFound(PatternMatchEvent event) {
        fMatchCount++;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IPatternMatchListener#getModifiers()
	 */
	public int getCompilerFlags() {
		return 0;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IPatternMatchListener#getMatchContext()
     */
    public String getMatchContext() {
        return "line";
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IPatternMatchListener#connect(org.eclipse.ui.console.IConsole)
     */
    public void connect(IConsole console) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IPatternMatchListener#disconnect()
     */
    public void disconnect() {
    }

}
    