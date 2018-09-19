/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
