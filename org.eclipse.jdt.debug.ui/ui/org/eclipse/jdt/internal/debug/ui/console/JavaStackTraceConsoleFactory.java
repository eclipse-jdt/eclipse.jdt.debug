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
package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;

/**
 * Creates a new console into which users can paste stack traces and follow
 * the hyperlinks.
 * 
 * @since 3.1
 */
public class JavaStackTraceConsoleFactory implements IConsoleFactory {
    

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsoleFactory#openConsole()
     */
    public void openConsole() {
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        IConsole[] consoles = consoleManager.getConsoles();
        IConsole theConsole = null;
        for (int i = 0; i < consoles.length; i++) {
            IConsole console = consoles[i];
            if (console.getType().equals(JavaStackTraceConsole.CONSOLE_TYPE)) {
                theConsole = console;
                break;
            }
            
        }
        
        if (theConsole == null) {
            theConsole = new JavaStackTraceConsole(); //$NON-NLS-1$
	        consoleManager.addConsoles(new IConsole[]{theConsole});
        }
        
        consoleManager.showConsoleView(theConsole);
    }
}
