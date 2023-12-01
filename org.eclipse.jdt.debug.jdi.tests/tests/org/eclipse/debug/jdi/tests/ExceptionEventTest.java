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

import com.sun.jdi.ReferenceType;
import com.sun.jdi.event.ExceptionEvent;

/**
 * Tests for JDI com.sun.jdi.event.ExceptionEvent.
 */
public class ExceptionEventTest extends AbstractJDITest {

	private ExceptionEvent fEvent;
	/**
	 * Creates a new test.
	 */
	public ExceptionEventTest() {
		super();
	}

	public ExceptionEventTest(String name) {
		super(name);
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Trigger an exception event
		fEvent =
			(ExceptionEvent) triggerAndWait(getExceptionRequest(),
				"ExceptionEvent",
				false);
	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		// The test has interrupted the VM, so let it go
		fVM.resume();

		// The test has resumed the test thread, so suspend it
		waitUntilReady();
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new ExceptionEventTest().runSuite(args);
	}
	/**
	 * Test JDI catchLocation().
	 */
	public void testJDICatchLocation() {
		// Uncaught exception
		assertNull("1", fEvent.catchLocation());

		// TO DO: Caught exception
	}
	/**
	 * Test JDI exception().
	 */
	public void testJDIException() {
		ReferenceType expected =
			fVM.classesByName("java.lang.Error").get(0);
		assertEquals("1", expected, fEvent.exception().referenceType());
	}
	/**
	 * Test JDI thread().
	 */
	public void testJDIThread() {
		assertEquals("1", "Test Exception Event", fEvent.thread().name());
	}
}
