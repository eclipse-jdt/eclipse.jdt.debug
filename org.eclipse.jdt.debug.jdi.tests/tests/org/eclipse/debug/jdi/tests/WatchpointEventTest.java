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

import java.util.ArrayList;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.WatchpointEvent;
import com.sun.jdi.request.EventRequestManager;

/**
 * Tests for JDI com.sun.jdi.event.WatchpointEvent.
 */
public class WatchpointEventTest extends AbstractJDITest {

	private WatchpointEvent fAccessWatchpointEvent,
		fStaticAccessWatchpointEvent,
		fModificationWatchpointEvent;
	// NB: Static modification watchpoint event is tested in ModificationWatchpointTest
	/**
	 * Creates a new test.
	 */
	public WatchpointEventTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Trigger an access watchpoint event
		fAccessWatchpointEvent =
			(AccessWatchpointEvent) triggerAndWait(getAccessWatchpointRequest(),
				"AccessWatchpointEvent",
				true);
		assertNotNull("Got access watchpoint event", fAccessWatchpointEvent);

		// Trigger a static access watchpoint event
		fStaticAccessWatchpointEvent =
			(AccessWatchpointEvent) triggerAndWait(
				getStaticAccessWatchpointRequest(),
				"StaticAccessWatchpointEvent",
				true);
		assertNotNull("Got static access watchpoint event", fStaticAccessWatchpointEvent);

		// Trigger a modification watchpoint event
		fModificationWatchpointEvent =
			(ModificationWatchpointEvent) triggerAndWait(
				getModificationWatchpointRequest(),
				"ModificationWatchpointEvent",
				false);
		// Interrupt the VM so that we can test valueCurrent()
		assertNotNull("Got modification watchpoint event", fModificationWatchpointEvent);

	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		// Ensure that the modification of the "fBool" field has completed
		fVM.resume();
		waitUntilReady();

		// Delete the event requests we created in this test
		EventRequestManager requestManager = fVM.eventRequestManager();
		requestManager.deleteEventRequests(
			new ArrayList<>(requestManager.accessWatchpointRequests()));
		requestManager.deleteEventRequests(
			new ArrayList<>(requestManager.modificationWatchpointRequests()));

		// Set the value of the "fBool" field back to its original value
		resetField();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new WatchpointEventTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.event.WatchpointEvent";
	}
	/**
	 * Test JDI field().
	 */
	public void testJDIField() {
		assertEquals("1", getField("fBool"), fAccessWatchpointEvent.field());
		assertEquals(
			"2",
			getField("fString"),
			fStaticAccessWatchpointEvent.field());
		assertEquals(
			"3",
			getField("fBool"),
			fModificationWatchpointEvent.field());
	}
	/**
	 * Test JDI object().
	 */
	public void testJDIObject() {
		assertEquals(
			"1",
			getObjectReference(),
			fAccessWatchpointEvent.object());
		assertNull("2", fStaticAccessWatchpointEvent.object());
		assertEquals(
			"3",
			getObjectReference(),
			fModificationWatchpointEvent.object());
	}
	/**
	 * Test JDI valueCurrent().
	 */
	public void testJDIValueCurrent() {
		assertFalse("1", ((BooleanValue) fAccessWatchpointEvent.valueCurrent()).value());

		assertEquals(
			"2",
			"Hello World",
			((StringReference) fStaticAccessWatchpointEvent.valueCurrent())
				.value());

		assertFalse("3", ((BooleanValue) fModificationWatchpointEvent.valueCurrent())
			.value());
	}
}
