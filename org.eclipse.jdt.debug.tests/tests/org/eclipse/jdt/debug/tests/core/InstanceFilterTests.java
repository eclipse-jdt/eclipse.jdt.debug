/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests instance filters on breakpoints.
 *
 * These tests only "run" on VMs that support instance breakpoints,
 * such as JDK 1.4.1.
 */
public class InstanceFilterTests extends AbstractDebugTest {

	public InstanceFilterTests(String name) {
		super(name);
	}

	/**
	 * Instance filter on a line breakpoint
	 *
	 * @throws Exception
	 */
	public void testLineBreakpoint() throws Exception {
		String typeName = "InstanceFilterObject";
		// main
		IJavaLineBreakpoint mainBreakpoint = createLineBreakpoint(42, typeName);
		// simpleMethod
		IJavaLineBreakpoint simpleMethod = createLineBreakpoint(22, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("hit wrong breakpoint", mainBreakpoint, hit);

			// can only do test if the VM supports instance filters
			if (supportsInstanceBreakpoints(thread)) {
				// restrict breakpoint in simpleMethod to object 1
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertNotNull(frame);
				IJavaVariable var1 = findVariable(frame, "object1");
				IJavaVariable var2 = findVariable(frame, "object2");
				assertNotNull(var1);
				assertNotNull(var2);
				IJavaObject object1 = (IJavaObject)var1.getValue();
				IJavaObject object2 = (IJavaObject)var2.getValue();
				assertNotNull(object1);
				assertNotNull(object2);

				simpleMethod.addInstanceFilter(object1);

				// resume the thread
				thread= resume(thread);
				IBreakpoint[] breakpoints = thread.getBreakpoints();
				assertEquals("should be a breakpoint", 1, breakpoints.length);
				assertEquals("should be in simpleMethod", simpleMethod, breakpoints[0]);

				// receiver should be object1
				frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertNotNull(frame);
				IJavaObject receiver = frame.getThis();
				assertEquals("should be in object1 context", object1, receiver);
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Instance filter on a method entry breakpoint
	 *
	 * @throws Exception
	 */
	public void testMethodEntryBreakpoint() throws Exception {
		String typeName = "InstanceFilterObject";
		// main
		IJavaLineBreakpoint mainBreakpoint = createLineBreakpoint(42, typeName);
		// simpleMethod
		IJavaMethodBreakpoint simpleMethod = createMethodBreakpoint(typeName, "simpleMethod", "()V", true, false);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("hit wrong breakpoint", mainBreakpoint, hit);

			// can only do test if the VM supports instance filters
			if (supportsInstanceBreakpoints(thread)) {
				// restrict breakpoint in simpleMethod to object 1
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertNotNull(frame);
				IJavaVariable var1 = findVariable(frame, "object1");
				IJavaVariable var2 = findVariable(frame, "object2");
				assertNotNull(var1);
				assertNotNull(var2);
				IJavaObject object1 = (IJavaObject)var1.getValue();
				IJavaObject object2 = (IJavaObject)var2.getValue();
				assertNotNull(object1);
				assertNotNull(object2);

				simpleMethod.addInstanceFilter(object1);

				// resume the thread
				thread= resume(thread);
				IBreakpoint[] breakpoints = thread.getBreakpoints();
				assertEquals("should be a breakpoint", 1, breakpoints.length);
				assertEquals("should be in simpleMethod", simpleMethod, breakpoints[0]);

				// receiver should be object1
				frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertNotNull(frame);
				IJavaObject receiver = frame.getThis();
				assertEquals("should be in object1 context", object1, receiver);
				// method should not have executed yet
				IJavaFieldVariable boolVar = receiver.getField("executedSimpleMethod",false);
				assertNotNull(boolVar);
				IValue value = boolVar.getValue();
				assertEquals("method should not have executed", ((IJavaDebugTarget)frame.getDebugTarget()).newValue(false), value);
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Instance filter on a method exit breakpoint
	 *
	 * @throws Exception
	 */
	public void testMethodExitBreakpoint() throws Exception {
		String typeName = "InstanceFilterObject";
		// main
		IJavaLineBreakpoint mainBreakpoint = createLineBreakpoint(42, typeName);
		// simpleMethod
		IJavaMethodBreakpoint simpleMethod = createMethodBreakpoint(typeName, "simpleMethod", "()V", false, true);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("hit wrong breakpoint", mainBreakpoint, hit);

			// can only do test if the VM supports instance filters
			if (supportsInstanceBreakpoints(thread)) {
				// restrict breakpoint in simpleMethod to object 1
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertNotNull(frame);
				IJavaVariable var1 = findVariable(frame, "object1");
				IJavaVariable var2 = findVariable(frame, "object2");
				assertNotNull(var1);
				assertNotNull(var2);
				IJavaObject object1 = (IJavaObject)var1.getValue();
				IJavaObject object2 = (IJavaObject)var2.getValue();
				assertNotNull(object1);
				assertNotNull(object2);

				simpleMethod.addInstanceFilter(object1);

				// resume the thread
				thread= resume(thread);
				IBreakpoint[] breakpoints = thread.getBreakpoints();
				assertEquals("should be a breakpoint", 1, breakpoints.length);
				assertEquals("should be in simpleMethod", simpleMethod, breakpoints[0]);

				// receiver should be object1
				frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertNotNull(frame);
				IJavaObject receiver = frame.getThis();
				assertEquals("should be in object1 context", object1, receiver);
				// method should have executed
				IJavaFieldVariable boolVar = receiver.getField("executedSimpleMethod",false);
				assertNotNull(boolVar);
				IValue value = boolVar.getValue();
				assertEquals("method should have executed", ((IJavaDebugTarget)frame.getDebugTarget()).newValue(true), value);
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Instance filter on an exception breakpoint
	 *
	 * @throws Exception
	 */
	public void testExceptionBreakpoint() throws Exception {
		String typeName = "InstanceFilterObject";
		// main
		IJavaLineBreakpoint mainBreakpoint = createLineBreakpoint(42, typeName);
		// exception breakpoint
		IJavaExceptionBreakpoint npe = createExceptionBreakpoint("java.lang.NullPointerException", true, true);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("hit wrong breakpoint", mainBreakpoint, hit);

			// can only do test if the VM supports instance filters
			if (supportsInstanceBreakpoints(thread)) {
				// restrict breakpoint in simpleMethod to object 1
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertNotNull(frame);
				IJavaVariable var1 = findVariable(frame, "object1");
				IJavaVariable var2 = findVariable(frame, "object2");
				assertNotNull(var1);
				assertNotNull(var2);
				IJavaObject object1 = (IJavaObject)var1.getValue();
				IJavaObject object2 = (IJavaObject)var2.getValue();
				assertNotNull(object1);
				assertNotNull(object2);

				npe.addInstanceFilter(object1);

				// resume the thread
				thread= resume(thread);
				IBreakpoint[] breakpoints = thread.getBreakpoints();
				assertEquals("should be a breakpoint", 1, breakpoints.length);
				assertEquals("should be in throwNPE", npe, breakpoints[0]);

				// receiver should be object1
				frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertNotNull(frame);
				IJavaObject receiver = frame.getThis();
				assertEquals("should be in object1 context", object1, receiver);
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Instance filter on an access watchpoint
	 *
	 * @throws Exception
	 */
	public void testAccessWatchpoint() throws Exception {
		String typeName = "InstanceFilterObject";
		// main
		IJavaLineBreakpoint mainBreakpoint = createLineBreakpoint(42, typeName);
		// exception breakpoint
		IJavaWatchpoint watchpoint = createWatchpoint(typeName, "field", true, false);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("hit wrong breakpoint", mainBreakpoint, hit);

			// can only do test if the VM supports instance filters
			if (supportsInstanceBreakpoints(thread)) {
				// restrict breakpoint in simpleMethod to object 1
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertNotNull(frame);
				IJavaVariable var1 = findVariable(frame, "object1");
				IJavaVariable var2 = findVariable(frame, "object2");
				assertNotNull(var1);
				assertNotNull(var2);
				IJavaObject object1 = (IJavaObject)var1.getValue();
				IJavaObject object2 = (IJavaObject)var2.getValue();
				assertNotNull(object1);
				assertNotNull(object2);

				watchpoint.addInstanceFilter(object1);

				// resume the thread
				thread= resume(thread);
				IBreakpoint[] breakpoints = thread.getBreakpoints();
				assertEquals("should be a breakpoint", 1, breakpoints.length);
				assertEquals("should be in access method", watchpoint, breakpoints[0]);

				// in "accessField"
				frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertNotNull(frame);
				assertEquals("should be in access method", "accessField", frame.getMethodName());

				// receiver should be object1
				IJavaObject receiver = frame.getThis();
				assertEquals("should be in object1 context", object1, receiver);
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Instance filter on an modification watchpoint
	 *
	 * @throws Exception
	 */
	public void testModificationWatchpoint() throws Exception {
		String typeName = "InstanceFilterObject";
		// main
		IJavaLineBreakpoint mainBreakpoint = createLineBreakpoint(42, typeName);
		// exception breakpoint
		IJavaWatchpoint watchpoint = createWatchpoint(typeName, "field", false, true);
		// disable to avoid variable initializer
		watchpoint.setEnabled(false);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("hit wrong breakpoint", mainBreakpoint, hit);

			// can only do test if the VM supports instance filters
			if (supportsInstanceBreakpoints(thread)) {
				// restrict breakpoint in simpleMethod to object 1
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertNotNull(frame);
				IJavaVariable var1 = findVariable(frame, "object1");
				IJavaVariable var2 = findVariable(frame, "object2");
				assertNotNull(var1);
				assertNotNull(var2);
				IJavaObject object1 = (IJavaObject)var1.getValue();
				IJavaObject object2 = (IJavaObject)var2.getValue();
				assertNotNull(object1);
				assertNotNull(object2);

				watchpoint.addInstanceFilter(object1);

				// enable watchpoint
				watchpoint.setEnabled(true);

				// resume the thread
				thread= resume(thread);
				IBreakpoint[] breakpoints = thread.getBreakpoints();
				assertEquals("should be a breakpoint", 1, breakpoints.length);
				assertEquals("should be in modification method", watchpoint, breakpoints[0]);

				// in "modifyField"
				frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertNotNull(frame);
				assertEquals("should be in modify method", "modifyField", frame.getMethodName());

				// receiver should be object1
				IJavaObject receiver = frame.getThis();
				assertEquals("should be in object1 context", object1, receiver);
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Returns whether the associated target supports instance breakpoints
	 *
	 * @param thread
	 * @return boolean
	 */
	private boolean supportsInstanceBreakpoints(IJavaThread thread) {
		IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
		return target.supportsInstanceBreakpoints();
	}
}
