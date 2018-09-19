/*******************************************************************************
 * Copyright (c) 2014 SAP SE and others.
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
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.console;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsole;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.internal.console.ConsoleHyperlinkPosition;

/**
 * Tests {@link JavaStackTraceConsole}
 */
public class JavaStackTraceConsoleTest extends AbstractDebugTest {

	private JavaStackTraceConsole fConsole;

	public JavaStackTraceConsoleTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		fConsole = new JavaStackTraceConsole();
		consoleManager.addConsoles(new IConsole[] { fConsole });
	}

	@Override
	protected void tearDown() throws Exception {
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		consoleManager.removeConsoles(new IConsole[] { fConsole });

		super.tearDown();
	}

	public void testHyperlinkMatchSignatureSimple() throws Exception {
		consoleDocumentWithText("at foo.bar.Type.method1(Type.java:1)");

		String[] matchTexts = linkTextsAtPositions(24);
		assertArrayEquals(allLinks(), new String[] { "Type.java:1" }, matchTexts);
	}

	public void testHyperlinkMatchSignatureExtended() throws Exception {
		consoleDocumentWithText("at foo.bar.Type.method1(IILjava/lang/String;)V(Type.java:1)");

		String[] matchTexts = linkTextsAtPositions(47);
		assertArrayEquals(allLinks(), new String[] { "Type.java:1" }, matchTexts);
	}

	public void testHyperlinkMatchMultiple() throws Exception {
		consoleDocumentWithText("at foo.bar.Type.method2(Type.java:2)\n" //
				+ "at foo.bar.Type.method1(Type.java:1)");

		String[] matchTexts = linkTextsAtPositions(24, 61);
		assertArrayEquals(allLinks(), new String[] { "Type.java:2", "Type.java:1" }, matchTexts);
	}

	public void testHyperlinkMatchInvalidLine() throws Exception {
		consoleDocumentWithText("at foo.bar.Type.method1(Type.java:fff)");

		String[] matchTexts = linkTextsAtPositions(24);
		assertArrayEquals(allLinks(), new String[] { "Type.java:fff" }, matchTexts);
	}

	public void testHyperlinkNoMatch() throws Exception {
		consoleDocumentWithText("at foo.bar.Type.method1(foo.bar.Type.java:42)");

		Position[] positions = allLinkPositions();
		assertArrayEquals("Expected no hyperlinks for invalid type name", new Position[0], positions);
	}

	private IDocument consoleDocumentWithText(String text) throws InterruptedException {
		IDocument document = fConsole.getDocument();
		document.set(text);
		// wait for document being parsed and hyperlinks created
		Job.getJobManager().join(fConsole, null);
		return document;
	}

	private String[] linkTextsAtPositions(int... offsets) throws BadLocationException {
		IDocument document = fConsole.getDocument();

		List<String> texts = new ArrayList<>(offsets.length);
		List<Position> positions = linkPositions(offsets);
		for (Position pos : positions) {
			String matchText = document.get(pos.getOffset(), pos.getLength());
			texts.add(matchText);
		}
		return texts.toArray(new String[texts.size()]);
	}

	private List<Position> linkPositions(int... offsets) {
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

	private Position[] allLinkPositions() {
		try {
			return fConsole.getDocument().getPositions(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
		}
		catch (BadPositionCategoryException ex) {
			// no hyperlinks
		}
		return new Position[0];
	}

	private String allLinks() {
		return Arrays.toString(allLinkPositions());
	}

}
