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

import java.util.StringTokenizer;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.console.IConsoleView;

public class FormatStackTraceActionDelegate implements IViewActionDelegate {
    
    private IConsoleView fConsoleView;
    private JavaStackTraceConsole fConsole;

    public FormatStackTraceActionDelegate() {
    }
    
    public FormatStackTraceActionDelegate(JavaStackTraceConsole console) {
        fConsole = console;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view) {
        fConsoleView = (IConsoleView) view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        if (fConsoleView != null) { 
            fConsole = (JavaStackTraceConsole) fConsoleView.getConsole();
        }
        IDocument document = fConsole.getDocument();
        String orig = document.get();
        if (orig != null && orig.length() > 0) {
            document.set(""); //$NON-NLS-1$ hack avoids bug in the default position updater
            document.set(format(orig));
        }
    }

    private String format(String trace) {
        StringTokenizer tokenizer = new StringTokenizer(trace, " \t\n\r\f", true); //$NON-NLS-1$
        StringBuffer formattedTrace = new StringBuffer();
        
        boolean insideAt = false;
        boolean newLine = true;
        int pendingSpaces = 0;
        boolean antTrace = false;
        
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.length() == 0)
                continue; // paranoid
            char c = token.charAt(0);
            // handle delimiters
            switch (c) {
            case ' ':
                if (newLine) {
                    pendingSpaces++;
                } else {
                    pendingSpaces = 1;
                }
                continue;
            case '\t':
                if (newLine) {
                    pendingSpaces += 4;
                } else {
                    pendingSpaces = 1;
                }
                continue;
            case '\n':
            case '\r':
            case '\f':
                if (insideAt) {
                    pendingSpaces = 1;
                } else {
                    pendingSpaces = 0;
                    newLine = true;
                }
                continue;
            }
            // consider newlines only before token starting with char '\"' or
            // token "at" or "-".
            if (newLine || antTrace) {
                if (c == '\"') { // leading thread name, e.g. "Worker-124"
                                    // prio=5
                    formattedTrace.append("\n\n"); //$NON-NLS-1$  print 2 lines to break between threads
                } else if ("-".equals(token)) { //$NON-NLS-1$ - locked ...
                    formattedTrace.append("\n"); //$NON-NLS-1$
                    formattedTrace.append("    "); //$NON-NLS-1$
                    formattedTrace.append(token);
                    pendingSpaces = 0;
                    continue;
                } else if ("at".equals(token)) { //$NON-NLS-1$  at ...
                    if (!antTrace) {
                        formattedTrace.append("\n"); //$NON-NLS-1$
                        formattedTrace.append("    "); //$NON-NLS-1$
                    } else {
                        formattedTrace.append(' ');
                    }
                    insideAt = true;
                    formattedTrace.append(token);
                    pendingSpaces = 0;
                    continue;
                } else if (c == '[') { //$NON-NLS-1$
                    if(antTrace) {
                        formattedTrace.append("\n"); //$NON-NLS-1$
                    }
                    formattedTrace.append(token);
                    pendingSpaces = 0;
                    newLine = false;
                    antTrace = true;
                    continue;
                }
                newLine = false;
            }
            if (pendingSpaces > 0) {
                for (int i = 0; i < pendingSpaces; i++) {
                    formattedTrace.append(' ');
                }
                pendingSpaces = 0;
            }
            formattedTrace.append(token);
            insideAt = false;
        }
        
        return formattedTrace.toString();
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }      
}
