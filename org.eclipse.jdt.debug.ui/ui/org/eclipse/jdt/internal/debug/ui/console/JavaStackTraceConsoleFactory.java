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

import java.io.IOException;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;

/**
 * Creates a new console into which users can paste stack traces and follow
 * the hyperlinks.
 * 
 * @since 3.1
 */
public class JavaStackTraceConsoleFactory implements IConsoleFactory {
    public final static String CONSOLE_TYPE = "javaStackTraceConsole"; //$NON-NLS-1$
    
    private IOConsole fConsole;

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsoleFactory#openConsole()
     */
    public void openConsole() {
        if (fConsole == null) {
        fConsole = new IOConsole(ConsoleMessages.getString("JavaStackTraceConsoleFactory.0"), CONSOLE_TYPE, null); //$NON-NLS-1$
        try {
            fConsole.getInputStream().close();
        } catch (IOException e) {
        }
        }
        
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        consoleManager.addConsoles(new IConsole[]{fConsole});
        consoleManager.showConsoleView(fConsole);
    }

}
