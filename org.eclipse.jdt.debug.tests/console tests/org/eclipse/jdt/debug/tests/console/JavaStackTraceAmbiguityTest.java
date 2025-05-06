/*******************************************************************************
 * Copyright (c) 2024, 2025 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.console;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

public class JavaStackTraceAmbiguityTest extends AbstractJavaStackTraceConsoleTest {

	public JavaStackTraceAmbiguityTest(String name) {
		super(name);
	}

	public void testLinkNavigationTrueForNoParameters() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("Sample.tes3() line: 31");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 2, hyperlinks.length);
		String expectedText = "System.out.println(\"Expected_Zero_Parameter\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForSingleParameter() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("Sample.tes3(int) line: 31");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 2, hyperlinks.length);
		String expectedText = "System.out.println(\"Expected_Single_Parameter\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForMultipleParameters() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("Sample.tes3(int, String) line: 31");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 2, hyperlinks.length);
		String expectedText = "System.out.println(\"Expected_Multiple_Parameter\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForMultipleParameters_Three() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("Sample.tes3(int, String,Sample) line: 34");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 2, hyperlinks.length);
		String expectedText = "System.out.println(\"Expected_Multiple_Parameter_Three\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForOneNormalAndOneFullyQualifiedArguments() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("Sample.tesComplex(String[], URL[]) line: 37");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 2, hyperlinks.length);
		String expectedText = "System.out.println(\"Expected_One_normal_&_One_fully_qualified\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForBothFullyQualifiedArguments() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("Sample.tesComplex(Object, URL[]) line: 37");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 2, hyperlinks.length);
		String expectedText = "System.out.println(\"Expected_both_fully_qualified\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForSingleVarArgs() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("Sample.testMethod(Object...) line: 43");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 2, hyperlinks.length);
		String expectedText = "System.out.println(\"Expected_oneVarArgs\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForSingleVarArgsAndOneNormal() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("Sample.testMethod(Object,Object...) line: 40");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 2, hyperlinks.length);
		String expectedText = "System.out.println(\"Expected_oneNormal&oneVarArgs\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForGenerics() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("SampleGenerics<E>(SampleGenerics).remove() line: 25");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 2, hyperlinks.length);
		String expectedText = "System.out.print(\"EXPECTED_GENERICS\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForInnerClass() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("InnerClassTest$innerL1.check() line: 23");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 2, hyperlinks.length);
		String expectedText = "System.out.println(\"EXPECTED_INNERCLASS\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForInnerClassMultilevel() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("InnerClassTest$innerL1$innerL2.check2() line: 27");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 2, hyperlinks.length);
		String expectedText = "System.out.println(\"EXPECTED_INNER-INNER_CLASS\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForInnerClassParameters() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("MyScheduledExecutor(Worker).doWork(MyScheduledExecutor$Wor) line: 20");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 2, hyperlinks.length);
		String expectedText = "System.out.println(\"Expected_Result\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForInnerClassMultiParameters() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("MyScheduledExecutor(Worker).doWorkParams(MyScheduledExecutor$Wor,MyScheduledExecutor$Ran) line: 23");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 2, hyperlinks.length);
		String expectedText = "System.out.println(\"Expected_Result\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForLinksWithNoProperMethodSignature() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("Sample.testBlank(Sample.java:40)");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 1, hyperlinks.length);
		String expectedText = "System.out.println(\"Expected_No_Signature\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForLinksWithVarArgs() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("Sample.tes2(Object, Object...) line: 40 ");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 2, hyperlinks.length);
		String expectedText = "System.out.println(\"Expected_VarArgs\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForLinksWithSingleSpaceBeforeSignature() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("at a.Sample.testBlank (Sample.java:40)");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 1, hyperlinks.length);
		String expectedText = "System.out.println(\"Expected_No_Signature\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForLinksWithMultiSpaceBeforeSignature() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("at a.Sample.testBlank\t\t(Sample.java:40)");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 1, hyperlinks.length);
		String expectedText = "System.out.println(\"Expected_No_Signature\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testLinkNavigationTrueForLinksWithTimeStamps1() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("250420 12:59:13.999 (SampleGenerics.java:25) Hello");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 1, hyperlinks.length);
		String expectedText = "System.out.print(\"EXPECTED_GENERICS\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}
	public void testLinkNavigationTrueForLinksWithTimeStamps2() throws Exception {
		String projectName = "StackTest";
		IJavaProject project = createProject(projectName, "testfiles/AmbiguityTest/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
		waitForBuild();
		waitForJobs();
		consoleDocumentWithText("2025-04-20 12.01.23 (SampleGenerics.java:25) Hello");
		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(), 1, hyperlinks.length);
		String expectedText = "System.out.print(\"EXPECTED_GENERICS\");";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				selectedText[0] = selectedText[0].trim();
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);

			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}
	private void waitForJobs() throws Exception {
		TestUtil.waitForJobs(getName(), 250, 10_000);
	}

	private static String getSelectedText(IEditorPart editor) {
		ITextEditor textEditor = (ITextEditor) editor;
		ISelectionProvider selectionProvider = textEditor.getSelectionProvider();
		ISelection selection = selectionProvider.getSelection();
		ITextSelection textSelection = (ITextSelection) selection;
		String selectedText = textSelection.getText();
		return selectedText;
	}

	private IEditorPart waitForEditorOpen() throws Exception {
		waitForJobs();
		IEditorPart[] editor = new IEditorPart[1];
		sync(() -> editor[0] = getActivePage().getActiveEditor());
		long timeout = 30_000;
		long start = System.currentTimeMillis();
		while (editor[0] == null && System.currentTimeMillis() - start < timeout) {
			waitForJobs();
			sync(() -> editor[0] = getActivePage().getActiveEditor());
		}
		if (editor[0] == null) {
			throw new AssertionError("Timeout occurred while waiting for editor to open");
		}
		return editor[0];
	}
}
