/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *     Paul Pazderski - Bug 546900: Fix IO handling in JavaStacktraceConsole
 *     Paul Pazderski - Bug 343023: Clear the initial stack trace console message on first edit
 *     Paul Pazderski - Bug 304219: Recognize more typical stack trace keywords for formatting
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
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

        @Override
		public boolean isReadOnly(int offset) {
            return false;
        }

        @Override
		public StyleRange[] getStyleRanges(int offset, int length) {
            return null;
        }

    }

    public final static String CONSOLE_TYPE = "javaStackTraceConsole"; //$NON-NLS-1$
    public final static String FILE_NAME = JDIDebugUIPlugin.getDefault().getStateLocation().toOSString() + File.separator + "stackTraceConsole.txt"; //$NON-NLS-1$

	private static final String NL = "\n"; //$NON-NLS-1$
	private static final String INDENT_STR = "    "; //$NON-NLS-1$
	private static final int INDENT_WIDTH = 4;

    private final JavaStackTraceConsolePartitioner partitioner = new JavaStackTraceConsolePartitioner();
    private final IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
        @Override
		public void propertyChange(PropertyChangeEvent event) {
            String property = event.getProperty();
            if (property.equals(IDebugUIConstants.PREF_CONSOLE_FONT)) {
                setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));
            }
        }
    };
	/** Memorize if stack trace console is showing the initial "How to use" text at the moment. */
	boolean showsUsageHint = false;
	/** Document listener to recognize if initial "How to use" text is changed programmatically. Removes itself after first document change. */
	private final IDocumentListener documentsFirstChangeListener = new IDocumentListener() {
		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
		}
		@Override
		public void documentChanged(DocumentEvent event) {
			event.getDocument().removeDocumentListener(documentsFirstChangeListener);
			showsUsageHint = false;
		}
	};

	/**
	 * Constructor
	 */
    public JavaStackTraceConsole() {
		super(ConsoleMessages.JavaStackTraceConsoleFactory_0, CONSOLE_TYPE, JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_JAVA_STACKTRACE_CONSOLE), true);
        Font font = JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT);
        setFont(font);
        partitioner.connect(getDocument());
    }

	/**
	 * inits the document backing this console
	 */
	public void initializeDocument() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
			try {
				byte[] fileContent = Files.readAllBytes(file.toPath());
				getDocument().set(new String(fileContent));
            } catch (IOException e) {
				getDocument().set(NLS.bind(ConsoleMessages.JavaStackTraceConsole_2, e.getMessage()));
            }
        } else {
			getDocument().set(ConsoleMessages.JavaStackTraceConsole_0);
			getDocument().addDocumentListener(documentsFirstChangeListener);
			showsUsageHint = true;
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
	public void saveDocument() {
		IDocument document = getDocument();
		if (document != null) {
			if (document.getLength() > 0) {
				String contents = document.get();
				try (FileOutputStream fout = new FileOutputStream(FILE_NAME)) {
					fout.write(contents.getBytes());
				} catch (IOException e) {
					JDIDebugUIPlugin.log(e);
				}
			} else {
				File file = new File(FILE_NAME);
				file.delete();
			}
		}
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
	 *
	 * @param trace
	 *            the stack trace to format
	 * @return the formatted stack trace for this console
	 */
	private String format(String trace) {
		StringTokenizer tokenizer = new StringTokenizer(trace, " \t\n\r\f", true); //$NON-NLS-1$
		StringBuilder formattedTrace = new StringBuilder(trace.length());

		boolean insideAt = false;
		boolean newLine = true;
		int pendingSpaces = 0;
		boolean antTrace = false;
		int depth = 1;
		// Block depth map is used to find the most likely indentation for a Caused.
		// In combination with Suppressed the correct indentation can be ambiguous.
		// Map has indentation in number of spaces of a previous block as key and formated
		// indentation depth used for this block as value.
		Map<Integer, Integer> blockDepth = new HashMap<>(3);

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.isEmpty()) {
				continue; // paranoid
			}
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
						pendingSpaces += INDENT_WIDTH;
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
			// token "at", "-", "...", "Caused by:", "Suppressed:" and "[CIRCULAR".
			if (newLine) {
				if (c == '\"') { // leading thread name, e.g. "Worker-124" prio=5
					formattedTrace.append(NL + NL); // print 2 lines to break between threads
				} else if (c == '-' // - locked <address>
						|| "...".equals(token)) { //$NON-NLS-1$ ... xx more
					applyIndentedToken(formattedTrace, depth, token, antTrace);
					pendingSpaces = 0;
					continue;
				} else if ("at".equals(token)) { //$NON-NLS-1$ at method
					insideAt = true;
					applyIndentedToken(formattedTrace, depth, token, antTrace);
					pendingSpaces = 0;
					continue;
				} else if (c == '[') {
					if ("[CIRCULAR".equals(token)) { //$NON-NLS-1$ [CIRCULAR REFERENCE:toString()]
						applyIndentedToken(formattedTrace, depth, token, antTrace);
						pendingSpaces = 0;
					} else {
						if (antTrace) {
							formattedTrace.append(NL);
						}
						formattedTrace.append(token);
						pendingSpaces = 0;
						antTrace = true;
					}
					continue;
				} else if ("Caused".equals(token)) { //$NON-NLS-1$ Caused by: reason
					// Guess depth for Cause block. This can be interpreted as if the Caused
					// block is moved to the left until it aligns with a previous Suppressed
					// block or hit the line begin.
					depth = 0;
					for (Map.Entry<Integer, Integer> block : blockDepth.entrySet()) {
						if (block.getKey() <= pendingSpaces && block.getValue() > depth) {
							depth = block.getValue();
						}
					}
					applyIndentedToken(formattedTrace, depth, token, antTrace);
					depth++;
					pendingSpaces = 0;
					continue;
				} else if ("Suppressed:".equals(token)) { //$NON-NLS-1$ Suppressed: reason
					if (depth >= 2) {
						depth--;
					}
					blockDepth.put(pendingSpaces, depth);
					applyIndentedToken(formattedTrace, depth, token, antTrace);
					depth = 2;
					pendingSpaces = 0;
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

	private void applyIndentedToken(StringBuilder formattedTrace, int depth, String token, boolean antTrace) {
		if (antTrace) {
			formattedTrace.append(' ');
		} else {
			formattedTrace.append(NL);
		}
		for (int i = 0; i < depth; i++) {
			formattedTrace.append(INDENT_STR);
		}
		formattedTrace.append(token);
	}
}
