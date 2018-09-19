/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.event.ThreadDeathEvent;

/**
 * Tests for JDI com.sun.jdi.event.ThreadDeathEvent.
 */
public class ThreadDeathEventTest extends AbstractJDITest {

	private ThreadDeathEvent fEvent;
	/**
	 * Creates a new test.
	 */
	public ThreadDeathEventTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Make sure the entire VM is not suspended before we start a new thread
		// (otherwise this new thread will start suspended and we will never get the
		// ThreadDeath event)
		fVM.resume();

		// Trigger a thread end event
		fEvent =
			(ThreadDeathEvent) triggerAndWait(fVM
				.eventRequestManager()
				.createThreadDeathRequest(),
				"ThreadDeathEvent",
				true);
	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		// The test has resumed the test thread, so suspend it
		waitUntilReady();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new ThreadDeathEventTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.event.ThreadDeathEvent";
	}
	/**
	 * Test JDI thread().
	 */
	public void testJDIThread() {
		try {
			assertEquals(
				"1",
				"Test Thread Death Event",
				fEvent.thread().name());
		} catch (ObjectCollectedException e) {
			// Workaround known problem in Sun's VM
		}
	}
}
