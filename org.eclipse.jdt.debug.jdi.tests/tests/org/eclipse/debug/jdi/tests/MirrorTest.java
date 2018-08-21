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

import com.sun.jdi.Mirror;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;

/**
 * Tests for JDI com.sun.jdi.Mirror.
 */
public class MirrorTest extends AbstractJDITest {
	List<Mirror> fMirrors = new LinkedList<>();
	/**
	 * Creates a new test.
	 */
	public MirrorTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get all kinds of concrete mirror that can be found in the VM
		// in alphabetical order.

		//TO DO: Add events too

		fMirrors = new LinkedList<>();

		if (fVM.canWatchFieldAccess())
			fMirrors.add(getAccessWatchpointRequest());
		// AccessWatchpointRequest

		fMirrors.add(getObjectArrayReference()); // ArrayReference
		fMirrors.add(getArrayType()); // ArrayType
		fMirrors.add(fVM.mirrorOf(true)); // BooleanValue
		fMirrors.add(getBreakpointRequest()); // BreakpointRequest
		fMirrors.add(fVM.mirrorOf((byte) 1)); // ByteValue
		fMirrors.add(fVM.mirrorOf('1')); // CharValue
		fMirrors.add(getClassLoaderReference()); // ClassLoaderReference
		fMirrors.add(getMainClass()); // ClassType
		fMirrors.add(fVM.mirrorOf(12345.6789)); // DoubleValue
		fMirrors.add(fVM.eventRequestManager()); // EventRequestManager
		fMirrors.add(fVM.eventQueue()); // EventQueue
		fMirrors.add(getField()); // Field
		fMirrors.add(fVM.mirrorOf(123.45f)); // FieldValue
		fMirrors.add(fVM.mirrorOf(12345)); // IntegerValue
		fMirrors.add(getInterfaceType()); // InterfaceType
		fMirrors.add(getLocalVariable()); // LocalVariable
		fMirrors.add(getLocation()); // Location
		fMirrors.add(fVM.mirrorOf(123456789l)); // LongValue
		fMirrors.add(getMethod()); // Method

		if (fVM.canWatchFieldModification())
			fMirrors.add(getModificationWatchpointRequest());
		// ModificationWatchpointRequest

		fMirrors.add(getObjectReference()); // ObjectReference
		fMirrors.add(fVM.mirrorOf((short) 12345)); // ShortValue
		fMirrors.add(getFrame(RUN_FRAME_OFFSET)); // StackFrame
		fMirrors.add(getStringReference()); // StringReference
		fMirrors.add(getThread().threadGroup()); // ThreadGroupReference
		fMirrors.add(getThread()); // ThreadReference
		fMirrors.add(fVM); // VirtualMachine
	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		ListIterator<Mirror> iterator = fMirrors.listIterator();
		while (iterator.hasNext()) {
			Object mirror = iterator.next();

			// Delete the access watchpoint request we created in this test
			if (mirror instanceof AccessWatchpointRequest)
				fVM.eventRequestManager().deleteEventRequest(
					(AccessWatchpointRequest) mirror);

			// Delete the breakpoint request we created in this test
			if (mirror instanceof BreakpointRequest)
				fVM.eventRequestManager().deleteEventRequest(
					(BreakpointRequest) mirror);

			// Delete the modification watchpoint request we created in this test
			if (mirror instanceof ModificationWatchpointRequest)
				fVM.eventRequestManager().deleteEventRequest(
					(ModificationWatchpointRequest) mirror);
		}
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new MirrorTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.Mirror";
	}
	/**
	 * Test JDI toString().
	 */
	public void testJDIToString() {
		for (int i = 0; i < fMirrors.size(); i++) {
			Mirror mirror = fMirrors.get(i);
			assertNotNull(Integer.toString(i), mirror.toString());
		}
	}
	/**
	 * Test JDI virtualMachine().
	 */
	public void testJDIVirtualMachine() {
		for (int i = 0; i < fMirrors.size(); i++) {
			Mirror mirror = fMirrors.get(i);
			assertEquals(Integer.toString(i), fVM, mirror.virtualMachine());
		}
	}
}
