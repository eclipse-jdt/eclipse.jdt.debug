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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.TextConsole;

public class JavaStackTraceConsole extends TextConsole {
    public final static String CONSOLE_TYPE = "javaStackTraceConsole"; //$NON-NLS-1$
    public final static String FILE_NAME = JDIDebugUIPlugin.getDefault().getStateLocation().toOSString() + File.separator + "stackTraceConsole.txt"; //$NON-NLS-1$

    private JavaStackTraceConsolePartitioner partitioner = new JavaStackTraceConsolePartitioner();
    private IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            String property = event.getProperty();
            if (property.equals(IDebugPreferenceConstants.CONSOLE_FONT)) {
                setFont(JFaceResources.getFont(IDebugPreferenceConstants.CONSOLE_FONT));
            }
        }
    };

    public JavaStackTraceConsole() {
        super(ConsoleMessages.JavaStackTraceConsoleFactory_0, CONSOLE_TYPE, null, true); //$NON-NLS-1$
        Font font = JFaceResources.getFont(IDebugPreferenceConstants.CONSOLE_FONT);
        setFont(font);
        partitioner.connect(getDocument());
    }

    void initializeDocument() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try {
                int len = (int) file.length();
                byte[] b = new byte[len];
                FileInputStream fin = new FileInputStream(file);
                int read = 0;
                while (read < len) {
                    read += fin.read(b);
                }
                getDocument().set(new String(b));
                fin.close();
            } catch (IOException e) {
            }
        } else {
			getDocument().set(ConsoleMessages.JavaStackTraceConsole_0); //$NON-NLS-1$
		}
    }

    protected void init() {
        JFaceResources.getFontRegistry().addListener(propertyListener);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.console.AbstractConsole#dispose()
     */
    protected void dispose() {
        saveDocument();
        JFaceResources.getFontRegistry().removeListener(propertyListener);
        super.dispose();
    }

    void saveDocument() {
        try {
            IDocument document = getDocument();
            if (document != null) {
                if (document.getLength() > 0) {
                    String contents = document.get();
                    FileOutputStream fout = new FileOutputStream(FILE_NAME);
                    fout.write(contents.getBytes());
                    fout.close();
                } else {
                    File file = new File(FILE_NAME);
                    file.delete();
                }
            }
        } catch (IOException e) {
        }
    }

    /*
     * (non-Javadoc)
     * 
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

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.console.IConsoleDocumentPartitioner#isReadOnly(int)
         */
        public boolean isReadOnly(int offset) {
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.console.IConsoleDocumentPartitioner#computeStyleRange(int,
         *      int)
         */
        public StyleRange[] getStyleRanges(int offset, int length) {
            return null;
        }

    }
}
