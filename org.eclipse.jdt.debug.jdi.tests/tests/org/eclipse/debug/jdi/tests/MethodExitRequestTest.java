/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

public class MethodExitRequestTest extends AbstractJDITest {

	public void localSetUp() {
	}
	public void localTearDown() {
		fVM.resume();
		waitUntilReady();
	}

	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new MethodExitRequestTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
	public String getName() {
		return "com.sun.jdi.MethodExitRequest";
	}

	protected MethodExitRequest getMethodExitRequest() {
		return fVM.eventRequestManager().createMethodExitRequest();
	}
	public void testJDIWithoutFilter() {
		MethodExitRequest request = getMethodExitRequest();

		Event e = triggerAndWait(request, "BreakpointEvent", true);
		assertEquals(request, e.request());

		MethodExitEvent event = (MethodExitEvent) e;
		assertEquals(getThread(), event.thread());
		fVM.eventRequestManager().deleteEventRequest(request);
	}

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
