/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;


/**
 * Creates JavaNativeStackTraceHyperlinks
 * @since 3.1
 */
public class JavaNativeConsoleTracker extends JavaConsoleTracker {
    @Override
	public void matchFound(PatternMatchEvent event) {
        try {
            int offset = event.getOffset();
            int length = event.getLength();
            TextConsole console = getConsole();
            IHyperlink link = new JavaNativeStackTraceHyperlink(console);
            console.addHyperlink(link, offset+1, length-2);   
        } catch (BadLocationException e) {
        }
    }

}
