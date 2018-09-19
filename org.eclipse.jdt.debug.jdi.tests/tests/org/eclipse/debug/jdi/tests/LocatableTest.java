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
import java.util.ListIterator;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Locatable;
import com.sun.jdi.Location;
import com.sun.jdi.Mirror;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;

/**
 * Tests for JDI com.sun.jdi.Locatable.
 */
public class LocatableTest extends AbstractJDITest {

	private List<Mirror> fLocatables = new LinkedList<>();
	private List<EventRequest> fRequests = new LinkedList<>();
	/**
	 * Creates a new test.
	 */
	public LocatableTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get all kinds of locatable

		BreakpointRequest bp = getBreakpointRequest();
		fLocatables.add(bp); // BreakpointRequest

		//ExceptionRequest er = getExceptionRequest();
		//fLocatables.add(triggerAndWait(er, 'e')); // ExceptionEvent

		fLocatables.add(getMethod()); // Method

		fLocatables.add(triggerStepAndWait()); // StepEvent

		fRequests.add(bp);
		fLocatables.add(triggerAndWait(bp, "BreakpointEvent", true));
		// BreakpointEvent

		if (fVM.canWatchFieldAccess()) {
			AccessWatchpointRequest ap = getAccessWatchpointRequest();
			fRequests.add(ap);
			fLocatables.add(triggerAndWait(ap, "AccessWatchpointEvent", true));
			// AccessWatchpointEvent
		}

		if (fVM.canWatchFieldModification()) {
			ModificationWatchpointRequest mp =
				getModificationWatchpointRequest();
			fRequests.add(mp);
			fLocatables.add(
				triggerAndWait(mp, "ModificationWatchpointEvent", false));
			// ModificationWatchpointEvent
		}

		// Note we can use the stack frame only if the thread is suspended,
		// that's why the previoue triggerAndWait doesn't resume the thread
		// and this Locatable is added last in the list
		fLocatables.add(getFrame(RUN_FRAME_OFFSET)); // StackFrame

	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		// Ensure that the modification of the "fBool" field has completed
		fVM.resume();
		waitUntilReady();

		// Delete the event requests we created in this test
		fVM.eventRequestManager().deleteEventRequests(fRequests);

		// Set the value of the "fBool" field back to its original value
		resetField();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new LocatableTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.Locatable";
	}
	/**
	 * Test JDI location()
	 */
	public void testJDILocation() {
		ListIterator<Mirror> iterator = fLocatables.listIterator();
		while (iterator.hasNext()) {
			Locatable locatable = (Locatable) iterator.next();
			Location location = locatable.location();
			assertTrue("1." + locatable, location != null);
			assertTrue(
				"2." + locatable,
				(location.codeIndex()) >= 0 || (location.codeIndex() == -1));
			assertTrue("3." + locatable, location.declaringType() != null);
			assertTrue(
				"4." + locatable,
				(location.lineNumber() > 0) || (location.lineNumber() == -1));
			assertNotNull("5", location.method());
			try {
				location.sourceName();
			} catch (AbsentInformationException e) {
				assertTrue("7", false);
			}
		}
	}
}
