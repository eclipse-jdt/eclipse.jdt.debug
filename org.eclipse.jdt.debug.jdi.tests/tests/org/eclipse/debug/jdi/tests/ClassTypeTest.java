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
import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.ThreadStartEvent;

/**
 * Tests for JDI com.sun.jdi.ClassType
 * and JDWP Class command set.
 */
public class ClassTypeTest extends AbstractJDITest {

	private ClassType fType;
	/**
	 * Creates a new test.
	 */
	public ClassTypeTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get the type org.eclipse.debug.jdi.tests.program.MainClass
		fType = getMainClass();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new ClassTypeTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.ClassType";
	}
	/**
	 * Test JDI allFields().
	 */
	public void testJDIAllFields() {
		boolean found = false;
		Iterator<?> it = fType.allFields().iterator();
		while (it.hasNext()) {
			Field fld = (Field) it.next();
			if (fld.name().equals("fString")) {
				found = true;
			}
		}
		assertTrue("1", found);
	}
	/**
	 * Test JDI allInterfaces().
	 */
	public void testJDIAllInterfaces() {
		List<?> all = fType.allInterfaces();
		boolean found = false;
		Iterator<?> interfaces = fType.allInterfaces().iterator();
		while (interfaces.hasNext()) {
			InterfaceType next = (InterfaceType) interfaces.next();
			assertTrue(next.name(), all.contains(next));
			if (next.name().equals("java.lang.Comparable"))
				found = true;
		}
		assertTrue("1", found);
	}
	/**
	 * Test JDI allMethods().
	 */
	public void testJDIAllMethods() {
		boolean found = false;
		Iterator<?> it = fType.allMethods().iterator();
		while (it.hasNext()) {
			Method mth = (Method) it.next();
			if (mth.name().equals("after")) { // in Date
				found = true;
			}
		}
		assertTrue("1", found);
	}
	/**
	 * Test JDI concreteMethodByName().
	 */
	public void testJDIConcreteMethodByName() {
		Method method = fType.concreteMethodByName("run", "()V");
		assertTrue("1", method != null);
		assertEquals("2", fType, method.declaringType());
		assertTrue("3", fType.concreteMethodByName("xxx", "(I)Z") == null);
	}
	/**
	 * Test JDI interfaces().
	 */
	public void testJDIInterfaces() {
		boolean found = false;
		boolean extra = false;
		List<?> interfaces = fType.interfaces();
		assertEquals("1", 2, interfaces.size());
		Iterator<?> iterator = interfaces.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			Object next = iterator.next();
			assertTrue("2." + i++, next instanceof InterfaceType);
			InterfaceType ift = (InterfaceType) next;
			if (ift.name().equals("java.lang.Runnable"))
				found = true;
			if (ift.name().equals("java.lang.Comparable"))
				extra = true;
		}
		assertTrue("1", found);
		assertTrue("2", !extra);
	}
	/**
	 * Test JDI invokeMethod(ThreadReference, Method, Value[]).
	 */
	public void testJDIInvokeMethod() {
		// Make sure the entire VM is not suspended before we start a new thread
		// (otherwise this new thread will start suspended and we will never get the
		// ThreadStart event)
		fVM.resume();

		ThreadStartEvent event =
			(ThreadStartEvent) triggerAndWait(fVM
				.eventRequestManager()
				.createThreadStartRequest(),
				"ThreadStartEvent",
				false);
		ThreadReference thread = event.thread();
		Method inv1 =
			fType.concreteMethodByName(
				"invoke1",
				"(ILjava/lang/Object;)Ljava/lang/String;");
		List<IntegerValue> args = new ArrayList<>();
		args.add(fVM.mirrorOf(41));
		args.add(null);
		Exception oops = null;
		Value val = null;
		try {
			val = fType.invokeMethod(thread, inv1, args, 0);
		} catch (Exception exc) {
			oops = exc;
		}
		assertTrue("1", oops == null);
		assertEquals("2", val == null ? null : ((StringReference) val).value(), "41");
	}
	/**
	 * Test JDI invokeMethod - failure.
	 */
	public void testJDIInvokeMethodFail() {
		// Make sure the entire VM is not suspended before we start a new thread
		// (otherwise this new thread will start suspended and we will never get the
		// ThreadStart event)
		fVM.resume();

		ThreadStartEvent event =
			(ThreadStartEvent) triggerAndWait(fVM
				.eventRequestManager()
				.createThreadStartRequest(),
				"ThreadStartEvent",
				false);
		ThreadReference thread = event.thread();
		Method inv2 = fType.concreteMethodByName("invoke2", "()V");
		Exception good = null;
		Exception oops = null;
		try {
			fType.invokeMethod(thread, inv2, new ArrayList<Value>(), 0);
		} catch (InvocationException exc) {
			good = exc;
		} catch (Exception exc) {
			oops = exc;
		}
		assertTrue("1", oops == null);
		assertTrue("2", good != null);
	}
	/**
	 * Test JDI locationsOfLine(int).
	 */
	public void testJDILocationsOfLine() {
		int lineNumber = getLocation().lineNumber();
		List<?> locations = null;
		try {
			locations = fType.locationsOfLine(lineNumber);
		} catch (AbsentInformationException e) {
			assertTrue("1", false);
		}
		assertEquals("2", 1, locations.size());
	}
	/**
	 * Test JDI methodByName
	 */
	public void testJDIMethodByName() {
		assertTrue("1", fType.methodsByName("main").size() == 1);
	}
	/**
	 * Test JDI methodByNameAndSignature
	 */
	public void testJDIMethodByNameAndSignature() {
		assertTrue("1", fType.methodsByName("printAndSignal", "()V").size() == 1);
	}
	/**
	 * Test JDI methods().
	 */
	public void testJDIMethods() {
		boolean found = false;
		Iterator<?> it = fType.methods().iterator();
		while (it.hasNext()) {
			Method mth = (Method) it.next();
			if (mth.name().equals("printAndSignal"))
				found = true;
		}
		assertTrue("1", found);
	}
	/**
	 * Test JDI newInstance(ThreadReference, Method, Value[]).
	 */
	public void testJDINewInstance() {
		// Make sure the entire VM is not suspended before we start a new thread
		// (otherwise this new thread will start suspended and we will never get the
		// ThreadStart event)
		fVM.resume();

		ThreadStartEvent event =
			(ThreadStartEvent) triggerAndWait(fVM
				.eventRequestManager()
				.createThreadStartRequest(),
				"ThreadStartEvent",
				false);
		ThreadReference thread = event.thread();
		Method constructor =
			fType
			.methodsByName("<init>", "(ILjava/lang/Object;Ljava/lang/Object;)V")
			.get(0);
		ObjectReference result = null;
		ArrayList<Value> arguments = new ArrayList<>();
		arguments.add(fVM.mirrorOf(0));
		arguments.add(fVM.allThreads().get(0));
		arguments.add(null);

		try {
			result = fType.newInstance(thread, constructor, arguments, 0);
		} catch (IncompatibleThreadStateException e) {
			assertTrue("1", false);
		} catch (InvalidTypeException e) {
			assertTrue("2", false);
		} catch (ClassNotLoadedException e) {
			assertTrue("3", false);
		} catch (InvocationException e) {
			assertTrue("4", false);
		}
		assertTrue("5", result != null);
		assertTrue("6", result.referenceType().equals(fType));
		waitUntilReady();
	}
	/**
	 * Test JDI setValue(Field,Value) and JDWP 'Class - Set Fields Values'.
	 */
	public void testJDISetValue() {

		// Get static field "fInt"
		Field field = fType.fieldByName("fInt");
		assertTrue("1", field != null);
		assertTrue("2", field.isStatic());

		// Remember old value
		Value oldValue = fType.getValue(field);

		// Set to new value
		Value newValue = fVM.mirrorOf(1234);
		try {
			fType.setValue(field, newValue);
		} catch (ClassNotLoadedException e) {
			assertTrue("3.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("3.2", false);
		}

		// Ensure the value as been set
		assertEquals("4", fType.getValue(field), newValue);

		// Cleanup
		try {
			fType.setValue(field, oldValue);
		} catch (ClassNotLoadedException e) {
			assertTrue("5.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("5.2", false);
		}

		// Get static field "fString" to test if it can be set to null.
		field = fType.fieldByName("fString");
		assertTrue("6", field != null);
		assertTrue("7", field.isStatic());

		// Remember old value
		oldValue = fType.getValue(field);

		// Set to new value
		newValue = null;
		try {
			fType.setValue(field, newValue);
		} catch (ClassNotLoadedException e) {
			assertTrue("8.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("8.2", false);
		}

		// Ensure the value as been set
		assertEquals("9", fType.getValue(field), newValue);

		// Cleanup
		try {
			fType.setValue(field, oldValue);
		} catch (ClassNotLoadedException e) {
			assertTrue("10.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("10.2", false);
		}
	}
	/**
	 * Test JDI subclasses().
	 */
	public void testJDISubclasses() {
		List<?> subclasses = fType.subclasses();
		assertEquals("1", 1, subclasses.size());
		Iterator<?> iterator = subclasses.iterator();
		while (iterator.hasNext()) {
			ClassType sub = (ClassType) iterator.next();
			assertEquals("2 " + sub.name(), fType, sub.superclass());
		}
	}
	/**
	 * Test JDI superclass() and JDWP 'Class - Get superclass'.
	 */
	public void testJDISuperclass() {
		ClassType superclass = fType.superclass();
		assertEquals("1", "java.util.Date", superclass.name());
	}
}
