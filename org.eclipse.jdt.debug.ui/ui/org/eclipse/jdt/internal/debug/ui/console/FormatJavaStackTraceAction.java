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

import java.util.StringTokenizer;

import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Event;

public class FormatJavaStackTraceAction extends Action {

    private JavaStackTraceConsole fConsole;

    FormatJavaStackTraceAction(JavaStackTraceConsole console) {
        super(ConsoleMessages.getString("FormatJavaStackTraceAction.0"), JavaDebugImages.DESC_ELCL_FORMAT_STACKTRACE); //$NON-NLS-1$
        setToolTipText(ConsoleMessages.getString("FormatJavaStackTraceAction.1")); //$NON-NLS-1$
        fConsole = console;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        IDocument document = fConsole.getDocument();
        String orig = document.get();
        if (orig != null && orig.length() > 0) {
            document.set(""); //$NON-NLS-1$ hack avoids bug in the default position updater
            document.set(format(orig));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IAction#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    public void runWithEvent(Event event) {
        run();
    }

    private String format(String trace) {
        StringTokenizer tokenizer = new StringTokenizer(trace, " \t\n\r\f", true); //$NON-NLS-1$
        StringBuffer formattedTrace = new StringBuffer();
        
        boolean insideAt = false;
        boolean newline = true;
        int pendingSpaces = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.length() == 0)
                continue; // paranoid
            char c = token.charAt(0);
            // handle delimiters
            switch (c) {
            case ' ':
                if (newline) {
                    pendingSpaces++;
                } else {
                    pendingSpaces = 1;
                }
                continue;
            case '\t':
                if (newline) {
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
                    newline = true;
                }
                continue;
            }
            // consider newlines only before token starting with char '\"' or
            // token "at" or "-".
            if (newline) {
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
                    formattedTrace.append("\n"); //$NON-NLS-1$
                    formattedTrace.append("    "); //$NON-NLS-1$
                    formattedTrace.append(token);
                    insideAt = true;
                    pendingSpaces = 0;
                    continue;
                } 
                newline = false;
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

}
