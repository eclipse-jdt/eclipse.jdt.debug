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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import junit.framework.Test;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.ThreadStartEvent;

/**
 * Tests for JDI com.sun.jdi.ObjectReference
 * and JDWP Object command set.
 */
public class ObjectReferenceTest extends AbstractJDITest {

	private ObjectReference fObject;

	/**
	 * Creates a new test.
	 */
	public ObjectReferenceTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Make sure the object is in expected state (eg. it has not entered a monitor)
		waitUntilReady();

		// Get static field "fObject"
		fObject = getObjectReference();
	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		// The test has resumed and suspended the Test Thread. Make sure this
		// thread is suspended at the right location
		waitUntilReady();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new ObjectReferenceTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.ObjectReference";
	}
	/**
	 * Returns all tests
	 */
	@Override
	protected Test suite() {
		JDITestSuite suite = (JDITestSuite) super.suite();
		Vector<?> testNames = getAllMatchingTests("testLast");
		Iterator<?> iterator = testNames.iterator();
		while (iterator.hasNext()) {
			String name = (String) iterator.next();
			suite.addTest(new JDITestCase(this, name));
		}
		return suite;
	}
	/**
	 * Test JDI disableCollection(). enableCollection() and isCollected().
	 */
	public void testJDIDisableEnableCollection() {
		assertTrue("1", !fObject.isCollected());
		fObject.disableCollection();
		fObject.enableCollection();
	}
	/**
	 * Test JDI entryCount().
	 */
	public void testJDIEntryCount() {
		if (fVM.canGetMonitorInfo()) {
			// Ensure we're in a good state
			fVM.resume();
			waitUntilReady();

			try {
				assertEquals("1", 1, fObject.entryCount());
			} catch (IncompatibleThreadStateException e) {
				assertTrue("2", false);
			}
		}
	}
	/**
	 * Test JDI equals() and hashCode().
	 */
	public void testJDIEquality() {
		assertTrue("1", fObject.equals(fObject));
		ObjectReference other = getThread();
		assertTrue("2", !fObject.equals(other));
		assertTrue("3", !fObject.equals(new Object()));
		assertTrue("4", !fObject.equals(null));
		assertTrue("5", fObject.hashCode() != other.hashCode());
	}
	/**
	 * Test JDI getValue(Field), getValues(List) and setValue(Field,Value)
	 * and JDWP 'Object - Get Fields Values' and 'Object - Set Fields Values'.
	 */
	public void testJDIGetSetValues() {
		// setup
		ReferenceType type = fObject.referenceType();
		List<?> fields = type.fields();
		ListIterator<?> iterator = fields.listIterator();
		List<Field> instanceFields = new LinkedList<>();
		while (iterator.hasNext()) {
			Field field = (Field) iterator.next();
			if (!field.isStatic())
				instanceFields.add(field);
		}
		Field field = instanceFields.get(4);
		assertEquals("1", "fChar", field.name());

		// getValues(List)
		Map<?, ?> values = fObject.getValues(instanceFields);
		assertTrue("2", values.size() == 7);
		Value value = (Value) values.get(field);
		assertEquals("3", value, fVM.mirrorOf('a'));

		// setValue(Field,Value)
		Value newValue = fVM.mirrorOf('b');
		try {
			fObject.setValue(field, newValue);
		} catch (ClassNotLoadedException e) {
			assertTrue("4.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("4.2", false);
		}

		// getValue(Field)
		assertEquals("5", fObject.getValue(field), newValue);

		// test set and get null value.
		field = instanceFields.get(5);

		assertEquals("6", "fString2", field.name());
		try {
			fObject.setValue(field, null);
		} catch (ClassNotLoadedException e) {
			assertTrue("7.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("7.2", false);
		}

		// getValue(Field)
		assertEquals("8", fObject.getValue(field), null);

		// test get final value.
		field = instanceFields.get(6);
		assertEquals("9", "fString3", field.name());

		// The value is null and should be because it's final
		//assertEquals("10", fVM.mirrorOf("HEY"), fObject.getValue(field));

	}
	/**
	 * Test JDI invokeMethod.
	 */
	public void testJDIInvokeMethod() {
		// Make sure the entire VM is not suspended before we start a new thread
		// (otherwise this new thread will start suspended and we will never get the
		// ThreadStart event)
		fVM.resume();
		waitUntilReady();

		ThreadStartEvent event =
			(ThreadStartEvent) triggerAndWait(fVM
				.eventRequestManager()
				.createThreadStartRequest(),
				"ThreadStartEvent",
				false);
		ThreadReference thread = event.thread();
		ClassType ct = (ClassType) fObject.referenceType();
		Method inv =
			ct.concreteMethodByName("invoke3", "(Ljava/lang/String;Ljava/lang/Object;)I");
		List<StringReference> args = new ArrayList<>();
		args.add(fVM.mirrorOf("888"));
		args.add(null);
		Exception oops = null;
		Value val = null;
		try {
			val = fObject.invokeMethod(thread, inv, args, 0);
		} catch (ClassNotLoadedException exc) {
			oops = exc;
		} catch (IncompatibleThreadStateException exc) {
			oops = exc;
		} catch (InvalidTypeException exc) {
			oops = exc;
		} catch (InvocationException exc) {
			oops = exc;
		}
		assertTrue("1", oops == null);
		assertEquals("2", val == null ? 0 : ((IntegerValue) val).value(), 888);
	}
	/**
	 * Test JDI invokeMethod - failure.
	 */
	public void testJDIInvokeMethodFail() {
		// Make sure the entire VM is not suspended before we start a new thread
		// (otherwise this new thread will start suspended and we will never get the
		// ThreadStart event)
		fVM.resume();
		waitUntilReady();

		ThreadStartEvent event =
			(ThreadStartEvent) triggerAndWait(fVM
				.eventRequestManager()
				.createThreadStartRequest(),
				"ThreadStartEvent",
				false);
		ThreadReference thread = event.thread();
		ClassType ct = (ClassType) fObject.referenceType();
		Method inv = ct.concreteMethodByName("invoke4", "()J");
		Exception good = null, oops = null;
		try {
			fObject.invokeMethod(thread, inv, new ArrayList<Value>(), 0);
		} catch (ClassNotLoadedException exc) {
			oops = exc;
		} catch (IncompatibleThreadStateException exc) {
			oops = exc;
		} catch (InvalidTypeException exc) {
			oops = exc;
		} catch (InvocationException exc) {
			good = exc;
		}
		assertTrue("1", oops == null);
		assertTrue("2", good != null);
	}
	/**
	 * Test JDI owningThread().
	 */
	public void testJDIOwningThread() {
		if (fVM.canGetMonitorInfo()) {
			// Ensure we're in a good state
			fVM.resume();
			waitUntilReady();

			try {
				assertEquals("1", getThread(), fObject.owningThread());
			} catch (IncompatibleThreadStateException e) {
				assertTrue("2", false);
			}
		}
	}
	/**
	 * Test JDI referenceType() and JDWP 'Type - Get type'.
	 */
	public void testJDIReferenceType() {
		ReferenceType type = fObject.referenceType();
		assertEquals("1", type.name(), "org.eclipse.debug.jdi.tests.program.MainClass");
	}
	/**
	 * Test JDI uniqueID().
	 */
	public void testJDIUniqueID() {
		fObject.uniqueID();
	}
	/**
	 * Test JDI waitingThreads().
	 */
	public void testJDIWaitingThreads() {
		if (fVM.canGetMonitorInfo()) {
			try {
				assertEquals("1", 0, fObject.waitingThreads().size());
			} catch (IncompatibleThreadStateException e) {
				assertTrue("2", false);
			}
		}
	}
}
