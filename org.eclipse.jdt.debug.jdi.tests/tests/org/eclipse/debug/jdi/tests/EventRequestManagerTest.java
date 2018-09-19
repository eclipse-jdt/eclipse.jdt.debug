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

import java.util.List;
import java.util.ListIterator;

import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;

/**
 * Tests for JDI com.sun.jdi.request.EventRequestManager.
 */
public class EventRequestManagerTest extends AbstractJDITest {

	private EventRequestManager fManager;
	/**
	 * Creates a new test.
	 */
	public EventRequestManagerTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get the event request manager
		fManager = fVM.eventRequestManager();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new EventRequestManagerTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.request.EventRequestManager";
	}
	/**
	 * Test JDI createAccessWatchpointRequest(Field), accessWatchpointRequests()
	 * and deleteEventRequest(EventRequest)
	 */
	public void testJDIAccessWatchpointRequest() {
		if (!fVM.canWatchFieldAccess())
			return;

		// Create an access watchpoint request
		Field field = getField();
		AccessWatchpointRequest request = fManager.createAccessWatchpointRequest(field);
		assertEquals("1", field, request.field());

		// Get all access watchpoint requests
		List<?> requests = fManager.accessWatchpointRequests();
		ListIterator<?> iterator = requests.listIterator();
		int i=0;
		while (iterator.hasNext()) {
			assertTrue("2."+i++, iterator.next() instanceof AccessWatchpointRequest);
		}

		// Delete an access watchpoint request
		fManager.deleteEventRequest(request);
		assertEquals("3", 0, fManager.accessWatchpointRequests().size());
	}
	/**
	 * Test JDI createBreakpointRequest(Location), breakpointRequests(),
	 * deleteEventRequest(EventRequest) and deleteAllBreakpoints()
	 */
	public void testJDIBreakpointRequest() {
		// Create a breakpoint at a valid location
		Location location = getLocation();
		BreakpointRequest bp = fManager.createBreakpointRequest(location);
		assertEquals("1", location, bp.location());

		// Get all breakpoints
		List<?> breakpoints = fManager.breakpointRequests();
		ListIterator<?> iterator = breakpoints.listIterator();
		while (iterator.hasNext()) {
			Object breakpoint = iterator.next();
			assertTrue("3", breakpoint instanceof BreakpointRequest);
		}

		// Delete a breakpoint
		fManager.deleteEventRequest(bp);
		assertEquals("4", 0, fManager.breakpointRequests().size());

		// Delete all breakpoints
		fManager.createBreakpointRequest(location);
		fManager.deleteAllBreakpoints();
		assertEquals("5", 0, fManager.breakpointRequests().size());
	}
	/**
	 * Test JDI createClassPrepareRequest(), classPrepareRequests()
	 * and deleteEventRequest(EventRequest)
	 */
	public void testJDIClassPrepareRequest() {
		// Create a class prepare request
		ClassPrepareRequest request= fManager.createClassPrepareRequest();

		// Get all class prepare requests
		List<?> requests = fManager.classPrepareRequests();
		ListIterator<?> iterator = requests.listIterator();
		int i=0;
		while (iterator.hasNext()) {
			assertTrue("1."+i++, iterator.next() instanceof ClassPrepareRequest);
		}

		// Delete a class prepare request
		fManager.deleteEventRequest(request);
		assertEquals("2", 0, fManager.classPrepareRequests().size());
	}
	/**
	 * Test JDI createClassUnloadRequest(), classUnloadRequests()
	 * and deleteEventRequest(EventRequest)
	 */
	public void testJDIClassUnloadRequest() {
		// Create a class unload request
		ClassUnloadRequest request= fManager.createClassUnloadRequest();

		// Get all class unload requests
		List<?> requests = fManager.classUnloadRequests();
		ListIterator<?> iterator = requests.listIterator();
		int i=0;
		while (iterator.hasNext()) {
			assertTrue("1."+i++, iterator.next() instanceof ClassUnloadRequest);
		}

		// Delete a class unload request
		fManager.deleteEventRequest(request);
		assertEquals("2", 0, fManager.classUnloadRequests().size());
	}
	/**
	 * Test JDI createExceptionRequest(), exceptionRequests()
	 * and deleteEventRequest(EventRequest)
	 */
	public void testJDIExceptionRequest() {
		// Create a exception request
		ExceptionRequest request= fManager.createExceptionRequest(null, true, true);

		// Get all exception requests
		List<?> requests = fManager.exceptionRequests();
		ListIterator<?> iterator = requests.listIterator();
		int i=0;
		while (iterator.hasNext()) {
			assertTrue("1."+i++, iterator.next() instanceof ExceptionRequest);
		}

		// Delete a exception request
		fManager.deleteEventRequest(request);
		assertEquals("2", i-1, fManager.exceptionRequests().size());
	}
	/**
	 * Test JDI createModificationWatchpointRequest(Field),
	 * accessWatchpointRequests() and deleteEventRequest(EventRequest)
	 */
	public void testJDIModificationWatchpointRequest() {
		if (!fVM.canWatchFieldAccess())
			return;

		// Create a modification watchpoint
		Field field = getField();
		ModificationWatchpointRequest request = fManager.createModificationWatchpointRequest(field);
		assertEquals("1", field, request.field());

		// Get all modification watchpoints
		List<?> requests = fManager.modificationWatchpointRequests();
		ListIterator<?> iterator = requests.listIterator();
		int i=0;
		while (iterator.hasNext()) {
			assertTrue("2."+i++, iterator.next() instanceof ModificationWatchpointRequest);
		}

		// Delete a modification watchpoint
		fManager.deleteEventRequest(request);
		assertEquals("3", 0, fManager.modificationWatchpointRequests().size());
	}
	/**
	 * Test JDI createStepRequest(), stepRequests()
	 * and deleteEventRequest(EventRequest)
	 */
	public void testJDIStepRequest() {
		// Create a step request
		StepRequest request= fManager.createStepRequest(getThread(), StepRequest.STEP_LINE, StepRequest.STEP_OVER);

		// Get all step requests
		List<?> requests = fManager.stepRequests();
		ListIterator<?> iterator = requests.listIterator();
		int i=0;
		while (iterator.hasNext()) {
			assertTrue("1."+i++, iterator.next() instanceof StepRequest);
		}

		// Delete a step request
		fManager.deleteEventRequest(request);
		assertEquals("2", 0, fManager.stepRequests().size());
	}
	/**
	 * Test JDI createThreadDeathRequest(), threadDeathRequests()
	 * and deleteEventRequest(EventRequest)
	 */
	public void testJDIThreadDeathRequest() {
		// Create a ThreadDeath request
		ThreadDeathRequest request= fManager.createThreadDeathRequest();

		// Get all ThreadDeath requests
		List<?> requests = fManager.threadDeathRequests();
		ListIterator<?> iterator = requests.listIterator();
		int i=0;
		while (iterator.hasNext()) {
			assertTrue("1."+i++, iterator.next() instanceof ThreadDeathRequest);
		}

		// Delete a ThreadDeath request
		fManager.deleteEventRequest(request);
		assertEquals("2", 0, fManager.threadDeathRequests().size());
	}
	/**
	 * Test JDI createThreadStartRequest(), classUnloadRequests()
	 * and deleteEventRequest(EventRequest)
	 */
	public void testJDIThreadStartRequest() {
		// Create a ThreadStart request
		ThreadStartRequest request= fManager.createThreadStartRequest();

		// Get all ThreadStart requests
		List<?> requests = fManager.classUnloadRequests();
		ListIterator<?> iterator = requests.listIterator();
		int i=0;
		while (iterator.hasNext()) {
			assertTrue("1."+i++, iterator.next() instanceof ThreadStartRequest);
		}

		// Delete a ThreadStart request
		fManager.deleteEventRequest(request);
		assertEquals("2", 0, fManager.classUnloadRequests().size());
	}
}
