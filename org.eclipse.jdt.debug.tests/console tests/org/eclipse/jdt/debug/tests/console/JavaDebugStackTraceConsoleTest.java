/*******************************************************************************
 * Copyright (c) 2014, 2020 SAP SE and others.
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
 *     Paul Pazderski - Bug 546900: Tests to check initial console content and content persistence
 *     Paul Pazderski - Bug 343023: Tests for 'clear initial content on first edit'
 *     Paul Pazderski - Bug 304219: Tests for formatting
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.console;

import static org.junit.Assert.assertArrayEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.internal.debug.ui.console.JavaDebugStackTraceConsoleTracker;
import org.eclipse.jdt.internal.debug.ui.console.JavaDebugStackTraceHyperlink;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests for hyper-links added for stack traces copied from the {@code Debug} view. See {@link JavaDebugStackTraceHyperlink} and
 * {@link JavaDebugStackTraceConsoleTracker}.
 */
public class JavaDebugStackTraceConsoleTest extends AbstractJavaStackTraceConsoleTest {

	public JavaDebugStackTraceConsoleTest(String name) {
		super(name);
	}

	public void testLinkNavigation() throws Exception {
		String projectName = JavaDebugStackTraceConsoleTest.class.getSimpleName();
		IJavaProject project = createProject(projectName, "testfiles/source/", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, true);
		waitForBuild();
		waitForJobs();

		consoleDocumentWithText("SomeClass.someMethod() line: 26");

		IHyperlink[] hyperlinks = fConsole.getHyperlinks();
		assertEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				2, hyperlinks.length);

		String expectedText = "\t\tsomeField = new Vector();";
		try {
			for (IHyperlink hyperlink : hyperlinks) {
				closeAllEditors();
				hyperlink.linkActivated();
				IEditorPart editor = waitForEditorOpen();
				String[] selectedText = new String[1];
				sync(() -> selectedText[0] = getSelectedText(editor));
				assertEquals("Wrong text selected after hyperlink navigation", expectedText, selectedText[0]);
			}
		} finally {
			closeAllEditors();
			boolean force = true;
			project.getProject().delete(force, new NullProgressMonitor());
		}
	}

	public void testNotAvailableLineLocation() throws Exception {
		consoleDocumentWithText("SomeClass.someMethod() line: not available [native method]");

		assertArrayEquals("Wrong hyperlinks", new Position[0], allLinkPositions());
	}

	public void testInvalidLineLocation() throws Exception {
		consoleDocumentWithText("SomeClass.someMethod() line: a1");

		assertArrayEquals("Wrong hyperlinks", new Position[0], allLinkPositions());
	}

	public void testHyperlink() throws Exception {
		consoleDocumentWithText("SomeClass.someMethod() line: 11");

		String[] matchTexts = linkTextsAtPositions(0, 24);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "SomeClass", "line: 11" }, matchTexts);
	}

	public void testEmptySpaces() throws Exception {
		consoleDocumentWithText("\t \t SomeClass.someMethod() line: 11  \t\t ");

		String[] matchTexts = linkTextsAtPositions(4, 28);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "SomeClass", "line: 11" }, matchTexts);
	}

	public void testInnerClass() throws Exception {
		consoleDocumentWithText("SomeClass$5.someMethod() line: 1155");

		String[] matchTexts = linkTextsAtPositions(0, 26);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "SomeClass", "line: 1155" }, matchTexts);


		consoleDocumentWithText("SomeClass2$Inner1$Inner2.someMethod() line: 1256");

		matchTexts = linkTextsAtPositions(0, 39);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "SomeClass2", "line: 1256" }, matchTexts);
	}

	public void testSubtype() throws Exception {
		consoleDocumentWithText("Subtype(Type).someMethod() line: 999");

		String[] matchTexts = linkTextsAtPositions(9, 28);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "Type", "line: 999" }, matchTexts);
	}

	public void testQualifiedClass() throws Exception {
		consoleDocumentWithText("somewhere1.somewhere2.SomeClass.someMethod() line: 123");

		String[] matchTexts = linkTextsAtPositions(0, 46);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "somewhere1.somewhere2.SomeClass", "line: 123" }, matchTexts);
	}

	public void testParameterizedClass() throws Exception {
		consoleDocumentWithText("SomeClass<SomeType>.someMethod() line: 504");

		String[] matchTexts = linkTextsAtPositions(0, 34);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "SomeClass", "line: 504" }, matchTexts);
	}

	public void testParameterizedSuperType() throws Exception {
		consoleDocumentWithText("Subtype<T>(Type<K>).someMethod() line: 704");

		String[] matchTexts = linkTextsAtPositions(12, 34);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "Type", "line: 704" }, matchTexts);
	}

	public void testMultipleTypeParameters() throws Exception {
		consoleDocumentWithText("ReferencePipeline$Head<E_IN,E_OUT>.forEach(Consumer<? super E_OUT>) line: 658");

		String[] matchTexts = linkTextsAtPositions(0, 69);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(), new String[] { "ReferencePipeline", "line: 658" }, matchTexts);
	}

	public void testMethodParameters() throws Exception {
		consoleDocumentWithText("Some3Class.someMethod(SomeType[]) line: 301");

		String[] matchTexts = linkTextsAtPositions(0, 36);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "Some3Class", "line: 301" }, matchTexts);


		consoleDocumentWithText("SomeClass55.someMethod(Type1, SomeType2...) line: 111");

		matchTexts = linkTextsAtPositions(0, 45);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "SomeClass55", "line: 111" }, matchTexts);


		consoleDocumentWithText("Some1Class101.someMethod(Type1<Type10>, SomeType2) line: 1");

		matchTexts = linkTextsAtPositions(0, 52);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "Some1Class101", "line: 1" }, matchTexts);


		consoleDocumentWithText("Some1101Class101.someMethod(Type1<?>, SomeType2<?>) line: 12");

		matchTexts = linkTextsAtPositions(0, 53);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "Some1101Class101", "line: 12" }, matchTexts);
	}

	public void testMixedCases() throws Exception {
		consoleDocumentWithText("org.somewhere.Some3Class.someMethod(org.eclipse.SomeType<?>) line: 3011\t");

		String[] matchTexts = linkTextsAtPositions(0, 62);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "org.somewhere.Some3Class", "line: 3011" }, matchTexts);


		consoleDocumentWithText("  org.SomeClass55(org.eclipse.SuperClass).myMethod(org.Type1<com.SomeType3>, com.SomeType2...) line: 10 ");

		matchTexts = linkTextsAtPositions(19, 96);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "org.eclipse.SuperClass", "line: 10" }, matchTexts);


		consoleDocumentWithText("\torg.MyType(somewhere1.somewhere2.Some1Class101$org.Type<com.AnotherType>).toString(java.lang.String[], int) line: 2 ");

		matchTexts = linkTextsAtPositions(13, 110);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "somewhere1.somewhere2.Some1Class101", "line: 2" }, matchTexts);


		consoleDocumentWithText(" org.MyType2(somewhere1.somewhere2.Some1Class105<my.Type>$org.Type<com.AnotherType>$com.InnerType).toString(java.lang.List<org.SomeClass>, byte, long,double) line: 5   ");

		matchTexts = linkTextsAtPositions(14, 159);
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(),
				new String[] { "somewhere1.somewhere2.Some1Class105", "line: 5" }, matchTexts);
	}

	public void testStackTraceDetectionWithColors() throws Exception {
		String exception = ""
				+ "Caused by\033[m: java.lang.NullPointerException: \033[1;31mCannot invoke \"org.eclipse.equinox.internal.provisional.p2.core.eventbus.IProvisioningEventBus.addListener(org.eclipse.equinox.internal.provisional.p2.core.eventbus.ProvisioningListener)\" because \"this.eventBus\" is null\033[m\n"
				+ "    \033[1mat\033[m org.eclipse.equinox.internal.p2.repository.helpers.AbstractRepositoryManager.<init> (\033[1mAbstractRepositoryManager.java:107\033[m)\n"
				+ "    \033[1mat\033[m org.eclipse.equinox.internal.p2.artifact.repository.ArtifactRepositoryManager.<init> (\033[1mArtifactRepositoryManager.java:41\033[m)\n"
				// ...
				+ "    \033[1mat\033[m java.lang.reflect.Method.invoke (\033[1mMethod.java:568\033[m)\n"
				+ "    \033[1mat\033[m org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced (\033[1mLauncher.java:282\033[m)\n"
				+ "    \033[1mat\033[m org.codehaus.plexus.classworlds.launcher.Launcher.launch (\033[1mLauncher.java:225\033[m)\n"
				+ "    \033[1mat\033[m org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode (\033[1mLauncher.java:406\033[m)\n"
				+ "    \033[1mat\033[m org.codehaus.plexus.classworlds.launcher.Launcher.main (\033[1mLauncher.java:347\033[m)\n";
		consoleDocumentWithText(exception);

		Position[] positions = allLinkPositions();
		int[] offsets = new int[positions.length];
		for (int i = 0; i < offsets.length; i++) {
			offsets[i] = positions[i].getOffset();
		}

		String[] matchTexts = linkTextsAtPositions(offsets);
		String[] expected = { "java.lang.NullPointerException", "AbstractRepositoryManager.java:107", "ArtifactRepositoryManager.java:41",
				"Method.java:568", "Launcher.java:282", "Launcher.java:225", "Launcher.java:406", "Launcher.java:347" };
		assertArrayEquals("Wrong hyperlinks, listing all links: " + allLinks(), expected, matchTexts);
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
		AtomicReference<IEditorPart> editor = new AtomicReference<>();
		sync(() -> editor.set(getActivePage().getActiveEditor()));
		long timeoutNanos = System.nanoTime() + 30_000 * 1_000_000L;
		while (editor.get() == null && System.nanoTime() < timeoutNanos) {
			waitForJobs();
			sync(() -> editor.set(getActivePage().getActiveEditor()));
		}
		if (editor.get() == null) {
			throw new AssertionError("Timeout occurred while waiting for editor to open");
		}
		return editor.get();
	}

	private void waitForJobs() throws Exception {
		TestUtil.waitForJobs(getName(), 250, 10_000);
	}
}
