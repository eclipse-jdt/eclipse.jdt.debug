package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.Iterator;
import java.util.List;

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ReferenceType;

/**
 * Tests for JDI com.sun.jdi.ClassLoaderReference.
 */
public class ClassLoaderReferenceTest extends AbstractJDITest {

	private ClassLoaderReference fClassLoader;
	/**
	 * Creates a new test.
	 */
	public ClassLoaderReferenceTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	public void localSetUp() {
		// Get the class loader of org.eclipse.debug.jdi.tests.program.MainClass
		fClassLoader = getClassLoaderReference();
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new ClassLoaderReferenceTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
	public String getName() {
		return "com.sun.jdi.ClassLoaderReference";
	}
	/**
	 * Test JDI definedClasses().
	 */
	public void testJDIDefinedClasses() {
		Iterator defined = fClassLoader.definedClasses().iterator();
		int i = 0;
		while (defined.hasNext())
			assertTrue(
				Integer.toString(i++),
				defined.next() instanceof ReferenceType);
	}
	/**
	 * Test JDI visibleClasses().
	 */
	public void testJDIVisibleClasses() {
		List visible = fClassLoader.visibleClasses();
		Iterator defined = fClassLoader.definedClasses().iterator();
		while (defined.hasNext()) {
			ReferenceType next = (ReferenceType) defined.next();
			assertTrue(next.name(), visible.contains(next));
		}
	}
}