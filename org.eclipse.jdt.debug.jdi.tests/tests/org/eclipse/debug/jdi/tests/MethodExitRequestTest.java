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
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.request.MethodExitRequest;

/**
 * Tests method exit requests
 */
public class MethodExitRequestTest extends AbstractJDITest {

	/**
	 * @see org.eclipse.debug.jdi.tests.AbstractJDITest#localSetUp()
	 */
	@Override
	public void localSetUp() {
	}
	/**
	 * @see org.eclipse.debug.jdi.tests.AbstractJDITest#localTearDown()
	 */
	@Override
	public void localTearDown() {
		fVM.resume();
		waitUntilReady();
	}

	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new MethodExitRequestTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.MethodExitRequest";
	}

	/**
	 * Creates and returns a new <code>MethodExitRequest</code>
	 * @return a new <code>MethodExitRequest</code>
	 */
	protected MethodExitRequest getMethodExitRequest() {
		return fVM.eventRequestManager().createMethodExitRequest();
	}
	/**
	 * Tests a method exit request without filtering
	 */
	public void testJDIWithoutFilter() {
		MethodExitRequest request = getMethodExitRequest();

		Event e = triggerAndWait(request, "BreakpointEvent", true);
		assertEquals(request, e.request());

		MethodExitEvent event = (MethodExitEvent) e;
		assertEquals(getThread(), event.thread());
		fVM.eventRequestManager().deleteEventRequest(request);
	}

	/**
	 * Tests a method exit request with an exclusion filter
	 */
	public void testJDIWithClassExclusionFilter() {
		MethodExitRequest request = getMethodExitRequest();
		request.addClassExclusionFilter("org.eclipse.debug.jdi.tests.program.*");

		Event e = triggerAndWait(request, "BreakpointEvent", true);
		assertEquals(request, e.request());

		MethodExitEvent event = (MethodExitEvent) e;
		Method m = event.method();
		ReferenceType r = m.location().declaringType();
		assertTrue("1", !r.name().startsWith("org.eclipse.debug.jdi.tests.program."));
		fVM.eventRequestManager().deleteEventRequest(request);
	}

	/**
	 * Tests a method exit request with a specific class filter
	 */
	public void testJDIWithClassFilter1() {
		MethodExitRequest request = getMethodExitRequest();
		ClassType clazz = getClass("java.io.PrintStream");
		request.addClassFilter(clazz);

		Event e = triggerAndWait(request, "BreakpointEvent", true);
		assertEquals(request, e.request());

		MethodExitEvent event = (MethodExitEvent) e;
		Method m = event.method();
		ReferenceType r = m.location().declaringType();
		assertEquals(clazz, r);
		fVM.eventRequestManager().deleteEventRequest(request);
	}

	/**
	 * Retests a method exit request with a specific class filter
	 */
	public void testJDIWithClassFilter2() {
		MethodExitRequest request = getMethodExitRequest();
		request.addClassFilter("java.io.PrintStream");

		Event e = triggerAndWait(request, "BreakpointEvent", true);
		assertEquals(request, e.request());

		MethodExitEvent event = (MethodExitEvent) e;
		Method m = event.method();
		ReferenceType r = m.location().declaringType();
		assertEquals("java.io.PrintStream", r.name());
		fVM.eventRequestManager().deleteEventRequest(request);
	}

	/**
	 * Test a method exit request with a thread filter
	 */
	public void testJDIWithThreadFilter() {
		MethodExitRequest request = getMethodExitRequest();
		ThreadReference thr = getMainThread();
		request.addThreadFilter(thr);

		Event e = triggerAndWait(request, "BreakpointEvent", true);
		assertEquals(request, e.request());

		MethodExitEvent event = (MethodExitEvent) e;
		assertEquals(thr, event.thread());
		fVM.eventRequestManager().deleteEventRequest(request);
	}

}
