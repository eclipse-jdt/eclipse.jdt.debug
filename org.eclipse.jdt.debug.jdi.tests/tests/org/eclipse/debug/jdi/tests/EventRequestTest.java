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

import java.util.LinkedList;
import java.util.List;

import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;

/**
 * Tests for JDI com.sun.jdi.request.EventRequest.
 */
public class EventRequestTest extends AbstractJDITest {

	private final List<EventRequest> fRequests = new LinkedList<>();
	/**
	 * Creates a new test.
	 */
	public EventRequestTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get all kinds of request
		if (fVM.canWatchFieldAccess()) {
			fRequests.add(getAccessWatchpointRequest());
		}
		fRequests.add(getBreakpointRequest());
		fRequests.add(fVM.eventRequestManager().createClassPrepareRequest());
		fRequests.add(fVM.eventRequestManager().createClassUnloadRequest());
		fRequests.add(getExceptionRequest());
		if (fVM.canWatchFieldModification()) {
			fRequests.add(getModificationWatchpointRequest());
		}
		fRequests.add(
			fVM.eventRequestManager().createStepRequest(
				getThread(),
				StepRequest.STEP_LINE,
				StepRequest.STEP_OVER));
		fRequests.add(fVM.eventRequestManager().createThreadDeathRequest());
		fRequests.add(fVM.eventRequestManager().createThreadStartRequest());
	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		// Delete the requests we created in this test
		fVM.eventRequestManager().deleteEventRequests(fRequests);
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new EventRequestTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.request.EventRequest";
	}
	/**
	 * Test JDI disable(), enable(), isEnable() and setEnable(boolean).
	 */
	public void testJDIEnable() {
		for (int i = 0; i < fRequests.size(); i++) {
			EventRequest request = fRequests.get(i);
			assertFalse("1." + i, request.isEnabled());
			request.setEnabled(true);
			assertTrue("2." + i, request.isEnabled());
			request.setEnabled(false);
			assertFalse("3." + i, request.isEnabled());
			request.enable();
			assertTrue("4." + i, request.isEnabled());
			request.disable();
			assertFalse("5." + i, request.isEnabled());
		}
	}
	/**
	 * Test JDI setSuspendPolicy(int) and suspendPolicy().
	 */
	public void testJDISuspendPolicy() {
		int policy = EventRequest.SUSPEND_EVENT_THREAD;
		for (int i = 0; i < fRequests.size(); i++) {
			EventRequest request = fRequests.get(i);
			request.setSuspendPolicy(policy);
		}
		for (int i = 0; i < fRequests.size(); i++) {
			EventRequest request = fRequests.get(i);
			assertEquals(String.valueOf(i), request.suspendPolicy(), policy);
		}
	}
	/**
	 * Test JDI putProperty and getProperty.
	 */
	public void testJDIProperties() {
		EventRequest request = fRequests.get(0);
		request.putProperty(Integer.valueOf(0), "prop1");
		String prop = (String) request.getProperty(Integer.valueOf(0));
		assertEquals("1", "prop1", prop);

		request.putProperty(Integer.valueOf(0), null);
		prop = (String) request.getProperty(Integer.valueOf(0));
		assertNull("2", prop);

		request.putProperty(Integer.valueOf(0), "prop2");
		request.putProperty(Integer.valueOf(0), "prop3");
		prop = (String) request.getProperty(Integer.valueOf(0));
		assertEquals("3", "prop3", prop);

		request.putProperty(Integer.valueOf(0), null);
		prop = (String) request.getProperty(Integer.valueOf(0));
		assertNull("4", prop);

		request.putProperty(Integer.valueOf(1), null);
		prop = (String) request.getProperty(Integer.valueOf(1));
		assertNull("5", prop);

		request.putProperty(Integer.valueOf(1), "prop1");
		prop = (String) request.getProperty(Integer.valueOf(1));
		assertEquals("6", "prop1", prop);

	}
}
