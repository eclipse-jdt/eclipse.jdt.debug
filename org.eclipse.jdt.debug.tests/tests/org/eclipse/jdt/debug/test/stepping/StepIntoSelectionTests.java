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
 *     Ivan Popov - [Bug 193488] org.eclipse.jdt.debug.test.stepping.StepIntoSelectionTests
 *     					depend on VM behavior
 *******************************************************************************/
package org.eclipse.jdt.debug.test.stepping;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.DebugElementEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.actions.StepIntoSelectionHandler;

/**
 * Tests 'step into selection'
 */
public class StepIntoSelectionTests extends AbstractDebugTest {

	/**
	 * Constructor
	 */
	public StepIntoSelectionTests(String name) {
		super(name);
	}

	/**
	 * Step into 'new StepIntoSelectionClass()'
	 *
	 * @throws Exception
	 */
	public void testStepIntoSourceConstructor() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.StepIntoSelectionClass";
		createLineBreakpoint(24, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "StepIntoSelectionClass.java");
			IType type = cu.getType("StepIntoSelectionClass");
			IMethod method = type.getMethod("StepIntoSelectionClass", new String[0]);
			assertTrue("Could not find constructor", method.exists());

			StepIntoSelectionHandler handler = new StepIntoSelectionHandler(thread, (IJavaStackFrame)thread.getTopStackFrame(), method);
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
			handler.step();
			Object source = waiter.waitForEvent();
			assertEquals("Step did not complete", thread, source);
			thread = (IJavaThread)source;

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("Should be in constructor", "<init>", frame.getMethodName());


		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Step into 'step()'
	 *
	 * @throws Exception
	 */
	public void testStepIntoSourceMethod() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.StepIntoSelectionClass";
		createLineBreakpoint(26, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "StepIntoSelectionClass.java");
			IType type = cu.getType("StepIntoSelectionClass");
			IMethod method = type.getMethod("step", new String[0]);
			assertTrue("Could not find method 'step'", method.exists());

			StepIntoSelectionHandler handler = new StepIntoSelectionHandler(thread, (IJavaStackFrame)thread.getTopStackFrame(), method);
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
			handler.step();
			Object source = waiter.waitForEvent();
			assertEquals("Step did not complete", thread, source);
			thread = (IJavaThread)source;

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("Should be in method 'step'", "step", frame.getMethodName());


		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Step into 'method1(int[], String[])'
	 *
	 * @throws Exception
	 */
	public void testStepIntoSourceMethodWithParameters() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.StepIntoSelectionClass";
		createLineBreakpoint(39, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "StepIntoSelectionClass.java");
			IType type = cu.getType("StepIntoSelectionClass");
			IMethod method = type.getMethod("method1", new String[] {"[I", "[QString;"});
			assertTrue("Could not find method 'method1'", method.exists());

			StepIntoSelectionHandler handler = new StepIntoSelectionHandler(thread, (IJavaStackFrame)thread.getTopStackFrame(), method);
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
			handler.step();
			Object source = waiter.waitForEvent();
			assertEquals("Step did not complete", thread, source);
			thread = (IJavaThread)source;

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("Should be in method 'step'", "method1", frame.getMethodName());


		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Step into 'Vector.addElement(Object)'
	 *
	 * @throws Exception
	 */
	public void testStepIntoBinaryMethod() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.StepIntoSelectionClass";
		createLineBreakpoint(37, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IType type = get14Project().findType("java.util.Vector");
			IMethod method = type.getMethod("addElement", new String[]{"Ljava.lang.Object;"});
			//for 1.5 compliance, addElement has a type 'E' not an object as the param type
			if(!method.exists()) {
				method = type.getMethod("addElement", new String[] {"TE;"});
			}
			assertTrue("Could not find method 'addElement'", method.exists());

			StepIntoSelectionHandler handler = new StepIntoSelectionHandler(thread, (IJavaStackFrame)thread.getTopStackFrame(), method);
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
			handler.step();
			Object source = waiter.waitForEvent();
			assertEquals("Step did not complete", thread, source);
			thread = (IJavaThread)source;

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("Should be in method 'addElement'", "addElement", frame.getMethodName());


		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Step into 'new Integer(i)'
	 *
	 * @throws Exception
	 */
	public void testStepIntoBinaryConstructor() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.StepIntoSelectionClass";
		createLineBreakpoint(37, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IType type = get14Project().findType("java.lang.Integer");
			IMethod method = type.getMethod("Integer", new String[]{"I"});
			assertTrue("Could not find method constructor", method.exists());

			StepIntoSelectionHandler handler = new StepIntoSelectionHandler(thread, (IJavaStackFrame)thread.getTopStackFrame(), method);
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
			handler.step();
			Object source = waiter.waitForEvent();
			assertEquals("Step did not complete", thread, source);
			thread = (IJavaThread)source;

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("Should be in constructor", "<init>", frame.getMethodName());


		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
