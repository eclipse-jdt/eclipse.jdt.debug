/*******************************************************************************
 *  Copyright (c) 2010, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import junit.framework.TestCase;

import org.eclipse.jdt.internal.debug.ui.actions.OpenFromClipboardAction;

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

	private int getMatachingPattern(String s) {
		return OpenFromClipboardAction.getMatchingPattern(s);
	}

	// type tests
	public void testClassFileLine_1() {
		assertEquals(JAVA_FILE_LINE, getMatachingPattern("Foo.java:100"));
	}

	public void testClassFileLine_2() {
		assertEquals(JAVA_FILE_LINE, getMatachingPattern("Foo.java : 100"));
	}

	public void testClassFile_1() {
		assertEquals(JAVA_FILE, getMatachingPattern("Foo.java"));
	}

	public void testTypeLine_1() {
		assertEquals(TYPE_LINE, getMatachingPattern("Foo:100"));
	}

	public void testTypeLine_2() {
		assertEquals(TYPE_LINE, getMatachingPattern("Foo : 100"));
	}

	// stack trace element tests
	public void testStackTraceLine_1() {
		assertEquals(STACK_TRACE_LINE, getMatachingPattern("(OpenFromClipboardAction.java:121)"));
	}

	public void testStackTraceLine_2() {
		assertEquals(STACK_TRACE_LINE, getMatachingPattern("( OpenFromClipboardAction.java : 121 )"));
	}

	public void testStackTraceLine_3() {
		assertEquals(STACK_TRACE_LINE, getMatachingPattern("at org.eclipse.jdt.internal.debug.ui.actions.OpenFromClipboardAction.getMatchingPattern(OpenFromClipboardAction.java:121)"));
	}

	public void testStackTraceLine_4() {
		assertEquals(STACK_TRACE_LINE, getMatachingPattern("OpenFromClipboardAction.getMatchingPattern(OpenFromClipboardAction.java:121)"));
	}

	public void testStackTraceLine_5() {
		assertEquals(STACK_TRACE_LINE, getMatachingPattern("OpenFromClipboardAction.getMatchingPattern ( OpenFromClipboardAction.java : 121 )"));
	}

	// method tests
	public void testMethod_1() {
		assertEquals(METHOD, getMatachingPattern("getBytes()"));
	}

	public void testMethod_2() {
		assertEquals(METHOD, getMatachingPattern("getBytes(String, int[], int)"));
	}

	public void testMethod_3() {
		assertEquals(METHOD, getMatachingPattern("String.getBytes()"));
	}

	public void testMethod_4() {
		assertEquals(METHOD, getMatachingPattern("String.getBytes(String, int[], int)"));
	}

	public void testMethod_5() {
		assertEquals(METHOD, getMatachingPattern("java.lang.String.getBytes()"));
	}

	public void testMethod_6() {
		assertEquals(METHOD, getMatachingPattern("java.lang.String.getBytes(String)"));
	}

	public void testMethod_7() {
		assertEquals(METHOD, getMatachingPattern("java.lang.String.getBytes(String, int[], int)"));
	}

	public void testMethod_8() {
		assertEquals(METHOD, getMatachingPattern("java.util.List.containsAll(Collection<?>)"));
	}

	public void testMethod_10() {
		assertEquals(METHOD, getMatachingPattern("A$B.run()"));
	}

	public void testMethod_11() {
		assertEquals(METHOD, getMatachingPattern("$.$$()"));
	}

	// member tests
	public void testMember_1() {
		assertEquals(MEMBER, getMatachingPattern("String#getBytes"));
	}

	public void testMember_2() {
		assertEquals(MEMBER, getMatachingPattern("java.lang.String#getBytes"));
	}

	public void testMember_3() {
		assertEquals(METHOD_JAVADOC_REFERENCE, getMatachingPattern("java.lang.String#getBytes(String)"));
	}

	// qualified name tests
	public void testQualifiedName_1() {
		assertEquals(QUALIFIED_NAME, getMatachingPattern("getBytes"));
	}

	public void testQualifiedName_2() {
		assertEquals(QUALIFIED_NAME, getMatachingPattern("String.getBytes"));
	}

	public void testQualifiedName_3() {
		assertEquals(QUALIFIED_NAME, getMatachingPattern("java.lang.String.getBytes"));
	}

	public void testQualifiedName_4() {
		assertEquals(QUALIFIED_NAME, getMatachingPattern("$"));
	}

	public void testQualifiedName_5() {
		assertEquals(QUALIFIED_NAME, getMatachingPattern("$$"));
	}

	public void testQualifiedName_6() {
		assertEquals(QUALIFIED_NAME, getMatachingPattern("A$"));
	}

	// stack element tests
	public void testStackElement_1() {
		assertEquals(STACK, getMatachingPattern("java.lang.String.valueOf(char) line: 1456"));
	}

	public void testStackElement_2() {
		assertEquals(STACK, getMatachingPattern("java.lang.String.valueOf(char): 1456"));
	}

	// invalid pattern tests
	public void testInvalidPattern_1() {
		assertEquals(INVALID, getMatachingPattern("(Collection)"));
	}

	public void testInvalidPattern_2() {
		assertEquals(INVALID, getMatachingPattern("()"));
	}
}
