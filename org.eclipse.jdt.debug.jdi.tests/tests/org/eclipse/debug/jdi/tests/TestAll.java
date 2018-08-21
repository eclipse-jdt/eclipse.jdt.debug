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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Run all tests
 */
public class TestAll {
	/**
	 * Returns all the test case classes (a Vector of Class) that are
	 * relevant for the given VM information.
	 * NB1: This doesn't include the VirtualMachineTest class.
	 * NB2: The last element must be the VMDeathEventTest class since
	 *      it shuts the VM down.
	 */
	protected static Vector<Class<?>> getAllTestCases(VMInformation info) {
		Vector<Class<?>> classes = new Vector<>();
		classes.addElement(AccessibleTest.class);
		classes.addElement(ArrayReferenceTest.class);
		classes.addElement(ArrayTypeTest.class);
		classes.addElement(BooleanValueTest.class);
		classes.addElement(BreakpointRequestTest.class);
		classes.addElement(ByteValueTest.class);
		classes.addElement(CharValueTest.class);
		classes.addElement(ClassLoaderReferenceTest.class);
		classes.addElement(ClassPrepareEventTest.class);
		classes.addElement(ClassPrepareRequestTest.class);
		classes.addElement(ClassTypeTest.class);
		classes.addElement(DoubleValueTest.class);
		classes.addElement(EventRequestManagerTest.class);
		classes.addElement(EventRequestTest.class);
		classes.addElement(EventTest.class);
		classes.addElement(ExceptionEventTest.class);
		classes.addElement(ExceptionRequestTest.class);
		classes.addElement(FieldTest.class);
		classes.addElement(FloatValueTest.class);
		classes.addElement(HotCodeReplacementTest.class);
		classes.addElement(IntegerValueTest.class);
		classes.addElement(InterfaceTypeTest.class);
		classes.addElement(LocalVariableTest.class);
		classes.addElement(LocatableTest.class);
		classes.addElement(LocationTest.class);
		classes.addElement(LongValueTest.class);
		classes.addElement(MethodTest.class);
		classes.addElement(MethodEntryRequestTest.class);
		classes.addElement(MethodExitRequestTest.class);
		classes.addElement(MirrorTest.class);

		if (info.fVM.canWatchFieldModification())
			classes.addElement(ModificationWatchpointEventTest.class);

		classes.addElement(ObjectReferenceTest.class);
		classes.addElement(PrimitiveValueTest.class);
		classes.addElement(ReferenceTypeTest.class);
		classes.addElement(ShortValueTest.class);
		classes.addElement(StackFrameTest.class);
		classes.addElement(StepEventTest.class);
		classes.addElement(StringReferenceTest.class);
		classes.addElement(ThreadDeathEventTest.class);
		classes.addElement(ThreadGroupReferenceTest.class);
		classes.addElement(ThreadReferenceTest.class);
		classes.addElement(ThreadStartEventTest.class);
		classes.addElement(TypeComponentTest.class);
		classes.addElement(TypeTest.class);
		classes.addElement(ValueTest.class);

		if (info.fVM.canWatchFieldAccess() && info.fVM.canWatchFieldModification()) {
			classes.addElement(WatchpointEventTest.class);
			classes.addElement(WatchpointRequestTest.class);
		}

		classes.addElement(VirtualMachineExitTest.class);
		classes.addElement(VMDisconnectEventTest.class);
		classes.addElement(VMDisposeTest.class);	// note that this test does not restore the state properly.
		return classes;
	}
	/**
	 * Run all tests with the given arguments.
	 * @see AbstractJDITest for details on the arguments.
	 * @param arguments
	 * @throws Throwable
	 */
	public static void main(String[] arguments) throws Throwable {
		// Create test result
		TextTestResult result= new TextTestResult();

		// Run the VirtualMachineTest
		AbstractJDITest test= run(result, VirtualMachineTest.class, arguments, null);

		// Was it possible to run the first test?
		if (test == null)
			return;

		// Get the VM info
		VMInformation info = test.getVMInfo();

		// Get all test cases
		Vector<Class<?>> classes= getAllTestCases(info);

		// Run the other tests
		Enumeration<Class<?>> enumeration= classes.elements();
		while (enumeration.hasMoreElements()) {
			Class<?> testClass= enumeration.nextElement();
			test= run(result, testClass, arguments, info);
			info= test.getVMInfo(); // In case the test has changed this info
		}

		// Shut down the VM
		test.shutDownTarget();

		// Show the result
		result.print();
	}
	/**
	 * Runs the given test with the given arguments.
	 * Returns the instance that was created.
	 * Returns null if there was a problem with the arguments.
	 * @see AbstractJDITest for details on the arguments.
	 */
	private static AbstractJDITest run(junit.framework.TestResult result, Class<?> testClass, String[] arguments, VMInformation info) throws Throwable {
		// Create test
		Class<?>[] argTypes= {};
		Constructor<?> construct = null;
		try {
			construct = testClass.getConstructor(argTypes);
		} catch (NoSuchMethodException e) {
		}
		AbstractJDITest test = null;
		try {
			test = (AbstractJDITest) construct.newInstance(new Object[]{});
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
		if (!AbstractJDITest.parseArgs(arguments))
			return null;
		test.setVMInfo(info);
		test.setInControl(false);

		// Run test
		System.out.println("\n" + new java.util.Date());
		System.out.println("Begin testing " + test.getName() + "...");
		long startTime= System.currentTimeMillis();
		test.suite().run(result);
		long endTime= System.currentTimeMillis();
		long runTime= endTime-startTime;
		System.out.println("\nTime: "+runTime/1000+"."+runTime%1000);
		System.out.println("Done testing " + test.getName() + ".");

		return test;
	}
}
