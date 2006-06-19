/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import java.util.StringTokenizer;

import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;

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
        super(ConsoleMessages.JavaStackTraceConsoleFactory_0, CONSOLE_TYPE, null, true); 
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
			getDocument().set(ConsoleMessages.JavaStackTraceConsole_0); 
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

    class JavaStackTraceConsolePartitioner extends FastPartitioner implements IConsoleDocumentPartitioner {

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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.AbstractConsole#getHelpContextId()
	 */
	public String getHelpContextId() {
		return IJavaDebugHelpContextIds.STACK_TRACE_CONSOLE;
	}
    
    public IPageBookViewPage createPage(IConsoleView view) {
    	return new JavaStackTraceConsolePage(this, view);
	}
    
    
    public void format() {
        IDocument document = getDocument();
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
                } else if (c == '[') { 
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
}
