/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

/**
 * Provides links for stack traces
 */
public class JavaConsoleTracker implements IPatternMatchListenerDelegate {
	
	/**
	 * The console associated with this line tracker 
	 */
	private TextConsole fConsole;

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IPatternMatchListenerDelegate#connect(org.eclipse.ui.console.IConsole)
     */
    public void connect(TextConsole console) {
	    fConsole = console;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IPatternMatchListenerDelegate#disconnect()
     */
    public void disconnect() {
        fConsole = null;
    }
    
    protected TextConsole getConsole() {
        return fConsole;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IPatternMatchListenerDelegate#matchFound(org.eclipse.ui.console.PatternMatchEvent)
     */
    public void matchFound(PatternMatchEvent event) {
        try {
            int offset = event.getOffset();
            int length = event.getLength();
            IHyperlink link = new JavaStackTraceHyperlink(fConsole);
            fConsole.addHyperlink(link, offset+1, length-2);   
        } catch (BadLocationException e) {
        }
    }

}
