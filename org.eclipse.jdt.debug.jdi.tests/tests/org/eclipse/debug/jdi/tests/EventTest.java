/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	private HashMap fAllEvents = new HashMap();
	/**
	 * Creates a new test.
	 */
	public EventTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
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

		// ClassPrepareEvent
		// TO DO

		// ClassUnloadEvent
		// TO DO

		// ExceptionEvent
		// TO DO

		// ModificationWatchpointEvent
		if (fVM.canWatchFieldModification()) {
			request = getModificationWatchpointRequest();
			fAllEvents.put(
				request,
				triggerAndWait(request, "ModificationWatchpointEvent", true));
		}

		// StepEvent
		// TO DO

		// ThreadEndEvent
		// TO DO

		// ThreadStartEvent
		// TO DO

		// VMDeathEvent
		// TO DO?

	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	public void localTearDown() {
		// Ensure that the modification of the "fBool" field has completed
		fVM.resume();
		waitUntilReady();

		// Remove the requests
		fVM.eventRequestManager().deleteEventRequests(
			new LinkedList(fAllEvents.keySet()));

		// Set the value of the "fBool" field back to its original value
		resetField();
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new EventTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
	public String getName() {
		return "com.sun.jdi.event.Event";
	}
	/**
	 * Test JDI request().
	 */
	public void testJDIRequest() {
		Iterator iterator = fAllEvents.keySet().iterator();
		while (iterator.hasNext()) {
			EventRequest request = (EventRequest) iterator.next();
			Event event = (Event) fAllEvents.get(request);

			assertEquals(event.toString(), request, event.request());
		}
	}
}
