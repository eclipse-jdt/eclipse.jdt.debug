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

import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.TextConsole;

public class JavaStackTraceConsole extends TextConsole {
    public final static String CONSOLE_TYPE = "javaStackTraceConsole"; //$NON-NLS-1$
    
    private JavaStackTraceConsolePartitioner partitioner = new JavaStackTraceConsolePartitioner();
   
    public JavaStackTraceConsole() {
        super(ConsoleMessages.getString("JavaStackTraceConsoleFactory.0"), CONSOLE_TYPE, null, true); //$NON-NLS-1$
        partitioner.connect(getDocument());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.TextConsole#getPartitioner()
     */
    protected IConsoleDocumentPartitioner getPartitioner() {
        return partitioner;
    }

    class JavaStackTraceConsolePartitioner extends DefaultPartitioner implements IConsoleDocumentPartitioner {
        
        public JavaStackTraceConsolePartitioner() {
            super(new RuleBasedPartitionScanner(), null);
            getDocument().setDocumentPartitioner(this);
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.console.IConsoleDocumentPartitioner#clearBuffer()
         */
        public void clearBuffer() {
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.console.IConsoleDocumentPartitioner#isReadOnly(int)
         */
        public boolean isReadOnly(int offset) {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.console.IConsoleDocumentPartitioner#computeStyleRange(int, int)
         */
        public StyleRange[] getStyleRanges(int offset, int length) {
            return null;
        }

    }
}
