/*******************************************************************************
 * Copyright (c) 2014, 2022 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *     Simeon Andreev - Bug 547041: Pulled as base class from {@link JavaStackTraceConsoleTest}
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.console;

import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.debug.tests.ui.AbstractDebugUiTests;
import org.eclipse.jdt.debug.ui.console.JavaStackTraceConsoleFactory;
import org.eclipse.jdt.internal.debug.ui.console.ConsoleMessages;
import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsole;
import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsolePage;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.internal.console.ConsoleHyperlinkPosition;
import org.eclipse.ui.internal.console.ConsoleView;

/**
 * Base for {@link JavaStackTraceConsole} tests.
 */
public class AbstractJavaStackTraceConsoleTest extends AbstractDebugUiTests {

	private final static Pattern LEFT_INDENT = Pattern.compile("^[ \\t]*");
	private final static Pattern RIGHT_INDENT = Pattern.compile("\\s+$");

	protected final JavaStackTraceConsoleFactory fConsoleFactory = new JavaStackTraceConsoleFactory();
	protected JavaStackTraceConsole fConsole;

	public AbstractJavaStackTraceConsoleTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		sync(() -> initConsole(true));
	}

	@Override
	protected void tearDown() throws Exception {
		sync(() -> removeConsole(false));
		super.tearDown();
	}

	/**
	 * Create and register a {@link JavaStackTraceConsole}.
	 *
	 * @param assertDefaultContent
	 *            If <code>true</code> assert console is initialized with its default content.
	 * @see #removeConsole(boolean)
	 */
	protected void initConsole(boolean assertDefaultContent) {
		fConsoleFactory.openConsole();
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		IConsole[] consoles = consoleManager.getConsoles();
		fConsole = null;
		for (IConsole console : consoles) {
			if (console instanceof JavaStackTraceConsole) {
				fConsole = (JavaStackTraceConsole) console;
				// do not end loop. There should be only one JavaStackTraceConsole but if there are more
				// the last one is most likely the one we opened
			}
		}
		assertNotNull("Failed to open a JavaStackTraceConsole", fConsole);
		if (assertDefaultContent) {
			assertInitialContent();
		}
	}

	/**
	 * Remove the previous created console.
	 *
	 * @param preserveContent
	 *            If <code>true</code> the remove does not prevent the current console content from being loaded by next
	 *            {@link JavaStackTraceConsole}.
	 * @see #initConsole(boolean)
	 */
	protected void removeConsole(boolean preserveContent) {
		if (!preserveContent) {
			fConsole.clearConsole();
		}
		final int contentLength = fConsole.getDocument().getLength();

		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		consoleManager.removeConsoles(new IConsole[] { fConsole });

		final Path stackTraceFile = Paths.get(JavaStackTraceConsole.FILE_NAME);
		if (!preserveContent) {
			assertTrue("Leaked content of JavaStackTraceConsole", Files.notExists(stackTraceFile));
		} else {
			assertTrue("JavaStackTraceConsole content was not persisted", Files.exists(stackTraceFile));
			try {
				assertTrue("Persisted content seems incomplete", Files.size(stackTraceFile) >= contentLength);
			} catch (IOException e) {
				fail("Persisted content seems incomplete");
			}
		}
	}

	protected IDocument consoleDocumentWithText(String text) throws Exception {
		IDocument document = sync(() -> {
			IDocument doc = fConsole.getDocument();
			doc.set(text);
			return doc;
		});
		// wait for document being parsed and hyperlinks created
		Job.getJobManager().join(fConsole, null);
		TestUtil.runEventLoop();
		return document;
	}

	protected String getLine(IDocument doc, int line) {
		IRegion lineInfo;
		try {
			lineInfo = doc.getLineInformation(line);
			return doc.get(lineInfo.getOffset(), lineInfo.getLength());
		} catch (BadLocationException ex) {
			return null;
		}
	}

	/**
	 * Do some tests on the stack trace indentation. No hardcoded valued just some general assumptions.
	 *
	 * @param doc
	 *            document to test
	 * @param startLine
	 *            first line to check
	 */
	protected void checkIndentationConsistency(IDocument doc, int startLine) {
		boolean firstSuppress = true;
		int lastIndent = -1;
		// Remember how the next line's indentation can differ from the previous.
		// -1 -> less indented
		// 0 -> equal
		// 1 -> more indented
		int allowedIndentChange = 1;
		for (int i = startLine, lineCount = doc.getNumberOfLines(); i < lineCount; i++) {
			String line = getLine(doc, i);
			line = line.replaceFirst("^\\[[^\\s\\]]+\\] ", ""); // remove and prefix if any
			if (i != 0) { // first line can be empty
				assertNotEquals("Empty line " + i, "", line);
			}
			assertFalse("Trailing whitespace in line " + i, RIGHT_INDENT.matcher(line).find());

			boolean causedBy = line.trim().startsWith("Caused by: ");
			boolean suppressed = line.trim().startsWith("Suppressed: ");
			if (causedBy || (suppressed && !firstSuppress)) {
				allowedIndentChange = -1;
			}

			int lineIndent = getLineIndentation(line);
			if (allowedIndentChange < 0) {
				assertTrue("Wrong indented line " + i + ": " + lastIndent + " > " + lineIndent, lastIndent > lineIndent);
			} else if (allowedIndentChange == 0) {
				assertEquals("Mixed indentation in line " + i, lastIndent, lineIndent);
			} else if (allowedIndentChange > 0) {
				assertTrue("Wrong indented line " + i + ": " + lastIndent + " < " + lineIndent, lastIndent < lineIndent);
			}
			lastIndent = lineIndent;
			allowedIndentChange = 0;
			if (causedBy || suppressed || i == startLine) {
				allowedIndentChange = 1;
			}
			firstSuppress &= !suppressed;
		}
	}

	protected int getLineIndentation(String line) {
		int tabSize = 4;
		String indent = "";
		Matcher m = LEFT_INDENT.matcher(line);
		if (m.find()) {
			indent = m.group();
		}
		int tabCount = indent.length() - indent.replace("\t", "").length();
		return indent.length() + (tabSize - 1) * tabCount;
	}

	/**
	 * Set given text, invoke formatting and wait until finished.
	 *
	 * @param text
	 *            new console text
	 * @return the consoles document
	 */
	protected IDocument consoleDocumentFormatted(String text) throws Exception {
		IDocument document = sync(() -> {
			IDocument doc = fConsole.getDocument();
			doc.set(text);
			fConsole.format();
			return doc;
		});
		// wait for document being formatted
		TestUtil.waitForJobs(getName(), 30, 1000);
		return document;
	}

	protected String[] linkTextsAtPositions(int... offsets) throws BadLocationException {
		IDocument document = fConsole.getDocument();

		List<String> texts = new ArrayList<>(offsets.length);
		List<Position> positions = linkPositions(offsets);
		for (Position pos : positions) {
			String matchText = document.get(pos.getOffset(), pos.getLength());
			texts.add(matchText);
		}
		return texts.toArray(new String[texts.size()]);
	}

	protected List<Position> linkPositions(int... offsets) {
		List<Position> filteredPositions = new ArrayList<>(offsets.length);
		for (Position position : allLinkPositions()) {
			for (int offset : offsets) {
				if (offset >= position.getOffset() && offset <= (position.getOffset() + position.getLength())) {
					filteredPositions.add(position);
					break;
				}
			}
		}
		return filteredPositions;
	}

	protected Position[] allLinkPositions() {
		try {
			return fConsole.getDocument().getPositions(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
		} catch (BadPositionCategoryException ex) {
			// no hyperlinks
		}
		return new Position[0];
	}

	protected String allLinks() {
		return Arrays.toString(allLinkPositions());
	}

	/**
	 * Check if initial content is shown.
	 */
	public void assertInitialContent() {
		assertEquals("Console not loaded with initial content.", ConsoleMessages.JavaStackTraceConsole_0, fConsole.getDocument().get());
	}

	/**
	 * Tries to get the viewer of the currently tested console.
	 *
	 * @return the consoles viewer
	 */
	protected TextConsoleViewer getConsolesViewer() {
		TestUtil.waitForJobs(getName(), 100, DEFAULT_TIMEOUT);
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		assertNotNull(workbenchWindow);
		IWorkbenchPage activePage = workbenchWindow.getActivePage();
		assertNotNull(activePage);
		JavaStackTraceConsolePage page = null;
		for (IViewReference vref : activePage.getViewReferences()) {
			IViewPart view = vref.getView(false);
			if (view instanceof ConsoleView) {
				ConsoleView consoleView = (ConsoleView) view;
				if (consoleView.getConsole() == fConsole && consoleView.getCurrentPage() instanceof JavaStackTraceConsolePage) {
					page = (JavaStackTraceConsolePage) consoleView.getCurrentPage();
					break;
				}
			}
		}
		assertNotNull(page);
		return page.getViewer();
	}

	/**
	 * Simulate user pressing a key.
	 *
	 * @param widget
	 *            widget to type in
	 * @param c
	 *            character to type
	 */
	protected void doKeyStroke(StyledText widget, char c) {
		final Event e = new Event();
		e.character = c;
		widget.notifyListeners(SWT.KeyDown, e);
		widget.notifyListeners(SWT.KeyUp, e);
	}
}
