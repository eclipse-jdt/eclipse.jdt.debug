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

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;

/**
 * Tests for the hot code replacement JDI extension.
 */
public class HotCodeReplacementTest extends AbstractJDITest {
	/**
	 * Creates a new test.
	 */
	public HotCodeReplacementTest() {
		super();
	}
	private void dropTopFrame(
		ThreadReference thread,
		org.eclipse.jdi.hcr.ThreadReference hcrThread) {
		// Get stack size
		int stackSize = 0;
		try {
			stackSize = thread.frames().size();
		} catch (IncompatibleThreadStateException e) {
			assertTrue("dropTopFrame.1", false);
		}

		// Create and install step out request
		StepRequest request =
			fVM.eventRequestManager().createStepRequest(
				thread,
				StepRequest.STEP_MIN,
				StepRequest.STEP_OUT);
		request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		request.enable();

		// Prepare to receive the event
		EventWaiter waiter = new EventWaiter(request, false);
		fEventReader.addEventListener(waiter);

		// Do return
		boolean finallyBlocksSkipped = hcrThread.doReturn(null, true);
		assertTrue("dropTopFrame.2", !finallyBlocksSkipped);

		// Wait for the event to come in
		Event event = waitForEvent(waiter, 10000); // Wait 10s max
		assertTrue("dropTopFrame.3", event != null);
		fEventReader.removeEventListener(waiter);
		fVM.eventRequestManager().deleteEventRequest(request);

		// Check thread has dropped top frame
		assertTrue("dropTopFrame.4", thread.isSuspended());
		int newStackSize = 0;
		try {
			newStackSize = thread.frames().size();
		} catch (IncompatibleThreadStateException e) {
			assertTrue("dropTopFrame.5", false);
		}
		assertEquals("dropTopFrame.6", stackSize - 1, newStackSize);
	}

	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		waitUntilReady();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(String[] args) {
		new HotCodeReplacementTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Hot code replacement extension to JDI (org.eclipse.jdi.hcr) tests";
	}
	private void reenterOnExit(ThreadReference thread) {
		// Get top frame's location
		Location location = null;
		try {
			StackFrame frame = thread.frames(0, 1).get(0);
			location = frame.location();
		} catch (IncompatibleThreadStateException e) {
			assertTrue("reenterOnExit.1", false);
		}

		// Create and install reenter step request
		org.eclipse.jdi.hcr.EventRequestManager eventRequestManager =
			(org.eclipse.jdi.hcr.EventRequestManager) fVM.eventRequestManager();
		org.eclipse.jdi.hcr.ReenterStepRequest request =
			eventRequestManager.createReenterStepRequest(thread);
		request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		request.enable();

		// Prepare to receive the step event
		EventWaiter waiter = new EventWaiter(request, false);
		fEventReader.addEventListener(waiter);

		// Resume thread with a doReturn so that the frame is reentered right away
		 ((org.eclipse.jdi.hcr.ThreadReference) thread).doReturn(null, false);

		// Wait for the step event to come in
		StepEvent event = (StepEvent) waitForEvent(waiter, 10000); // Wait 10s max
		assertTrue("reenterOnExit.2", event != null);
		fEventReader.removeEventListener(waiter);
		fVM.eventRequestManager().deleteEventRequest(request);

		// Check that the top frame location is as expected
		Location newLocation = null;
		try {
			StackFrame frame = thread.frames(0, 1).get(0);
			newLocation = frame.location();
		} catch (IncompatibleThreadStateException e) {
			assertTrue("reenterOnExit.3", false);
		}
		assertTrue("reenterOnExit.4", !newLocation.equals(location));
		assertTrue("reenterOnExit.5", newLocation.codeIndex() <= location.codeIndex());

	}
	private void reloadClasses() {
		// Gets the old class
		ReferenceType oldType = getMainClass();

		// Create and install class unload and class prepare event requests
		ClassUnloadRequest unloadRequest =
			fVM.eventRequestManager().createClassUnloadRequest();
		unloadRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		unloadRequest.enable();
		ClassPrepareRequest loadRequest =
			fVM.eventRequestManager().createClassPrepareRequest();
		loadRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		loadRequest.enable();

		// Prepare to receive the class unload event
		EventWaiter unloadEventWaiter = new EventWaiter(unloadRequest, true);
		fEventReader.addEventListener(unloadEventWaiter);

		// Prepare to receive the class prepare event
		EventWaiter loadEventWaiter = new EventWaiter(loadRequest, true);
		fEventReader.addEventListener(loadEventWaiter);

		// Reload classes
		org.eclipse.jdi.hcr.VirtualMachine vm =
			(org.eclipse.jdi.hcr.VirtualMachine) fVM;
		int result =
			vm.classesHaveChanged(
				new String[] { "org.eclipse.debug.jdi.tests.program.MainClass" });
		assertEquals("reloadClasses.1", org.eclipse.jdi.hcr.VirtualMachine.RELOAD_SUCCESS, result);

		// Wait for the class unload event to come in
		ClassUnloadEvent unloadEvent =
			(ClassUnloadEvent) waitForEvent(unloadEventWaiter, 10000);
		// Wait 10s max
		assertTrue("reloadClasses.2", unloadEvent != null);
		fEventReader.removeEventListener(unloadEventWaiter);
		fVM.eventRequestManager().deleteEventRequest(unloadRequest);
		assertEquals(
			"reloadClasses.3",
			"org.eclipse.debug.jdi.tests.program.MainClass",
			unloadEvent.className());

		// Wait for the class prepare event to come in
		ClassPrepareEvent loadEvent =
			(ClassPrepareEvent) waitForEvent(loadEventWaiter, 10000);
		// Wait 10s max
		assertTrue("reloadClasses.4", loadEvent != null);
		fEventReader.removeEventListener(loadEventWaiter);
		fVM.eventRequestManager().deleteEventRequest(loadRequest);
		ReferenceType newType = loadEvent.referenceType();
		assertEquals(
			"reloadClasses.5",
			"org.eclipse.debug.jdi.tests.program.MainClass",
			newType.name());
		assertTrue("reloadClasses.6", !oldType.equals(newType));
	}
	/**
	 * Use case 1:
	 * . get a thread and suspend it
	 * . get hot code replacement capabilities
	 * . drop the top a frame
	 * . reload some classes
	 * . request reeenter on exit
	 * . resume thread
	 * . get step event
	 * . get class file version for some classes
	 */
	public void testJDIUseCase1() {
		// Get the suspended thread
		ThreadReference thread = getThread();
		assertTrue("1", thread.isSuspended());
		assertEquals("2", 1, thread.suspendCount());
		org.eclipse.jdi.hcr.ThreadReference hcrThread =
			(org.eclipse.jdi.hcr.ThreadReference) thread;

		org.eclipse.jdi.hcr.VirtualMachine vm =
			(org.eclipse.jdi.hcr.VirtualMachine) fVM;


		// Drop the top a frame
		try {
			if (vm.canDoReturn()) {
				dropTopFrame(thread, hcrThread);
			}
		} catch (UnsupportedOperationException e) {
			//not using a VM that supports the extension
			return;
		}

		// Reload classes
		if (vm.canReloadClasses())
			reloadClasses();

		// Reenter on exit
		if (vm.canReenterOnExit())
			reenterOnExit(thread);
	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		waitUntilReady();
	}
}
