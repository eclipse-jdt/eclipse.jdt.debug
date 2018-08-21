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
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.MethodEntryRequest;

/**
 * Tests a method entry request
 */
public class MethodEntryRequestTest extends AbstractJDITest {

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
		new MethodEntryRequestTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.MethodEntryRequest";
	}

	/**
	 * Creates and returns a new <code>MethodEntryRequest</code>
	 * @return a new <code>MethodEntryRequest</code>
	 */
	protected MethodEntryRequest getMethodEntryRequest() {
		return fVM.eventRequestManager().createMethodEntryRequest();
	}

	/**
	 * Tests a request without any filtering
	 */
	public void testJDIWithoutFilter() {
		MethodEntryRequest request = getMethodEntryRequest();

		Event e = triggerAndWait(request, "BreakpointEvent", true);
		assertEquals(request, e.request());

		MethodEntryEvent event = (MethodEntryEvent) e;
		assertEquals(getThread(), event.thread());
		fVM.eventRequestManager().deleteEventRequest(request);
	}

	/**
	 * Test a request with class exclusion filtering
	 */
	public void testJDIWithClassExclusionFilter() {
		MethodEntryRequest request = getMethodEntryRequest();
		request.addClassExclusionFilter("org.eclipse.debug.jdi.tests.program.*");

		Event e = triggerAndWait(request, "BreakpointEvent", true);
		assertEquals(request, e.request());

		MethodEntryEvent event = (MethodEntryEvent) e;
		Method m = event.method();
		ReferenceType r = m.location().declaringType();
		assertTrue("1", !r.name().startsWith("org.eclipse.debug.jdi.tests.program."));
		fVM.eventRequestManager().deleteEventRequest(request);
	}

	/**
	 * Tests a method entry request with a specified class filter
	 */
	public void testJDIWithClassFilter1() {
		MethodEntryRequest request = getMethodEntryRequest();
		ClassType clazz = getClass("java.io.PrintStream");
		request.addClassFilter(clazz);

		Event e = triggerAndWait(request, "BreakpointEvent", true);
		assertEquals(request, e.request());

		MethodEntryEvent event = (MethodEntryEvent) e;
		Method m = event.method();
		ReferenceType r = m.location().declaringType();
		assertEquals(clazz, r);
		fVM.eventRequestManager().deleteEventRequest(request);
	}

	/**
	 * Retests a method entry request with a specified class filter
	 */
	public void testJDIWithClassFilter2() {
		MethodEntryRequest request = getMethodEntryRequest();
		request.addClassFilter("java.io.PrintStream");

		Event e = triggerAndWait(request, "BreakpointEvent", true);
		assertEquals(request, e.request());

		MethodEntryEvent event = (MethodEntryEvent) e;
		Method m = event.method();
		ReferenceType r = m.location().declaringType();
		assertEquals("java.io.PrintStream", r.name());
		fVM.eventRequestManager().deleteEventRequest(request);
	}

	/**
	 * Tests a method entry request with a thread filter
	 */
	public void testJDIWithThreadFilter() {
		MethodEntryRequest request = getMethodEntryRequest();
		ThreadReference thr = getMainThread();
		request.addThreadFilter(thr);

		Event e = triggerAndWait(request, "BreakpointEvent", true);
		assertEquals(request, e.request());

		MethodEntryEvent event = (MethodEntryEvent) e;
		assertEquals(thr, event.thread());
		fVM.eventRequestManager().deleteEventRequest(request);
	}
}
