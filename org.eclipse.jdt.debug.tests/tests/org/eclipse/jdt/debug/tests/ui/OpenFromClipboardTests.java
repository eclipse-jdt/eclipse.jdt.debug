/*******************************************************************************
 *  Copyright (c) 2010, 2018 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.TestAgainException;
import org.eclipse.jdt.internal.debug.ui.actions.OpenFromClipboardAction;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.widgets.Display;
import org.junit.Assert;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the Open from Clipboard action.
 */
public class OpenFromClipboardTests extends TestCase {

	/*
	 * Copy of constants from OpenFromClipboardAction
	 */
	private static final int INVALID = 0;

	private static final int QUALIFIED_NAME = 1;

	private static final int JAVA_FILE = 2;

	private static final int JAVA_FILE_LINE = 3;

	private static final int TYPE_LINE = 4;

	private static final int STACK_TRACE_LINE = 5;

	private static final int METHOD = 6;

	private static final int STACK = 7;

	private static final int MEMBER = 8;

	private static final int METHOD_JAVADOC_REFERENCE = 9;

	private IPackageFragmentRoot fSourceFolder;

	private Accessor fAccessor = new Accessor(OpenFromClipboardAction.class);

	private static class MyTestSetup extends TestSetup {

		public static IJavaProject fJProject;

		public MyTestSetup(Test test) {
			super(test);
		}

		@Override
		protected void setUp() throws Exception {
			super.setUp();
			fJProject = createProject("OpenFromClipboardTests");
		}

		@Override
		protected void tearDown() throws Exception {
			fJProject.getProject().delete(true, true, null);
			super.tearDown();
		}

		private static IJavaProject createProject(String name) throws CoreException {
			// delete any pre-existing project
			IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			if (pro.exists()) {
				pro.delete(true, true, null);
			}

			// create project
			IJavaProject javaProject = JavaProjectHelper.createJavaProject(name, "bin");

			// add rt.jar
			IVMInstall vm = JavaRuntime.getDefaultVMInstall();
			Assert.assertNotNull("No default JRE", vm);
			JavaProjectHelper.addContainerEntry(javaProject, new Path(JavaRuntime.JRE_CONTAINER));
			return javaProject;
		}
	}

	public static Test suite() {
		return new MyTestSetup(new TestSuite(OpenFromClipboardTests.class));
	}

	public static Test setUpTest(Test someTest) {
		return new MyTestSetup(someTest);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fSourceFolder = JavaProjectHelper.addSourceContainer(MyTestSetup.fJProject, "src");
	}

	@Override
	protected void tearDown() throws Exception {
		JavaProjectHelper.removeSourceContainer(MyTestSetup.fJProject, "src");
		super.tearDown();
	}

	private int getMatachingPattern(String s) {
		Object returnValue = fAccessor.invoke("getMatchingPattern", new Object[] { s });
		return ((Integer) returnValue).intValue();
	}

	private List<?> getJavaElementMatches(final String textData) {
		final List<?> matches = new ArrayList<>();
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				fAccessor.invoke("getJavaElementMatches", new Class[] { String.class, List.class }, new Object[] { textData, matches });
			}
		});
		return matches;
	}

	private void setupTypeTest(String typeName) throws CoreException {
		IPackageFragment pack= fSourceFolder.createPackageFragment("p", false, null);
		((IContainer)pack.getUnderlyingResource()).setDefaultCharset("UTF-8", null);
		StringBuilder buf= new StringBuilder();
		buf.append("package p;\n");
		buf.append("public class " + typeName + " {\n");
		buf.append("	void getMatching$Pattern(){\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit x= pack.createCompilationUnit(typeName + ".java", buf.toString(), false, null);
		x.exists();

	}

	public void testClassFileLine_1() throws Exception {
		String s = "OpenFromClipboardTests.java:100";
		assertEquals(JAVA_FILE_LINE, getMatachingPattern(s));

		setupTypeTest("OpenFromClipboardTests");
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testClassFileLine_1 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testClassFileLine_2() throws Exception {
		String s = "OpenFromClipboardTests.java : 100";
		assertEquals(JAVA_FILE_LINE, getMatachingPattern(s));

		setupTypeTest("OpenFromClipboardTests");
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testClassFileLine_2 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testClassFileLine_3() throws Exception {
		String s = "OpenFromClipboard$Tests.java:100";
		assertEquals(JAVA_FILE_LINE, getMatachingPattern(s));

		setupTypeTest("OpenFromClipboard$Tests");
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testClassFileLine_3 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testDBCS() throws Exception {
		String typeName= "\u65b0\u898f\u30af\u30e9\u30b9";
		String s= typeName + ".java:100";

		assertEquals(JAVA_FILE_LINE, getMatachingPattern(s));

		setupTypeTest(typeName);

		List<?> matches= getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testDBCS test again");
		}
		assertEquals(1, matches.size());
	}

	public void testUmlaut() throws Exception {
		String typeName= "\u0042\u006c\u00f6\u0064";
		String s= typeName + ".java:100";

		assertEquals(JAVA_FILE_LINE, getMatachingPattern(s));

		setupTypeTest(typeName);

		List<?> matches= getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testUmlaut test again");
		}
		assertEquals(1, matches.size());
	}

	public void testClassFile_1() throws Exception {
		String s = "OpenFromClipboardTests.java";
		assertEquals(JAVA_FILE, getMatachingPattern(s));

		setupTypeTest("OpenFromClipboardTests");
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testClassFile_1 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testTypeLine_1() throws Exception {
		String s = "OpenFromClipboardTests:100";
		assertEquals(TYPE_LINE, getMatachingPattern(s));

		setupTypeTest("OpenFromClipboardTests");
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testTypeLine_1 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testTypeLine_2() throws Exception {
		String s = "OpenFromClipboardTests : 100";
		assertEquals(TYPE_LINE, getMatachingPattern(s));

		setupTypeTest("OpenFromClipboardTests");
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testTypeLine_2 test again");
		}
		assertEquals(1, matches.size());
	}

	// stack trace element tests
	public void testStackTraceLine_1() throws Exception {
		String s = "(OpenFromClipboardTests.java:121)";
		assertEquals(STACK_TRACE_LINE, getMatachingPattern(s));

		setupTypeTest("OpenFromClipboardTests");
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testStackTraceLine_1 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testStackTraceLine_2() throws Exception {
		String s = "( OpenFromClipboardTests.java : 121 )";
		assertEquals(STACK_TRACE_LINE, getMatachingPattern(s));

		setupTypeTest("OpenFromClipboardTests");
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testStackTraceLine_2 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testStackTraceLine_3() throws Exception {
		String s = "at p.OpenFromClipboardTests.getMatchingPattern(OpenFromClipboardTests.java:121)";
		assertEquals(STACK_TRACE_LINE, getMatachingPattern(s));

		setupTypeTest("OpenFromClipboardTests");
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testStackTraceLine_3 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testStackTraceLine_4() throws Exception {
		String s = "OpenFromClipboardTests.getMatchingPattern(OpenFromClipboardTests.java:121)";
		assertEquals(STACK_TRACE_LINE, getMatachingPattern(s));

		setupTypeTest("OpenFromClipboardTests");
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testStackTraceLine_4 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testStackTraceLine_5() throws Exception {
		String s = "OpenFromClipboardTests.getMatchingPattern ( OpenFromClipboardTests.java : 121 )";
		assertEquals(STACK_TRACE_LINE, getMatachingPattern(s));

		setupTypeTest("OpenFromClipboardTests");
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testStackTraceLine_5 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testStackTraceLine_6() throws Exception {
		String s = "at p.OpenFromClipboard$Tests.getMatching$Pattern(OpenFromClipboardTests.java:121)";
		setupTypeTest("OpenFromClipboardTests");

		setupTypeTest("OpenFromClipboard$Tests");
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testStackTraceLine_6 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testStackTraceLine_7() throws Exception {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=349933#c2
		String s = "getMatchingPattern ( OpenFromClipboardTests.java : 121 )";
		assertEquals(STACK_TRACE_LINE, getMatachingPattern(s));

		setupTypeTest("OpenFromClipboardTests");
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testStackTraceLine_7 test again");
		}
		assertEquals(1, matches.size());
	}

	// method tests
	private void setupMethodTest() throws JavaModelException {
		IPackageFragment pack= fSourceFolder.createPackageFragment("p", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package p;\n");
		buf.append("public class OpenFromClipboardTests {\n");
		buf.append("	private void invokeOpenFromClipboardCommand() {\n");
		buf.append("	}\n");
		buf.append("	private void invokeOpenFromClipboardCommand(String s) {\n");
		buf.append("	}\n");
		buf.append("	private void invokeOpenFromClipboardCommand(String s, int[] a, int b) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		pack.createCompilationUnit("OpenFromClipboardTests.java", buf.toString(), false, null);
	}

	public void testMethod_1() throws Exception {
		String s = "invokeOpenFromClipboardCommand()";
		assertEquals(METHOD, getMatachingPattern(s));

		setupMethodTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testMethod_1 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testMethod_2() throws Exception {
		String s = "invokeOpenFromClipboardCommand(String, int[], int)";
		assertEquals(METHOD, getMatachingPattern(s));

		setupMethodTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testMethod_2 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testMethod_3() throws Exception {
		String s = "OpenFromClipboardTests.invokeOpenFromClipboardCommand()";
		assertEquals(METHOD, getMatachingPattern(s));

		setupMethodTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testMethod_3 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testMethod_4() throws Exception {
		String s = "OpenFromClipboardTests.invokeOpenFromClipboardCommand(String, int[], int)";
		assertEquals(METHOD, getMatachingPattern(s));

		setupMethodTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testMethod_4 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testMethod_5() throws Exception {
		String s = "p.OpenFromClipboardTests.invokeOpenFromClipboardCommand()";
		assertEquals(METHOD, getMatachingPattern(s));

		setupMethodTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testMethod_5 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testMethod_6() throws Exception {
		String s = "p.OpenFromClipboardTests.invokeOpenFromClipboardCommand(String)";
		assertEquals(METHOD, getMatachingPattern(s));

		setupMethodTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testMethod_6 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testMethod_7() throws Exception {
		String s = "p.OpenFromClipboardTests.invokeOpenFromClipboardCommand(String, int[], int)";
		assertEquals(METHOD, getMatachingPattern(s));

		setupMethodTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testMethod_7 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testMethod_8() throws Exception {
		String s = "java.util.List.containsAll(Collection<?>)";
		assertEquals(METHOD, getMatachingPattern(s));
	}

	private void setupMethodWithDollarSignTest() throws JavaModelException {
		IPackageFragment pack = fSourceFolder.createPackageFragment("p", false, null);
		StringBuilder buf = new StringBuilder();
		buf.append("package p;\n");
		buf.append("public class OpenFromClipboard$Tests {\n");
		buf.append("	private void invokeOpenFromClipboardCommand() {\n");
		buf.append("	}\n");
		buf.append("	private void invokeOpenFromClipboardCommand(String s) {\n");
		buf.append("	}\n");
		buf.append("	class Inner {\n");
		buf.append("		private void invokeOpenFromClipboardCommand() {\n");
		buf.append("		}\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("class $ {\n");
		buf.append("	void $$() {\n");
		buf.append("	}\n");
		buf.append("}\n");
		pack.createCompilationUnit("OpenFromClipboard$Tests.java", buf.toString(), false, null);
	}

	public void testMethod_9() throws Exception {
		String s = "OpenFromClipboard$Tests.invokeOpenFromClipboardCommand()";
		assertEquals(METHOD, getMatachingPattern(s));

		// TODO: This currently fails. see https://bugs.eclipse.org/bugs/show_bug.cgi?id=333948
		// setupMethodWithDollarSignTest();
		// performTest(s,1);
	}

	public void testMethod_10() throws Exception {
		String s = "OpenFromClipboard$Tests.invokeOpenFromClipboardCommand(String)";
		assertEquals(METHOD, getMatachingPattern(s));

		setupMethodWithDollarSignTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testMethod_10 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testMethod_11() throws Exception {
		String s = "$.$$()";
		assertEquals(METHOD, getMatachingPattern(s));

		setupMethodWithDollarSignTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testMethod_11 test again");
		}
		assertEquals(1, matches.size());
	}

	// member tests
	private void setupMemberTest() throws JavaModelException {
		IPackageFragment pack = fSourceFolder.createPackageFragment("p", false, null);
		StringBuilder buf = new StringBuilder();
		buf.append("package p;\n");
		buf.append("public class OpenFromClipboardTests {\n");
		buf.append("	private void invokeOpenFromClipboardCommand(String s) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		pack.createCompilationUnit("OpenFromClipboardTests.java", buf.toString(), false, null);
	}

	public void testMember_1() throws Exception {
		String s = "OpenFromClipboardTests#invokeOpenFromClipboardCommand";
		assertEquals(MEMBER, getMatachingPattern(s));

		setupMemberTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testMember_1 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testMember_2() throws Exception {
		String s = "p.OpenFromClipboardTests#invokeOpenFromClipboardCommand";
		assertEquals(MEMBER, getMatachingPattern(s));

		setupMemberTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testMember_2 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testMember_3() throws Exception {
		String s = "p.OpenFromClipboardTests#invokeOpenFromClipboardCommand(String)";
		assertEquals(METHOD_JAVADOC_REFERENCE, getMatachingPattern(s));

		setupMemberTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testMember_3 test again");
		}
		assertEquals(1, matches.size());
	}

	// qualified name tests
	public void testQualifiedName_1() throws Exception {
		String s = "invokeOpenFromClipboardCommand";
		assertEquals(QUALIFIED_NAME, getMatachingPattern(s));

		setupMemberTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testQualifiedName_1 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testQualifiedName_2() throws Exception {
		String s = "OpenFromClipboardTests.invokeOpenFromClipboardCommand";
		assertEquals(QUALIFIED_NAME, getMatachingPattern(s));

		setupMemberTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testQualifiedName_2 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testQualifiedName_3() throws Exception {
		String s = "p.OpenFromClipboardTests.invokeOpenFromClipboardCommand";
		assertEquals(QUALIFIED_NAME, getMatachingPattern(s));

		setupMemberTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testQualifiedName_3 test again");
		}
		assertEquals(1, matches.size());
	}

	private void setupQualifiedNameWithDollarSignTest() throws JavaModelException {
		IPackageFragment pack = fSourceFolder.createPackageFragment("p", false, null);
		StringBuilder buf = new StringBuilder();
		buf.append("package p;\n");
		buf.append("public class OpenFromClipboard$Tests {\n");
		buf.append("	private void invokeOpenFromClipboardCommand() {\n");
		buf.append("	}\n");
		buf.append("	class Inner {\n");
		buf.append("		private void invokeOpenFromClipboardCommand() {\n");
		buf.append("		}\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("class $ {\n");
		buf.append("}\n");
		buf.append("class $$ {\n");
		buf.append("}\n");
		pack.createCompilationUnit("OpenFromClipboard$Tests.java", buf.toString(), false, null);
	}

	public void testQualifiedName_4() throws Exception {
		String s = "$";
		assertEquals(QUALIFIED_NAME, getMatachingPattern(s));

		setupQualifiedNameWithDollarSignTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testQualifiedName_4 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testQualifiedName_5() throws Exception {
		String s = "$$";
		assertEquals(QUALIFIED_NAME, getMatachingPattern(s));

		setupQualifiedNameWithDollarSignTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testQualifiedName_5 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testQualifiedName_6() throws Exception {
		String s = "OpenFromClipboard$Tests";
		assertEquals(QUALIFIED_NAME, getMatachingPattern(s));

		setupQualifiedNameWithDollarSignTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testQualifiedName_6 test again");
		}
		assertEquals(1, matches.size());
	}

	// stack element tests
	private void setupStackElementTest() throws JavaModelException {
		IPackageFragment pack = fSourceFolder.createPackageFragment("p", false, null);
		StringBuilder buf = new StringBuilder();
		buf.append("package p;\n");
		buf.append("public class OpenFromClipboardTests {\n");
		buf.append("	private void invokeOpenFromClipboardCommand(char ch) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		pack.createCompilationUnit("OpenFromClipboardTests.java", buf.toString(), false, null);
	}

	public void testStackElement_1() throws Exception {
		String s = "p.OpenFromClipboardTests.invokeOpenFromClipboardCommand(char) line: 1456";
		assertEquals(STACK, getMatachingPattern(s));

		setupStackElementTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testStackElement_1 test again");
		}
		assertEquals(1, matches.size());
	}

	public void testStackElement_2() throws Exception {
		String s = "p.OpenFromClipboardTests.invokeOpenFromClipboardCommand(char): 1456";
		assertEquals(STACK, getMatachingPattern(s));

		setupStackElementTest();
		List<?> matches = getJavaElementMatches(s);
		if (matches.size() != 1) {
			throw new TestAgainException("testStackElement_2 test again");
		}
		assertEquals(1, matches.size());
	}

	// invalid pattern tests
	public void testInvalidPattern_1() {
		String s = "(Collection)";
		assertEquals(INVALID, getMatachingPattern(s));
	}

	public void testInvalidPattern_2() {
		String s = "()";
		assertEquals(INVALID, getMatachingPattern(s));
	}

	public void testInvalidPattern_3() throws Exception {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426392#c6
		String s = "java.lang.IllegalArgumentException\n" +
				"	at org.eclipse.core.runtime.Assert.isLegal(Assert.java:63)\n" +
				"	at something.completely.Different(Different.java:47)";
		assertEquals(INVALID, getMatachingPattern(s));
	}
}
