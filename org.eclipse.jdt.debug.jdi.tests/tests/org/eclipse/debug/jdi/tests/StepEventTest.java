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
	@Override
	public void localSetUp() {
		// Trigger a step event
		fStepEvent = triggerStepAndWait();
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
		new StepEventTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
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

	/**
	 * Tests a step request with a specified class filter
	 */
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

	/**
	 * Retests a step request with a specified class filter
	 */
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

	/**
	 * Tests a step request with a specific exclusion filter
	 */
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

	/**
	 * Retests a step request with a specific exclusion filter
	 */
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

	/**
	 * Creates a returns a new <code>StepRequest</code>
	 * @return a new <code>StepRequest</code>
	 */
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
