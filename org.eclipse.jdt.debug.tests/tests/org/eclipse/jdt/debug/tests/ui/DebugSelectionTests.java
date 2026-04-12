/*******************************************************************************
 * Copyright (c) 2026 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.debug.tests.ui;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.test.OrderedTestSuite;
import org.eclipse.ui.texteditor.ITextEditor;

import junit.framework.Test;

public class DebugSelectionTests extends AbstractDebugUiTests {

	public static Test suite() {
		return new OrderedTestSuite(DebugSelectionTests.class);
	}

	private IJavaProject project;

	public DebugSelectionTests(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return project;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		project = createProject("DebugSelectionTests", "testfiles/DebugSelectionTests/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
	}

	@Override
	public void tearDown() throws Exception {
		closeAllEditors();
		project.getProject().delete(true, null);
		super.tearDown();
	}

	/**
	 * Resume at lambda chain and at each resume expect selecting the next lambda expression.
	 */
	public void testLambdaEditorSelection() throws Exception {
		IJavaThread thread = null;
		try {
			String typeName = "selectiontests.LambdaSelectionTest";
			createLineBreakpoint(27, typeName);
			ILaunchConfiguration config = createLaunchConfiguration(project, typeName);
			thread = launchAndSuspend(config);
			resume(thread);
			waitForSelection("s -> s.toLowerCase()", 10_000L);
			resume(thread);
			waitForSelection("s -> s.equals(\"b\")", 10_000L);
		} finally {
			removeAllBreakpoints();
			if (thread != null) {
				terminateAndRemove(thread);
			}
		}
	}

	private static void waitForSelection(String expectedSelection, long timeout) throws InterruptedException {
		long s = System.currentTimeMillis();
		while (System.currentTimeMillis() - s < timeout) {
			if (expectedSelection.equals(getActiveEditorSelectionText())) {
				return;
			}
			TestUtil.runEventLoop();
			Thread.sleep(50L);
		}
		assertEquals("Timed out while waiting for selection", expectedSelection, getActiveEditorSelectionText());
	}

	private static String getActiveEditorSelectionText() {
		return callInUi(() -> {
			ITextEditor editor = (ITextEditor) getActivePage().getActiveEditor();
			if (editor != null) {
				ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
				if (selection != null) {
					return selection.getText();
				}
			}
			return "";
		});
	}
}
