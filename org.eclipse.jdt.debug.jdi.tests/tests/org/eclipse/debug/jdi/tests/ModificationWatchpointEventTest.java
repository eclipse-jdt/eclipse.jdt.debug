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

import com.sun.jdi.StringReference;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.request.WatchpointRequest;

/**
 * Tests for JDI com.sun.jdi.ModificationWatchpointEvent.
 */
public class ModificationWatchpointEventTest extends AbstractJDITest {

	private ModificationWatchpointEvent fWatchpointEvent;
	private WatchpointRequest fWatchpointRequest;
	/**
	 * Creates a new test.
	 */
	public ModificationWatchpointEventTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Trigger a static modification watchpoint event
		fWatchpointRequest = getStaticModificationWatchpointRequest();
		fWatchpointEvent =
			(ModificationWatchpointEvent) triggerAndWait(fWatchpointRequest,
				"StaticModificationWatchpointEvent",
				false);
		// Interrupt the VM so that we can test valueToBe()
	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		// Ensure that the modification of the "fString" field has completed
		fVM.resume();
		waitUntilReady();

		// Remove the modification watchpoint request
		fVM.eventRequestManager().deleteEventRequest(fWatchpointRequest);

		// Set the value of the "fString" field back to its original value
		resetStaticField();
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new ModificationWatchpointEventTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.event.ModificationWatchpointEvent";
	}
	/**
	 * Test JDI valueToBe().
	 */
	public void testJDIValueToBe() {
		assertEquals(
			"1",
			"Hello Universe",
			((StringReference) fWatchpointEvent.valueToBe()).value());
	}
}
