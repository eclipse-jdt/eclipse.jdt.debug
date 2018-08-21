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

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;

/**
 * Test cases for the implementation of heap walking in the new java 1.6 VM
 *
 * @since 3.3
 */
public class HeapWalkingTests extends AbstractJDITest {

	private ReferenceType fClass, fClass1;
	private ObjectReference fObject;

	/**	 setup our tests */
	@Override
	public void localSetUp() {}

	/** tear down our tests */
	@Override
	public void localTearDown() {
		super.localTearDown();
		fClass = null;
		fClass1 = null;
		fObject = null;
	}

	/**
	 * test to make sure that the VM supports getting instance info for heap walking if it is a 1.6 VM
	 */
	public void testCanGetInstanceInfo() {
		if(is16OrGreater()) {
			assertTrue("Should have instance info", fVM.canGetInstanceInfo());
		}
		else {
			assertTrue("Should not have instance info", !fVM.canGetInstanceInfo());
		}
	}

	/**
	 * tests the new method instanceCounts, to make sure it throws an NPE when required.
	 * test is not applicable to non 1.6 VMs
	 */
	public void testGetInstanceCountsNullAttrib() {
		if(!fVM.canGetInstanceInfo()) {
			return;
		}
		try {
			fVM.instanceCounts(null);
			assertTrue("No excpetion thrown", false);
		}
		catch (NullPointerException npe) {}
	}

	/**
	 * tests to make sure the instanceCounts method throws a not supported
	 */
	public void testGetInstanceCountsUnsupported() {
		if(is16OrGreater()) {
			try {
				fVM.instanceCounts(new ArrayList<ReferenceType>());
			}
			catch(UnsupportedOperationException uoe) {
				assertTrue("Threw unsupported exception in 1.6 VM", false);
			}
		}
		else {
			try {
				fVM.instanceCounts(new ArrayList<ReferenceType>());
				assertTrue("No exception for non 1.6 VM", false);
			}
			catch(UnsupportedOperationException uoe) {}
		}
	}

	/**
	 * test to collect any referring instances can be collected for the specified class.
	 * test is not applicable to non 1.6 VMs.
	 */
	public void testGetInstanceCounts() {
		if(!fVM.canGetInstanceInfo()) {
			return;
		}
		triggerAndWait(fVM.eventRequestManager().createClassPrepareRequest(), "refclass3load", true);
		triggerAndWait(fVM.eventRequestManager().createClassPrepareRequest(), "refclassload", true);
		ArrayList<ReferenceType> list = new ArrayList<>(2);
		fClass = getClass("org.eclipse.debug.jdi.tests.program.RefClass1");
		assertNotNull("RefClass should not be null", fClass);
		list.add(fClass);
		fClass1 = getClass("org.eclipse.debug.jdi.tests.program.RefClass2");
		list.add(fClass1);
		long[] counts = fVM.instanceCounts(list);
		assertNotNull("counts should not be null", counts);
		assertTrue("counts should have two entires", counts.length == 2);
		assertTrue("count for RefClass1 should be 2", counts[0] == 2);
		assertTrue("count for RefClass2 should be 1", counts[1] == 1);
	}

	/**
	 * test to make sure instances throws an unsupported exception for non 1.6 VMs
	 */
	public void testGetInstancesUnsupported() {
		fClass = getClass("java.io.PrintStream");
		assertNotNull("classs should not be null", fClass);
		if(is16OrGreater()) {
			try {
				fClass.instances(20);
			}
			catch(UnsupportedOperationException uoe) {
				assertTrue("Threw unsupported exception in 1.6 VM", false);
			}
		}
		else {
			try {
				fClass.instances(20);
				assertTrue("No exception for non 1.6 VM", false);
			}
			catch(UnsupportedOperationException uoe) {}
		}
	}

	/**
	 * test to make sure instances throws and IllegalArgument exception for negative long arguments.
	 * test is not applicable to non 1.6 VMs
	 */
	public void testGetInstancesNegativeMax() {
		if(!fVM.canGetInstanceInfo()) {
			return;
		}
		fClass = getClass("java.io.PrintStream");
		assertNotNull("classs should not be null", fClass);
		try {
			fClass.instances(-1);
			assertTrue("No excpetion thrown", false);
		}
		catch (IllegalArgumentException iae) {}
	}

	/**
	 * test to collect a list of instances.
	 * test is not applicable to non 1.6 VMs.
	 */
	public void testGetInstances() {
		if(!fVM.canGetInstanceInfo()) {
			return;
		}
		triggerAndWait(fVM.eventRequestManager().createClassPrepareRequest(), "refclass3load", true);
		triggerAndWait(fVM.eventRequestManager().createClassPrepareRequest(), "refclassload", true);
		fClass = getClass("org.eclipse.debug.jdi.tests.program.RefClass3");
		assertNotNull("RefClass3 should not be null", fClass);
		fClass1 = getClass("org.eclipse.debug.jdi.tests.program.RefClass1");
		assertNotNull("RefClass1 should not be null", fClass1);
		List<?> list = fClass1.instances(10);
		assertNotNull("list should not be null", list);
		assertTrue("list should have two enrtries", list.size() == 2);
	}

	/**
	 * test to make sure referringObjects throws an unsupported exception for non-1.6 VMs
	 */
	public void testGetReferringObjectsUnsupported() {
		fClass = getMainClass();
		assertNotNull("main class ref should not be null", fClass);
		fObject = getObjectReference();
		assertNotNull("String obj ref should not be null", fObject);
		if(is16OrGreater()) {
			try {
				fObject.referringObjects(100);
			}
			catch(UnsupportedOperationException uoe) {
				assertTrue("Threw unsupported exception in 1.6 VM", false);
			}
		}
		else {
			try {
				fObject.referringObjects(10);
				assertTrue("No exception for non 1.6 VM", false);
			}
			catch(UnsupportedOperationException uoe) {}
		}
	}

	/**
	 * test to make sure referringObjects throws an IllegalArgument exception for bad values of max
	 */
	public void testGetreferringObjectsNegativeMax() {
		if(!fVM.canGetInstanceInfo()) {
			return;
		}
		fClass = getMainClass();
		assertNotNull("main class ref should not be null", fClass);
		fObject = getStringReference();
		assertNotNull("String obj ref should not be null", fObject);
		try {
			fObject.referringObjects(-1);
			assertTrue("No excpetion thrown", false);
		}
		catch (IllegalArgumentException iae) {
			assertTrue("Threw exception", true);
		}
	}

	/**
	 * tests the method referring objects to ensure working to spec.
	 * test is not applicable to non 1.6 VMs
	 */
	public void testGetReferringObjects() {
		if(!fVM.canGetInstanceInfo()) {
			return;
		}
		fClass = getMainClass();
		assertNotNull("main class ref should not be null", fClass);
		triggerAndWait(fVM.eventRequestManager().createClassPrepareRequest(), "refclassload", true);
		fClass1 = getClass("org.eclipse.debug.jdi.tests.program.RefClass");
		assertNotNull("RefClass should not be null", fClass1);
		fObject = getObjectReference();
		assertNotNull("String obj ref should not be null", fObject);
		List<?> list = fObject.referringObjects(100);
		assertNotNull("referring objects list should not be null", list);
		assertTrue("list size should be 4", list.size() == 4);
		assertTrue("list should contain the main class", list.contains(fClass.classObject()));
		assertTrue("list should contain the main class thread", list.contains(getThread()));
	}
}
