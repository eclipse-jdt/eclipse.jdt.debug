/*******************************************************************************
 * Copyright (c) 2026, Daniel Schmid and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Daniel Schmid - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.sourcelookup;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.sourcelookup.JavaStackFrameSourceDisplayAdapter;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.PlatformUI;

public class ClassFileEditorHighlightingTest extends AbstractDebugTest {

	private static final String CLASS_NAME = "OneToTen";

	public ClassFileEditorHighlightingTest(String name) {
		super(name);
	}

	public void test123() throws Exception {
		IJavaProject javaProject = getProjectContext();
		createLineBreakpoint(21, CLASS_NAME);

		JavaStackFrameSourceDisplayAdapter sourceDisplay = new JavaStackFrameSourceDisplayAdapter();

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(CLASS_NAME);

			ClassFileEditor editor = (ClassFileEditor) EditorUtility.openInEditor(
					javaProject.getProject().getFile("bin/" + CLASS_NAME + ".class")
			);
			IClassFileEditorInput editorInput = (IClassFileEditorInput) editor.getEditorInput();
			IClassFile classFile = editorInput.getClassFile();
			thread.getTopStackFrame().getLaunch().setSourceLocator(stackFrame -> classFile);

			sourceDisplay.displaySource(thread.getTopStackFrame(), PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), true);
			StyledText noSourceTextWidget = editor.getNoSourceTextWidget();
			StyleRange[] styleRanges = noSourceTextWidget.getStyleRanges();
			assertEquals(1, styleRanges.length);
			String highlightedText = noSourceTextWidget.getContent().getTextRange(styleRanges[0].start, styleRanges[0].length);
			assertEquals("     0  getstatic java.lang.System.out : java.io.PrintStream [16]", highlightedText);

			stepOver((IJavaStackFrame) thread.getTopStackFrame());
			IStackFrame topStackFrame = thread.getTopStackFrame();
			sourceDisplay.displaySource(topStackFrame, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), true);
			styleRanges = noSourceTextWidget.getStyleRanges();
			assertEquals(1, styleRanges.length);
			highlightedText = noSourceTextWidget.getContent().getTextRange(styleRanges[0].start, styleRanges[0].length);
			assertEquals("     8  getstatic java.lang.System.out : java.io.PrintStream [16]", highlightedText);

			thread.resume();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
