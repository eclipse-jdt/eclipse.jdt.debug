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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.IDebugUIConstants;
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
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Provides a stack trace console for Java stack traces
 */
public class JavaStackTraceConsole extends TextConsole {
	
	/**
	 * Provides a partitioner for this console type
	 */
	class JavaStackTraceConsolePartitioner extends FastPartitioner implements IConsoleDocumentPartitioner {

        public JavaStackTraceConsolePartitioner() {
            super(new RuleBasedPartitionScanner(), null);
            getDocument().setDocumentPartitioner(this);
        }

        public boolean isReadOnly(int offset) {
            return false;
        }

        public StyleRange[] getStyleRanges(int offset, int length) {
            return null;
        }

    }
	
    public final static String CONSOLE_TYPE = "javaStackTraceConsole"; //$NON-NLS-1$
    public final static String FILE_NAME = JDIDebugUIPlugin.getDefault().getStateLocation().toOSString() + File.separator + "stackTraceConsole.txt"; //$NON-NLS-1$

    private JavaStackTraceConsolePartitioner partitioner = new JavaStackTraceConsolePartitioner();
    private IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            String property = event.getProperty();
            if (property.equals(IDebugUIConstants.PREF_CONSOLE_FONT)) {
                setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));
            }
        }
    };

    /**
     * Constructor
     */
    public JavaStackTraceConsole() {
        super(ConsoleMessages.JavaStackTraceConsoleFactory_0, CONSOLE_TYPE, null, true); 
        Font font = JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT);
        setFont(font);
        partitioner.connect(getDocument());
    }

	/**
	 * inits the document backing this console
	 */
	void initializeDocument() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try {
                int len = (int) file.length();
                byte[] b = new byte[len];
                InputStream fin = new BufferedInputStream(new FileInputStream(file));
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

    /**
     * @see org.eclipse.ui.console.AbstractConsole#init()
     */
    @Override
	protected void init() {
        JFaceResources.getFontRegistry().addListener(propertyListener);
    }

    /**
     * @see org.eclipse.ui.console.TextConsole#dispose()
     */
    @Override
	protected void dispose() {
        saveDocument();
        JFaceResources.getFontRegistry().removeListener(propertyListener);
        super.dispose();
    }

    /**
     * Saves the backing document for this console
     */
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
        } 
        catch (IOException e) {}
    }

    /**
     * @see org.eclipse.ui.console.TextConsole#getPartitioner()
     */
    @Override
	protected IConsoleDocumentPartitioner getPartitioner() {
        return partitioner;
    }

	/**
	 * @see org.eclipse.ui.console.AbstractConsole#getHelpContextId()
	 */
	@Override
	public String getHelpContextId() {
		return IJavaDebugHelpContextIds.STACK_TRACE_CONSOLE;
	}
    
    /**
     * @see org.eclipse.ui.console.TextConsole#createPage(org.eclipse.ui.console.IConsoleView)
     */
    @Override
	public IPageBookViewPage createPage(IConsoleView view) {
    	return new JavaStackTraceConsolePage(this, view);
	}
    
    /**
     * performs the formatting of the stacktrace console
     */
    public void format() {
    	WorkbenchJob job = new WorkbenchJob(ConsoleMessages.JavaStackTraceConsole_1) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
	            IJobManager jobManager = Job.getJobManager();
	            try {
	                jobManager.join(this, monitor);
	            } catch (OperationCanceledException e1) {
	                return Status.CANCEL_STATUS;
	            } catch (InterruptedException e1) {
	                return Status.CANCEL_STATUS;
	            }
	            IDocument document = getDocument();
	            String orig = document.get();
	            if (orig != null && orig.length() > 0) {
	                document.set(format(orig));
	            }
	            
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
       
    }
    
    /**
     * Underlying format operation
     * @param trace the stack trace to format
     * @return the formatted stack trace for this console
     */
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
