/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.test.stepping;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.testplugin.DebugElementEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

import com.sun.jdi.InvalidTypeException;

/**
 * Tests forcing return from method.
 * 
 * @since 3.3
 */
public class ForceReturnTests extends AbstractDebugTest {

	/**
	 * Creates test case.
	 * 
	 * @param name test name
	 */
	public ForceReturnTests(String name) {
		super(name);
	}
	
	/**
	 * Tests forcing the return of an integer
	 * 
	 * @throws Exception
	 */
	public void testForceIntReturn() throws Exception {
		String typeName = "ForceReturnTests";
		createLineBreakpoint(22, typeName);
		ILineBreakpoint bp = createLineBreakpoint(31, typeName);
		
		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			IJavaDebugTarget target = (IJavaDebugTarget) stackFrame.getDebugTarget();
			if (target.supportsForceReturn()) {
				assertTrue("Force return should be enabled", thread.canForceReturn());
				DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
				thread.forceReturn(target.newValue(42));
				Object source = waiter.waitForEvent();
				assertTrue("Suspend should be from thread", source instanceof IJavaThread);
				thread = (IJavaThread) source;
				stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
				IJavaVariable var = stackFrame.findVariable("x");
				assertNotNull("Missing variable 'x'", var);
				assertEquals("Return value incorrect", target.newValue(42), var.getValue());
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	/**
	 * Tests forcing the return of a string
	 * 
	 * @throws Exception
	 */
	public void testForceStringReturn() throws Exception {
		String typeName = "ForceReturnTests";
		createLineBreakpoint(24, typeName);
		ILineBreakpoint bp = createLineBreakpoint(36, typeName);
		
		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			IJavaDebugTarget target = (IJavaDebugTarget) stackFrame.getDebugTarget();
			if (target.supportsForceReturn()) {
				assertTrue("Force return should be enabled", thread.canForceReturn());
				DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
				IJavaValue stringValue = target.newValue("forty two");
				thread.forceReturn(stringValue);
				Object source = waiter.waitForEvent();
				assertTrue("Suspend should be from thread", source instanceof IJavaThread);
				thread = (IJavaThread) source;
				stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
				IJavaVariable var = stackFrame.findVariable("s");
				assertNotNull("Missing variable 's'", var);
				assertEquals("Return value incorrect", stringValue, var.getValue());
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	

	/**
	 * Tests forcing the return of an object
	 * 
	 * @throws Exception
	 */
	public void testForceObjectReturn() throws Exception {
		String typeName = "ForceReturnTests";
		createLineBreakpoint(26, typeName);
		ILineBreakpoint bp = createLineBreakpoint(43, typeName);
		
		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			IJavaDebugTarget target = (IJavaDebugTarget) stackFrame.getDebugTarget();
			if (target.supportsForceReturn()) {
				assertTrue("Force return should be enabled", thread.canForceReturn());
				DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
				IJavaValue objectValue = target.newValue("a string");
				thread.forceReturn(objectValue);
				Object source = waiter.waitForEvent();
				assertTrue("Suspend should be from thread", source instanceof IJavaThread);
				thread = (IJavaThread) source;
				stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
				IJavaVariable var = stackFrame.findVariable("v");
				assertNotNull("Missing variable 'v'", var);
				assertEquals("Return value incorrect", objectValue, var.getValue());
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	/**
	 * Tests that an incompatible type causes an exception
	 * 
	 * @throws Exception
	 */
	public void testIncompatibleReturnType() throws Exception {
		String typeName = "ForceReturnTests";
		createLineBreakpoint(26, typeName);
		ILineBreakpoint bp = createLineBreakpoint(43, typeName);
		
		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			IJavaDebugTarget target = (IJavaDebugTarget) stackFrame.getDebugTarget();
			if (target.supportsForceReturn()) {
				assertTrue("Force return should be enabled", thread.canForceReturn());
				IJavaValue objectValue = target.newValue(42);
				try {
					thread.forceReturn(objectValue);
				} catch (DebugException e) {
					assertTrue("Should be invalid type exception", e.getStatus().getException() instanceof InvalidTypeException);
					return;
				}
				assertTrue("Should have caused incompatible return type exception", false);
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	
}
