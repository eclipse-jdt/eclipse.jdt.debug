package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import com.sun.jdi.ClassType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;

/**
 * Tests for JDI com.sun.jdi.event.StepEvent.
 */
public class StepEventTest extends AbstractJDITest {

	private StepEvent fStepEvent;
	/**
	 * Creates a new test.
	 */
	public StepEventTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	public void localSetUp() {
		// Trigger a step event
		fStepEvent = triggerStepAndWait();
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
		new StepEventTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
	public String getName() {
		return "com.sun.jdi.event.StepEvent";
	}
	/**
	 * Test JDI thread().
	 */
	public void testJDIThread() {
		assertEquals("1", "Test Thread", fStepEvent.thread().name());
	}
	/**
	 * Test all possible steps.
	 */
	public void testJDIVariousSteps() {
		ThreadReference thread = getThread();
		triggerStepAndWait(thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO);
		waitUntilReady();
		triggerStepAndWait(thread, StepRequest.STEP_MIN, StepRequest.STEP_OVER);
		waitUntilReady();
		triggerStepAndWait(thread, StepRequest.STEP_LINE, StepRequest.STEP_INTO);
		waitUntilReady();
		triggerStepAndWait(thread, StepRequest.STEP_LINE, StepRequest.STEP_OVER);
		waitUntilReady();
		triggerStepAndWait(thread, StepRequest.STEP_LINE, StepRequest.STEP_OUT);
		waitUntilReady();
	}

	public void testJDIClassFilter1() {
		// Request for step events
		StepRequest request = getRequest();
		request.addClassFilter("java.lang.NegativeArraySizeException");
		request.enable();
		StepEvent event = null;
		try {
			event = triggerStepAndWait(getThread(), request, 1000);
		} catch (Error e) {
		}
		if (event != null) {
			assertTrue("1", false);
		}
		waitUntilReady();
		fVM.eventRequestManager().deleteEventRequest(request);

		request = getRequest();
		request.addClassFilter("java.lang.*");
		request.enable();
		event = null;
		try {
			event = triggerStepAndWait(getThread(), request, 1000);
		} catch (Error e) {
		}
		if (event != null) {
			assertTrue("1", false);
		}
		waitUntilReady();
		fVM.eventRequestManager().deleteEventRequest(request);
	}

	public void testJDIClassFilter2() {
		// Request for step events
		StepRequest request = getRequest();
		ClassType clazz = getClass("java.lang.NegativeArraySizeException");
		request.addClassFilter(clazz);
		request.enable();
		StepEvent event = null;
		try {
			event = triggerStepAndWait(getThread(), request, 1000);
		} catch (Error e) {
		}
		if (event != null) {
			assertTrue("1", false);
		}
		waitUntilReady();
		fVM.eventRequestManager().deleteEventRequest(request);
	}

	public void testJDIClassExclusionFilter1() {
		// Request for step events
		StepRequest request = getRequest();
		request.addClassExclusionFilter(
			"org.eclipse.debug.jdi.tests.program.MainClass");
		request.enable();
		StepEvent event = null;
		try {
			event = triggerStepAndWait(getThread(), request, 1000);
		} catch (Error e) {
		}
		if (event != null) {
			assertTrue("1", false);
		}
		waitUntilReady();
		fVM.eventRequestManager().deleteEventRequest(request);
	}

	public void testJDIClassExclusionFilter2() {
		StepRequest request = getRequest();
		request.addClassExclusionFilter("org.eclipse.*");
		request.addClassExclusionFilter("java.lang.*");
		request.enable();
		StepEvent event = null;
		try {
			event = triggerStepAndWait(getThread(), request, 1000);
		} catch (Error e) {
		}
		if (event != null) {
			System.out.println(event.location().declaringType());
			assertTrue("1", false);
		}
		waitUntilReady();
		fVM.eventRequestManager().deleteEventRequest(request);
	}

	public StepRequest getRequest() {
		StepRequest eventRequest =
			fVM.eventRequestManager().createStepRequest(
				getThread(),
				StepRequest.STEP_LINE,
				StepRequest.STEP_OVER);
		eventRequest.addCountFilter(1);
		eventRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		return eventRequest;
	}

}