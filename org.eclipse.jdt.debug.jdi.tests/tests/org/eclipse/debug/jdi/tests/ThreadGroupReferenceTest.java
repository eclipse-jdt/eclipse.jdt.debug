/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.jdi.tests;

import java.util.List;
import java.util.ListIterator;

import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;

/**
 * Tests for JDI com.sun.jdi.ThreadGroupReference
 * and JDWP ThreadGroup command set.
 */
public class ThreadGroupReferenceTest extends AbstractJDITest {

	private ThreadGroupReference fThreadGroup;
	/**
	 * Creates a new test.
	 */
	public ThreadGroupReferenceTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get value of "fThread"
		ThreadReference thread = getThread();

		// Get its thread group
		fThreadGroup = thread.threadGroup();
	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		// The test has resumed the thread group, and so the test thread, so suspend it
		waitUntilReady();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new ThreadGroupReferenceTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.ThreadGroupReference";
	}
	/**
	 * Test JDI name() and JDWP 'ThreadGroup - Get name'.
	 */
	public void testJDIName() {
		assertEquals("1", "Test ThreadGroup", fThreadGroup.name());
	}
	/**
	 * Test JDI parent() and JDWP 'ThreadGroup - Get parent'.
	 */
	public void testJDIParent() {
		ThreadGroupReference systemThreadGroup = fThreadGroup.parent();
		assertNotNull("1", systemThreadGroup);
		assertEquals("2", "main", systemThreadGroup.name());
		// assertTrue("3", systemThreadGroup.parent() == null);
	}
	/**
	 * Test JDI suspend() and resume().
	 */
	public void testJDISuspendResume() {
		fThreadGroup.suspend();
		fThreadGroup.resume();
	}
	/**
	 * Test JDI threadGroups().
	 */
	public void testJDIThreadGroups() {
		List<?> threadGroups = fThreadGroup.threadGroups();
		assertEquals("1", 0, threadGroups.size());
	}
	/**
	 * Test JDI threads() and JDWP 'ThreadGroup - Get children'.
	 */
	public void testJDIThreads() {
		List<?> threads = fThreadGroup.threads();
		ListIterator<?> iterator = threads.listIterator();
		boolean isIncluded = false;
		while (iterator.hasNext()) {
			ThreadReference thread = (ThreadReference) iterator.next();
			if (thread.name().equals("Test Thread")) {
				isIncluded = true;
				break;
			}
		}
		assertTrue("1", isIncluded);
	}
}
