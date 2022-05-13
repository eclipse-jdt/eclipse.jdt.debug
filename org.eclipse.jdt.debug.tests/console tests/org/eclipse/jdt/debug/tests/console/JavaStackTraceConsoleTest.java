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
import static org.junit.Assert.assertNotEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsole;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.custom.ST;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.TextConsoleViewer;

/**
 * Tests {@link JavaStackTraceConsole}
 */
public class JavaStackTraceConsoleTest extends AbstractJavaStackTraceConsoleTest {

	public JavaStackTraceConsoleTest(String name) {
		super(name);
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
		consoleDocumentWithText("at foo.bar.Type.method1(foo.bar#.Type.java:42)");

		Position[] positions = allLinkPositions();
		assertArrayEquals("Expected no hyperlinks for invalid type name", new Position[0], positions);
	}

	public void testBug489365_unicodeMatch() throws Exception {
		consoleDocumentWithText("at com.example.Fran\u00E7ais.de\u0301butant(Fran\u00E7ais.java:101)\n" // "Latin Small Letter C with Cedilla"
				+ "at com.example.Franc\u0327ais.de\u0301butant(Franc\u0327ais.java:101)\n" // "Latin Small Letter C" + "Combining Cedilla"
				+ "at Exc\u00E4ption.main(Exc\u00E4ption.java:4)\n" // "Latin Small Letter A with Diaeresis"
				+ "at Exca\u0308ption.main(Exca\u0308ption.java:4)"); // "Latin Small Letter A" + "Combining Diaeresis"

		String[] matchTexts = linkTextsAtPositions(34, 88, 126, 163);
		assertArrayEquals(allLinks(), new String[] { "Fran\u00E7ais.java:101", "Franc\u0327ais.java:101",
				"Exc\u00E4ption.java:4", "Exca\u0308ption.java:4" }, matchTexts);
	}

	/**
	 * Test save/restore of stack trace console content on console close/reactivation.
	 */
	public void testLoadAndSaveDocument() throws Exception {
		IDocument initialDocument = fConsole.getDocument();
		String storedContent = "at foo.bar.Type.method1(Type.java:fff)";
		consoleDocumentWithText(storedContent);
		sync(() -> removeConsole(true));

		Path file = Paths.get(JavaStackTraceConsole.FILE_NAME);
		assertTrue("Content was not stored.", Files.exists(file));
		assertTrue("Content was not stored.", Files.size(file) > 0);

		sync(() -> initConsole(false));
		assertNotSame("Failed to create new console.", initialDocument, fConsole.getDocument());
		assertEquals("Failed to restore previous content.", storedContent, fConsole.getDocument().get());
	}

	/**
	 * Test for Bug 343023. Test if initial content is cleared on first edit and not cleared if it was changed programmatically before.
	 */
	public void testClearInitialContent() {
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			// type at start of initial content
			TextConsoleViewer viewer = getConsolesViewer();
			viewer.getTextWidget().invokeAction(ST.TEXT_START);
			doKeyStroke(viewer.getTextWidget(), 'a');
			assertEquals("Initial content was not cleared.", "a", fConsole.getDocument().get());
			doKeyStroke(viewer.getTextWidget(), 'b');
			assertEquals("Content was cleared again.", "ab", fConsole.getDocument().get());
			removeConsole(false);

			// type inside initial content
			initConsole(true);
			viewer = getConsolesViewer();
			viewer.getTextWidget().setCaretOffset(5);
			doKeyStroke(viewer.getTextWidget(), 'a');
			assertEquals("Initial content was not cleared.", "a", fConsole.getDocument().get());
			doKeyStroke(viewer.getTextWidget(), 'b');
			assertEquals("Content was cleared again.", "ab", fConsole.getDocument().get());
			removeConsole(false);

			// type at end of initial content
			initConsole(true);
			viewer = getConsolesViewer();
			viewer.getTextWidget().invokeAction(ST.TEXT_END);
			doKeyStroke(viewer.getTextWidget(), 'a');
			assertEquals("Initial content was not cleared.", "a", fConsole.getDocument().get());
			doKeyStroke(viewer.getTextWidget(), 'b');
			assertEquals("Content was cleared again.", "ab", fConsole.getDocument().get());
			removeConsole(false);

			// select all initial content
			initConsole(true);
			viewer = getConsolesViewer();
			viewer.getTextWidget().selectAll();
			doKeyStroke(viewer.getTextWidget(), 'a');
			assertEquals("Initial content was not cleared.", "a", fConsole.getDocument().get());
			doKeyStroke(viewer.getTextWidget(), 'b');
			assertEquals("Content was cleared again.", "ab", fConsole.getDocument().get());
			removeConsole(false);

			// select part of initial content
			initConsole(true);
			viewer = getConsolesViewer();
			viewer.getTextWidget().setSelection(5, 10);
			doKeyStroke(viewer.getTextWidget(), 'a');
			assertEquals("Initial content was not cleared.", "a", fConsole.getDocument().get());
			doKeyStroke(viewer.getTextWidget(), 'b');
			assertEquals("Content was cleared again.", "ab", fConsole.getDocument().get());
			removeConsole(false);

			// paste inside initial content
			initConsole(true);
			viewer = getConsolesViewer();
			viewer.getTextWidget().setCaretOffset(5);
			viewer.getTextWidget().insert("at foo.bar.Type.method1(IILjava/lang/String;)V(Type.java:1)");
			assertEquals("Initial content was not cleared.", "at foo.bar.Type.method1(IILjava/lang/String;)V(Type.java:1)", fConsole.getDocument().get());
			doKeyStroke(viewer.getTextWidget(), 'b');
			assertTrue("Content was cleared again.", fConsole.getDocument().getLength() > 5);
			removeConsole(false);

			// user edit after something already modified the content (expect no magic clear)
			initConsole(true);
			viewer = getConsolesViewer();
			String text = "Text set programmatically";
			viewer.getTextWidget().setText(text);
			viewer.getTextWidget().invokeAction(ST.TEXT_END);
			doKeyStroke(viewer.getTextWidget(), '!');
			assertEquals(text + "!", fConsole.getDocument().get());
			removeConsole(true); // preserve content this time!

			// check first edit does not clear if console is initialized with custom content
			initConsole(false);
			viewer = getConsolesViewer();
			int lengthBefore = fConsole.getDocument().getLength();
			doKeyStroke(viewer.getTextWidget(), 'a');
			assertEquals("Custom content was cleared.", lengthBefore + 1, fConsole.getDocument().getLength());
			// do not remove last console so tearDown can do something
		});
	}

	/** Test formatting of a plain simple stack trace. */
	public void testFormatSimple() throws Exception {
		IDocument doc = consoleDocumentFormatted("java.lang.AssertionError: expected:5 but was:7\n\n"
				+ "at org.junit.Ass\nert.fail(Assert.java:88) \n" + "at\norg.junit.   \nAssert.failNotEquals(Assert.java:834)\n"
				+ "at org.junit.Assert.assertEquals(Assert.java:118)\n" + "at \norg.junit.Assert.assertEquals\n(Assert.java:144)");
		assertEquals("java.lang.AssertionError: expected:5 but was:7", getLine(doc, 0));
		assertEquals("at org.junit.Assert.fail(Assert.java:88)", getLine(doc, 1).trim());
		assertEquals("at org.junit.Assert.failNotEquals(Assert.java:834)", getLine(doc, 2).trim());
		assertEquals("at org.junit.Assert.assertEquals(Assert.java:118)", getLine(doc, 3).trim());
		assertEquals("at org.junit.Assert.assertEquals(Assert.java:144)", getLine(doc, 4).trim());
		checkIndentationConsistency(doc, 0);
	}

	/** Test formatting of a stack trace including thread name. */
	public void testFormatThreadName() throws Exception {
		IDocument doc = consoleDocumentFormatted("Exception in thread \"ma\nin\" java.lang.NullPointerException\n"
				+ "at \nStacktrace.main(Stacktrace.java:4)");
		assertEquals("Exception in thread \"main\" java.lang.NullPointerException", getLine(doc, 0));
		assertEquals("at Stacktrace.main(Stacktrace.java:4)", getLine(doc, 1).trim());
		checkIndentationConsistency(doc, 0);
	}

	/** Test formatting with some less common method names. */
	public void testFormatUncommonMethods() throws Exception {
		IDocument doc = consoleDocumentFormatted("Stack Trace\n" + "  at org.eclipse.core.runtime.SafeRunner.run\n(SafeRunner.java:43)\n"
				+ "      at org.eclipse.ui.internal.JFaceUtil.lambda$0(JFaceUtil.java:47)\n"
				+ "    at org.eclipse.ui.internal.JFaceUtil$$Lambda$107/0x00000   \n008013c5c40.run(Unknown Source)\n"
				+ "\tat org.eclipse.jface.util.SafeRunnable.run(SafeRunnable.java:174)\n"
				+ "         at java.base@12/java.lang.reflect.Method.invoke(Method.java:567)\n"
				+ " \n at app/\n/org.eclipse.equinox.launcher.Main.main(Main.java:1438)");
		assertEquals("Stack Trace", getLine(doc, 0));
		assertEquals("at org.eclipse.core.runtime.SafeRunner.run(SafeRunner.java:43)", getLine(doc, 1).trim());
		assertEquals("at org.eclipse.ui.internal.JFaceUtil.lambda$0(JFaceUtil.java:47)", getLine(doc, 2).trim());
		assertEquals("at org.eclipse.ui.internal.JFaceUtil$$Lambda$107/0x00000008013c5c40.run(Unknown Source)", getLine(doc, 3).trim());
		assertEquals("at org.eclipse.jface.util.SafeRunnable.run(SafeRunnable.java:174)", getLine(doc, 4).trim());
		assertEquals("at java.base@12/java.lang.reflect.Method.invoke(Method.java:567)", getLine(doc, 5).trim());
		assertEquals("at app//org.eclipse.equinox.launcher.Main.main(Main.java:1438)", getLine(doc, 6).trim());
		checkIndentationConsistency(doc, 0);
	}

	/** Test formatting with a 'locked' entry. */
	public void testFormatLocked() throws Exception {
		IDocument doc = consoleDocumentFormatted("java.lang.Thread.State: RUNNABLE\n"
				+ " at java.net.PlainSocketImpl.socketAccept(Native Method)\n\n\n"
				+ "at java.net.PlainSocketImpl\n.accept(PlainSocketImpl.java:408)\n" + "\t - locked <0x911d3c30>   (a java.net.SocksSocketImpl)\n"
				+ "    at java.net.ServerSocket.implAccept(ServerSocket.java:462)\n" + "at java.net.ServerSocket.accept(ServerSocket.java:430)");
		assertEquals("java.lang.Thread.State: RUNNABLE", getLine(doc, 0));
		assertEquals("at java.net.PlainSocketImpl.socketAccept(Native Method)", getLine(doc, 1).trim());
		assertEquals("at java.net.PlainSocketImpl.accept(PlainSocketImpl.java:408)", getLine(doc, 2).trim());
		assertEquals("- locked <0x911d3c30> (a java.net.SocksSocketImpl)", getLine(doc, 3).trim());
		assertEquals("at java.net.ServerSocket.implAccept(ServerSocket.java:462)", getLine(doc, 4).trim());
		assertEquals("at java.net.ServerSocket.accept(ServerSocket.java:430)", getLine(doc, 5).trim());
		checkIndentationConsistency(doc, 0);
	}

	/** Test formatting with a ... more entry. */
	public void testFormatMore() throws Exception {
		// additional this one is missing the 'header' line and starting with an 'at' line
		IDocument doc = consoleDocumentFormatted(" at org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager.preparePersistenceUnitInfos(DefaultPersistenceUnitManager.java:470)\n"
				+ "    at org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager.afterPropertiesSet(DefaultPersistenceUnitManager.java:424)\n"
				+ "at org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.createNativeEntityManagerFactory(LocalContainerEntityManagerFactoryBean.java:310)\n"
				+ "          at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.afterPropertiesSet(AbstractEntityManagerFactoryBean.java:318)\n\n"
				+ "  \t\t    at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeInitMethods(AbstractAutowireCapableBeanFactory.java:1633)\n"
				+ "   \t \tat org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1570)\n"
				+ "  ... 53 more\n" + "");
		assertEquals("at org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager.preparePersistenceUnitInfos(DefaultPersistenceUnitManager.java:470)", getLine(doc, 1).trim());
		assertEquals("at org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager.afterPropertiesSet(DefaultPersistenceUnitManager.java:424)", getLine(doc, 2).trim());
		assertEquals("at org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.createNativeEntityManagerFactory(LocalContainerEntityManagerFactoryBean.java:310)", getLine(doc, 3).trim());
		assertEquals("at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.afterPropertiesSet(AbstractEntityManagerFactoryBean.java:318)", getLine(doc, 4).trim());
		assertEquals("at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeInitMethods(AbstractAutowireCapableBeanFactory.java:1633)", getLine(doc, 5).trim());
		assertEquals("at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1570)", getLine(doc, 6).trim());
		assertEquals("... 53 more", getLine(doc, 7).trim());
		checkIndentationConsistency(doc, 0);
	}

	/** Test formatting stack trace with cause. */
	public void testFormatCause() throws Exception {
		IDocument doc = consoleDocumentFormatted("HighLevelException:\n LowLevelException\n" + "\tat Junk.a(Junk.java:13)\n"
				+ "         at Junk.main(Junk.java:4)\n" + "     Caused by: LowLevelException\n" + " at Junk.e(Junk.java:30)\n"
				+ "    at Junk.d\n(Junk.java:27)\n" + "at Junk.c(Junk.java:21)");
		assertEquals("HighLevelException: LowLevelException", getLine(doc, 0));
		assertEquals("at Junk.a(Junk.java:13)", getLine(doc, 1).trim());
		assertEquals("at Junk.main(Junk.java:4)", getLine(doc, 2).trim());
		assertEquals("Caused by: LowLevelException", getLine(doc, 3));
		assertEquals("at Junk.e(Junk.java:30)", getLine(doc, 4).trim());
		assertEquals("at Junk.d(Junk.java:27)", getLine(doc, 5).trim());
		assertEquals("at Junk.c(Junk.java:21)", getLine(doc, 6).trim());
		checkIndentationConsistency(doc, 0);

		// nested causes
		doc = consoleDocumentFormatted("HighLevelException:\t MidLevelException:\n LowLevelException\n" + "\tat Junk.a(Junk.java:13)\n"
				+ "    at Junk.main(Junk.java:4)\n" + " Caused by: MidLevelException: LowLevelException\n" + "    at Junk.c(Junk.java:23)\n"
				+ "      at Junk.b(Junk.java:17)\n" + "      at Junk.a(Junk.java:11)\n" + "... 1 more\n" + " Caused by: LowLevelException\n"
				+ "     at Junk.e(Junk.java:30)\n" + "at Junk.d(Junk.java:27)\n" + "   at Junk.c(Junk.java:21)\n" + "         ... 3 more\n");
		assertEquals("HighLevelException: MidLevelException: LowLevelException", getLine(doc, 0));
		assertEquals("at Junk.a(Junk.java:13)", getLine(doc, 1).trim());
		assertEquals("at Junk.main(Junk.java:4)", getLine(doc, 2).trim());
		assertEquals("Caused by: MidLevelException: LowLevelException", getLine(doc, 3));
		assertEquals("at Junk.c(Junk.java:23)", getLine(doc, 4).trim());
		assertEquals("at Junk.b(Junk.java:17)", getLine(doc, 5).trim());
		assertEquals("at Junk.a(Junk.java:11)", getLine(doc, 6).trim());
		assertEquals("... 1 more", getLine(doc, 7).trim());
		assertEquals("Caused by: LowLevelException", getLine(doc, 8));
		assertEquals("at Junk.e(Junk.java:30)", getLine(doc, 9).trim());
		assertEquals("at Junk.d(Junk.java:27)", getLine(doc, 10).trim());
		assertEquals("at Junk.c(Junk.java:21)", getLine(doc, 11).trim());
		assertEquals("... 3 more", getLine(doc, 12).trim());
		checkIndentationConsistency(doc, 0);
	}

	/** Test formatting stack trace with suppressed exceptions. */
	public void testFormatSuppressed() throws Exception {
		IDocument doc = consoleDocumentFormatted("Exception in thread \"main\" java.lang.Exception: Something happened\n" + "at Foo.bar(Native)\n"
				+ "  at Foo.main(Foo.java:5)\n" + "  Suppressed: Resource$CloseFailException: Resource ID = 0\n"
				+ "    at Resource.close(Resource\n.java:26)\n" + "      at Foo.bar(Foo.java)\n" + "         ... 1 more\n" + "");
		assertEquals("Exception in thread \"main\" java.lang.Exception: Something happened", getLine(doc, 0));
		assertEquals("at Foo.bar(Native)", getLine(doc, 1).trim());
		assertEquals("at Foo.main(Foo.java:5)", getLine(doc, 2).trim());
		assertEquals("Suppressed: Resource$CloseFailException: Resource ID = 0", getLine(doc, 3).trim());
		assertEquals("at Resource.close(Resource.java:26)", getLine(doc, 4).trim());
		assertEquals("at Foo.bar(Foo.java)", getLine(doc, 5).trim());
		assertEquals("... 1 more", getLine(doc, 6).trim());
		checkIndentationConsistency(doc, 0);

		// multiple suppressed
		doc = consoleDocumentFormatted("Exception in thread \"main\" java.lang.Exception: Main block\n" + "  at Foo3.main(Foo3.java:7)\n"
				+ "     Suppressed: Resource$CloseFailException: Resource ID = 2\n" + "      at Resource.close(Resource.java:26)\n"
				+ "   \t\tat Foo3.main(Foo3.java:5)\n" + "Suppressed: Resource$CloseFailException: Resource ID = 1\n"
				+ "      at Resource.close(Resource.java:26)\n" + "                at Foo3.main(Foo3.java:5)\n" + "");
		assertEquals("Exception in thread \"main\" java.lang.Exception: Main block", getLine(doc, 0));
		assertEquals("at Foo3.main(Foo3.java:7)", getLine(doc, 1).trim());
		assertEquals("Suppressed: Resource$CloseFailException: Resource ID = 2", getLine(doc, 2).trim());
		assertEquals("at Resource.close(Resource.java:26)", getLine(doc, 3).trim());
		assertEquals("at Foo3.main(Foo3.java:5)", getLine(doc, 4).trim());
		assertEquals("Suppressed: Resource$CloseFailException: Resource ID = 1", getLine(doc, 5).trim());
		assertEquals("at Resource.close(Resource.java:26)", getLine(doc, 6).trim());
		assertEquals("at Foo3.main(Foo3.java:5)", getLine(doc, 7).trim());
		checkIndentationConsistency(doc, 0);
	}

	/** Test formatting stack trace with mixture of cause and suppressed. */
	public void testFormatSuppressedWithCause() throws Exception {
		// exception with suppressed and cause
		IDocument doc = consoleDocumentFormatted("Exception in thread \"main\" java.lang.Exception: Main block\n" + "  at Foo3.main(Foo3.java:7)\n"
				+ "  Suppressed: Resource$CloseFailException: Resource ID = 1\n" + "          at Resource.close(Resource.java:26)\n"
				+ "          at Foo3.main(Foo3.java:5)\n" + "Caused by: java.lang.Exception: I did it\n" + "  at Foo3.main(Foo3.java:8)\n");
		assertEquals("Exception in thread \"main\" java.lang.Exception: Main block", getLine(doc, 0));
		assertEquals("at Foo3.main(Foo3.java:7)", getLine(doc, 1).trim());
		assertEquals("Suppressed: Resource$CloseFailException: Resource ID = 1", getLine(doc, 2).trim());
		assertEquals("at Resource.close(Resource.java:26)", getLine(doc, 3).trim());
		assertEquals("at Foo3.main(Foo3.java:5)", getLine(doc, 4).trim());
		assertEquals("Caused by: java.lang.Exception: I did it", getLine(doc, 5).trim());
		assertEquals("at Foo3.main(Foo3.java:8)", getLine(doc, 6).trim());
		checkIndentationConsistency(doc, 0);
		// Additional indentation check. Since cause is linked to primary exception it must be less indented as suppressed stuff.
		assertEquals(getLineIndentation(getLine(doc, 0)), getLineIndentation(getLine(doc, 5)));
		assertEquals(getLineIndentation(getLine(doc, 1)), getLineIndentation(getLine(doc, 6)));
		assertTrue(getLineIndentation(getLine(doc, 3)) > getLineIndentation(getLine(doc, 5)));
		assertTrue(getLineIndentation(getLine(doc, 3)) > getLineIndentation(getLine(doc, 6)));

		// exception with suppressed and cause for the suppressed
		doc = consoleDocumentFormatted("Exception in thread \"main\" java.lang.Exception: Main block\n" + "  at Foo4.main(Foo4.java:6)\n"
				+ "  Suppressed: Resource2$CloseFailException: Resource ID = 1\n" + "          at Resource2.close(Resource2.java:20)\n"
				+ "          at Foo4.main(Foo4.java:5)\n" + "  Caused by: java.lang.Exception: Rats, you caught me\n"
				+ "          at Resource2$CloseFailException.<init>(Resource2.java:45)\n" + "          ... 2 more\n");
		assertEquals("Exception in thread \"main\" java.lang.Exception: Main block", getLine(doc, 0));
		assertEquals("at Foo4.main(Foo4.java:6)", getLine(doc, 1).trim());
		assertEquals("Suppressed: Resource2$CloseFailException: Resource ID = 1", getLine(doc, 2).trim());
		assertEquals("at Resource2.close(Resource2.java:20)", getLine(doc, 3).trim());
		assertEquals("at Foo4.main(Foo4.java:5)", getLine(doc, 4).trim());
		assertEquals("Caused by: java.lang.Exception: Rats, you caught me", getLine(doc, 5).trim());
		assertEquals("at Resource2$CloseFailException.<init>(Resource2.java:45)", getLine(doc, 6).trim());
		assertEquals("... 2 more", getLine(doc, 7).trim());
		checkIndentationConsistency(doc, 0);
		// Additional indentation check. Since cause is linked to suppressed exception it must be greater indented as primary exception stuff.
		assertNotEquals(getLineIndentation(getLine(doc, 0)), getLineIndentation(getLine(doc, 5)));
		assertEquals(getLineIndentation(getLine(doc, 2)), getLineIndentation(getLine(doc, 5)));
		assertNotEquals(getLineIndentation(getLine(doc, 1)), getLineIndentation(getLine(doc, 6)));
		assertEquals(getLineIndentation(getLine(doc, 3)), getLineIndentation(getLine(doc, 6)));
		assertEquals(getLineIndentation(getLine(doc, 4)), getLineIndentation(getLine(doc, 7)));
		assertTrue(getLineIndentation(getLine(doc, 5)) > getLineIndentation(getLine(doc, 0)));
		assertTrue(getLineIndentation(getLine(doc, 6)) > getLineIndentation(getLine(doc, 1)));
	}

	/** Test formatting the rare [CIRCULAR REFERENCE:...] entry. */
	public void testFormatCircular() throws Exception {
		IDocument doc = consoleDocumentFormatted("Exception in thread \"main\" Stacktrace$BadException\n"
				+ "at Stacktrace.main\n(Stacktrace.java:4)\n" + " Caused by: Stacktrace$BadExceptionCompanion: Stacktrace$BadException\n"
				+ "   at Stacktrace$BadException.<init>(Stacktrace.java:10)\n" + "    ... 1 more\n"
				+ "  [CIRCULAR REFERENCE:Stacktrace$BadException]");
		assertEquals("Exception in thread \"main\" Stacktrace$BadException", getLine(doc, 0));
		assertEquals("at Stacktrace.main(Stacktrace.java:4)", getLine(doc, 1).trim());
		assertEquals("Caused by: Stacktrace$BadExceptionCompanion: Stacktrace$BadException", getLine(doc, 2));
		assertEquals("at Stacktrace$BadException.<init>(Stacktrace.java:10)", getLine(doc, 3).trim());
		assertEquals("... 1 more", getLine(doc, 4).trim());
		assertEquals("[CIRCULAR REFERENCE:Stacktrace$BadException]", getLine(doc, 5).trim());
		checkIndentationConsistency(doc, 0);
	}

	/** Test formatting stack trace from an ant execution. (output mixed with ant prefixes) */
	public void testFormatAnt() throws Exception {
		IDocument doc = consoleDocumentFormatted("[java] !ENTRY org.eclipse.debug.core 4 120 2005-01-11 03:02:30.321\n"
				+ "     [java] !MESSAGE An exception occurred while dispatching debug events.\n" + "     [java] !STACK 0\n"
				+ "     [java] java.lang.NullPointerException\n" + "     [java] 	at \n"
				+ "org.eclipse.debug.internal.ui.views.console.ProcessConsole.closeStreams\n" + "(ProcessConsole.java:364)\n" + "     [java] 	at \n"
				+ "org.eclipse.debug.internal.ui.views.console.ProcessConsole.handleDebugEvents\n" + "(ProcessConsole.java:438)\n"
				+ "     [java] 	at org.eclipse.debug.core.DebugPlugin$EventNotifier.run\n" + "(DebugPlugin.java:1043)");
		assertEquals("[java] !ENTRY org.eclipse.debug.core 4 120 2005-01-11 03:02:30.321", getLine(doc, 0));
		assertEquals("[java] !MESSAGE An exception occurred while dispatching debug events.", getLine(doc, 1));
		assertEquals("[java] !STACK 0", getLine(doc, 2));
		assertEquals("[java] java.lang.NullPointerException", getLine(doc, 3));
		assertEquals("at org.eclipse.debug.internal.ui.views.console.ProcessConsole.closeStreams(ProcessConsole.java:364)", getLine(doc, 4).replace("[java]", "").trim());
		assertEquals("at org.eclipse.debug.internal.ui.views.console.ProcessConsole.handleDebugEvents(ProcessConsole.java:438)", getLine(doc, 5).replace("[java]", "").trim());
		assertEquals("at org.eclipse.debug.core.DebugPlugin$EventNotifier.run(DebugPlugin.java:1043)", getLine(doc, 6).replace("[java]", "").trim());
		checkIndentationConsistency(doc, 3);
	}

	public void testHyperlinkMatchWithModule() throws Exception {
		consoleDocumentWithText("at java.nio.charset.Charset.checkName(java.base/Charset.java:296)");

		String[] matchTexts = linkTextsAtPositions(38);
		assertArrayEquals(allLinks(), new String[] { "java.base/Charset.java:296" }, matchTexts);
	}
}
