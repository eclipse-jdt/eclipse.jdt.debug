package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

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
	public void localTearDown() {
		// The test has resumed the test thread, so suspend it
		waitUntilReady();
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new ThreadDeathEventTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
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
