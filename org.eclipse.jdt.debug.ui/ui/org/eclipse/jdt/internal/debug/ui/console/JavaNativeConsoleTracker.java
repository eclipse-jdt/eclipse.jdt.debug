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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.PatternMatchEvent;


/**
 * Creates JavaNativeStackTraceHyperlinks
 * @since 3.1
 */
public class JavaNativeConsoleTracker extends JavaConsoleTracker {
    public void matchFound(PatternMatchEvent event) {
        try {
            int offset = event.getOffset();
            int length = event.getLength();
            IOConsole console = getConsole();
            IHyperlink link = new JavaNativeStackTraceHyperlink(console);
            console.addHyperlink(link, offset, length);   
        } catch (BadLocationException e) {
        }
    }

}
