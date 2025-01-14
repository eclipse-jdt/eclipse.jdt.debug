/*******************************************************************************
 * Copyright (c) 2023 Andrey Loskutov (loskutov@gmx.de) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.debug.tests.ui;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.test.OrderedTestSuite;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

import junit.framework.Test;

public class JavaSnippetEditorTest extends AbstractDebugUiTests {

	public static Test suite() {
		return new OrderedTestSuite(JavaSnippetEditorTest.class);
	}

	private static final String EXPRESSION = "2 + 2";

	private IJavaProject project;

	private IFile scrapbook;

	public JavaSnippetEditorTest(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get18Project();
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		closeAllEditors();
		project = getProjectContext();
		scrapbook = project.getProject().getFile("scrapbook.jpage");
		scrapbook.create(new ByteArrayInputStream(EXPRESSION.getBytes()), true, null);
	}

	@Override
	public void tearDown() throws Exception {
		closeAllEditors();
		scrapbook.delete(true, null);
		super.tearDown();
	}

	/**
	 * Tests if we can open scrapbook editor and evaluate 2+2 expression
	 */
	public void testEvaluation() throws Exception {
		JavaSnippetEditor snippetEditor = (JavaSnippetEditor) openEditor(scrapbook);
		processUiEvents();
		IDocumentProvider documentProvider = snippetEditor.getDocumentProvider();
		IDocument document = documentProvider.getDocument(new FileEditorInput(scrapbook));
		String originalText = document.get();
		assertEquals("Unexpected content", EXPRESSION, originalText);

		// Select expression and trigger evaluation
		sync(() -> {
			ISelectionProvider selectionProvider = snippetEditor.getSelectionProvider();
			selectionProvider.setSelection(new TextSelection(0, EXPRESSION.length()));
			processUiEvents();
			TextSelection selection = (TextSelection) selectionProvider.getSelection();
			assertEquals("Wrong selection offset", 0, selection.getOffset());
			assertEquals("Wrong selection line", 0, selection.getStartLine());
			assertEquals("Wrong selection length", EXPRESSION.length(), selection.getLength());

			// Starts evaluation that is supposed to write result directly to the editor
			snippetEditor.evalSelection(JavaSnippetEditor.RESULT_DISPLAY);
		});

		// Evaluation runs in a separated thread (not a job), so let wait for it
		long timeoutNanos = System.nanoTime() + 60_000 * 1_000_000L;
		while (snippetEditor.isEvaluating() && System.nanoTime() < timeoutNanos) {
			processUiEvents(1000);
		}

		String newText = document.get();
		assertEquals("Editor should show evaluation result", EXPRESSION + "(int) 4", newText);
	}

}
