package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.ClassPrepareRequest;

/**
 * Tests for JDI com.sun.jdi.event.ClassPrepareEvent.
 */
public class ClassPrepareEventTest extends AbstractJDITest {
	private ClassPrepareRequest fRequest;
	private ClassPrepareEvent fEvent;
	/**
	 * Creates a new test.
	 */
	public ClassPrepareEventTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	public void localSetUp() {
		// Trigger a class prepare event
		fRequest = fVM.eventRequestManager().createClassPrepareRequest();
		fEvent =
			(ClassPrepareEvent) triggerAndWait(fRequest,
				"ClassPrepareEvent",
				true);
	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	public void localTearDown() {
		// The test has resumed the test thread, so suspend it
		waitUntilReady();

		// Delete the class prepare request
		fVM.eventRequestManager().deleteEventRequest(fRequest);
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new ClassPrepareEventTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
	public String getName() {
		return "com.sun.jdi.event.ClassPrepareEvent";
	}
	/**
	 * Test JDI referenceType().
	 */
	public void testJDIReferenceType() {
		assertEquals(
			"1",
			"org.eclipse.debug.jdi.tests.program.TestClass",
			fEvent.referenceType().name());
	}
	/**
	 * Test JDI thread().
	 */
	public void testJDIThread() {
		assertEquals("1", "Test Thread", fEvent.thread().name());
	}
}
