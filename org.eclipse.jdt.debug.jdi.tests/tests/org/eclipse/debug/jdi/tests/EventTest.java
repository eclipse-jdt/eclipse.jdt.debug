/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.sun.jdi.event.Event;
import com.sun.jdi.request.EventRequest;

/**
 * Tests for JDI com.sun.jdi.event.Event.
 */
public class EventTest extends AbstractJDITest {
	private HashMap<EventRequest, Event> fAllEvents = new HashMap<>();
	/**
	 * Creates a new test.
	 */
	public EventTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// All events...

		EventRequest request;

		// AccessWatchpointEvent
		if (fVM.canWatchFieldAccess()) {
			request = getAccessWatchpointRequest();
			fAllEvents.put(
				request,
				triggerAndWait(request, "AccessWatchpointEvent", true));
		}

		// BreakpointEvent
		request = getBreakpointRequest();
		fAllEvents.put(
			request,
			triggerAndWait(request, "BreakpointEvent", true));

		// TODO ClassPrepareEvent
		// TODO ClassUnloadEvent
		// TODO ExceptionEvent

		// ModificationWatchpointEvent
		if (fVM.canWatchFieldModification()) {
			request = getModificationWatchpointRequest();
			fAllEvents.put(
				request,
				triggerAndWait(request, "ModificationWatchpointEvent", true));
		}

		// TODO StepEvent
		// TODO ThreadEndEvent
		// TODO ThreadStartEvent
		// TODO VMDeathEvent

	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		// Ensure that the modification of the "fBool" field has completed
		fVM.resume();
		waitUntilReady();

		// Remove the requests
		fVM.eventRequestManager().deleteEventRequests(
			new LinkedList<>(fAllEvents.keySet()));

		// Set the value of the "fBool" field back to its original value
		resetField();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new EventTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.event.Event";
	}
	/**
	 * Test JDI request().
	 */
	public void testJDIRequest() {
		Iterator<EventRequest> iterator = fAllEvents.keySet().iterator();
		while (iterator.hasNext()) {
			EventRequest request = iterator.next();
			Event event = fAllEvents.get(request);

			assertEquals(event.toString(), request, event.request());
		}
	}
}
