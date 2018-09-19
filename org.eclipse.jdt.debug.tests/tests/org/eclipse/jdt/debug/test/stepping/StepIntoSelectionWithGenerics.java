/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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
package org.eclipse.jdt.debug.test.stepping;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.DebugElementEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.actions.StepIntoSelectionHandler;

/**
 * Test stepping into a given selection with Java source level 1.5+
 * @since 3.8
 */
public class StepIntoSelectionWithGenerics extends AbstractDebugTest {

	private String qtypename = "a.b.c.StepIntoSelectionWithGenerics";
	private String pname = "a.b.c";
	private String jname = "StepIntoSelectionWithGenerics.java";
	private String mname = "hello";

	/**
	 * Constructor
	 */
	public StepIntoSelectionWithGenerics() {
		super("StepIntoSelectionWithGenerics");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.tests.AbstractDebugTest#getProjectContext()
	 */
	@Override
	protected IJavaProject getProjectContext() {
		return get15Project();
	}

	/**
	 * Tests stepping into a method of a top-level class that is generified
	 * @throws Exception
	 */
	public void testStepIntoSelection1() throws Exception {
		createLineBreakpoint(33, qtypename);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(qtypename);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			ICompilationUnit cu = getCompilationUnit(get15Project(), "src", pname, jname);
			IJavaProject jp = cu.getJavaProject();
			NullProgressMonitor monitor = new NullProgressMonitor();
			IType type = jp.findType(qtypename, monitor);
			assertTrue("The top-level type"+qtypename+" must exist", type.exists());
			IMethod method = type.getMethod(mname, new String[0]);
			assertTrue("Could not find method "+mname+" in type "+qtypename, method.exists());

			StepIntoSelectionHandler handler = new StepIntoSelectionHandler(thread, (IJavaStackFrame)thread.getTopStackFrame(), method);
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
			handler.step();
			Object source = waiter.waitForEvent();
			assertEquals("Step did not complete", thread, source);
			thread = (IJavaThread)source;

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("Should be in StepIntoSelectionWithGenerics."+mname, mname, frame.getMethodName());
			assertEquals("Should be stopped on line", 29, frame.getLineNumber());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests stepping into a method of an inner class that is generified
	 *
	 * @throws Exception
	 */
	public void testStepIntoSelection2() throws Exception {
		createLineBreakpoint(34, qtypename);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(qtypename);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			ICompilationUnit cu = getCompilationUnit(get15Project(), "src", pname, jname);
			IJavaProject jp = cu.getJavaProject();
			NullProgressMonitor monitor = new NullProgressMonitor();
			IType type = jp.findType(qtypename, monitor);
			assertTrue("The top-level type"+qtypename+" must exist", type.exists());
			type = jp.findType(qtypename+".InnerClazz", monitor);
			assertNotNull("The iner type InnerClazz must not be null", type);
			IMethod method = type.getMethod(mname, new String[0]);
			assertTrue("Could not find method "+mname+" in type InnerClazz", method.exists());

			StepIntoSelectionHandler handler = new StepIntoSelectionHandler(thread, (IJavaStackFrame)thread.getTopStackFrame(), method);
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
			handler.step();
			Object source = waiter.waitForEvent();
			assertEquals("Step did not complete", thread, source);
			thread = (IJavaThread)source;

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("Should be in InnerClazz."+mname, mname, frame.getMethodName());
			assertEquals("Should be stopped on line", 24, frame.getLineNumber());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests stepping into a method of an inner-inner class that is generified
	 *
	 * @throws Exception
	 */
	public void testStepIntoSelection3() throws Exception {
		createLineBreakpoint(35, qtypename);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(qtypename);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			ICompilationUnit cu = getCompilationUnit(get15Project(), "src", pname, jname);
			IJavaProject jp = cu.getJavaProject();
			NullProgressMonitor monitor = new NullProgressMonitor();
			IType type = jp.findType(qtypename, monitor);
			assertTrue("The top-level type"+qtypename+" must exist", type.exists());
			type = jp.findType(qtypename+".InnerClazz.InnerClazz2", monitor);
			assertNotNull("The inner type InnerClazz2 must not be null", type);
			IMethod method = type.getMethod(mname, new String[0]);
			assertTrue("Could not find method "+mname+" in type InnerClazz2", method.exists());

			StepIntoSelectionHandler handler = new StepIntoSelectionHandler(thread, (IJavaStackFrame)thread.getTopStackFrame(), method);
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
			handler.step();
			Object source = waiter.waitForEvent();
			assertEquals("Step did not complete", thread, source);
			thread = (IJavaThread)source;

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("Should be in InnerClazz2."+mname, mname, frame.getMethodName());
			assertEquals("Should be stopped on line", 20, frame.getLineNumber());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
