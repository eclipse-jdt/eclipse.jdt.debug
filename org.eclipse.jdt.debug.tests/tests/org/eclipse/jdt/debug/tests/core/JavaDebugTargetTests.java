/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

import static org.junit.Assert.assertNotEquals;

import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

/**
 * Tests IJavaDebugTarget API
 *
 * @since 3.4
 */
public class JavaDebugTargetTests extends AbstractDebugTest {

	public JavaDebugTargetTests(String name) {
		super(name);
	}

	public void testGetVMName() throws Exception {
		String typeName = "Breakpoints";
		createLineBreakpoint(55, typeName);

		IJavaThread thread= null;
		try {
			// do not register launch - see bug 130911
			thread= launchToBreakpoint(typeName, false);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaDebugTarget target = (IJavaDebugTarget) thread.getDebugTarget();
			String name = target.getVMName();
			assertNotNull("Missing VM name", name);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testGetVersion() throws Exception {
		String typeName = "Breakpoints";
		createLineBreakpoint(55, typeName);

		IJavaThread thread= null;
		try {
			// do not register launch - see bug 130911
			thread= launchToBreakpoint(typeName, false);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaDebugTarget target = (IJavaDebugTarget) thread.getDebugTarget();
			String version = target.getVersion();
			assertNotNull("Missing version property", version);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testIsAvailable() throws Exception {
		String typeName = "Breakpoints";
		createLineBreakpoint(55, typeName);

		IJavaThread thread = null;
		try {
			// do not register launch - see bug 130911
			thread = launchToBreakpoint(typeName, false);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			JDIDebugTarget target = (JDIDebugTarget) thread.getDebugTarget();
			assertTrue(target.isAvailable());
			JDIDebugTargetProxy proxy = new JDIDebugTargetProxy(target);
			assertTrue(proxy.isAvailable());
			assertFalse(proxy.isDisconnecting());
			assertFalse(proxy.isTerminating());

			proxy.setDisconnecting(true);
			assertFalse(proxy.isAvailable());
			assertTrue(proxy.isDisconnecting());
			assertFalse(proxy.isTerminating());

			proxy.setDisconnecting(false);
			assertTrue(proxy.isAvailable());
			assertFalse(proxy.isDisconnecting());
			assertFalse(proxy.isTerminating());

			proxy.setTerminating(true);
			assertFalse(proxy.isAvailable());
			assertFalse(proxy.isDisconnecting());
			assertTrue(proxy.isTerminating());

			proxy.setTerminating(false);
			assertTrue(proxy.isAvailable());
			assertFalse(proxy.isDisconnecting());
			assertFalse(proxy.isTerminating());

			terminateAndRemove(thread);
			assertFalse(proxy.isAvailable());
			assertFalse(proxy.isDisconnecting());
			assertFalse(proxy.isTerminating());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that debug target ignores breakpoints from unrelated projects, see bugs 5188 and 508524
	 */
	public void testSupportsResource() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass";
		IJavaProject project1 = get14Project();
		IJavaProject project2 = get15Project();
		IType type1 = project1.findType(typeName);
		IType type2 = project2.findType(typeName);
		assertNotEquals(type1, type2);

		// Types FQNs are same
		assertEquals(type1.getFullyQualifiedName(), type2.getFullyQualifiedName());

		// Paths are same, except the project part
		assertEquals(type1.getResource().getFullPath().removeFirstSegments(1), type2.getResource().getFullPath().removeFirstSegments(1));

		final int lineNumber = 24;
		IJavaLineBreakpoint bp1 = createLineBreakpoint(type1, lineNumber);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(type2, lineNumber);
		assertNotEquals(bp1, bp2);

		IJavaThread thread = null;
		try {
			// Launch the first project config: the breakpoint from second one shouldn't be supported
			thread = launchToBreakpoint(project1, typeName, typeName, true);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			JDIDebugTarget target = (JDIDebugTarget) thread.getDebugTarget();
			assertTrue(target.isAvailable());
			List<IBreakpoint> breakpoints = getUserBreakpoints(target);
			assertEquals(1, breakpoints.size());
			assertEquals(bp1, getUserBreakpoints(target).get(0));
			assertTrue(target.supportsResource(() -> typeName, type1.getResource()));
			assertFalse(target.supportsResource(() -> typeName, type2.getResource()));
			terminateAndRemove(thread);
			// Line above *deletes all breakpoints!*

			bp1 = createLineBreakpoint(type1, lineNumber);
			bp2 = createLineBreakpoint(type2, lineNumber);
			assertNotEquals(bp1, bp2);

			// Launch the second project config: the breakpoint from first one shouldn't be supported
			thread = launchToBreakpoint(project2, typeName, typeName + CLONE_SUFFIX, true);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			target = (JDIDebugTarget) thread.getDebugTarget();
			assertTrue(target.isAvailable());
			assertEquals(1, getUserBreakpoints(target).size());
			assertEquals(bp2, getUserBreakpoints(target).get(0));
			assertFalse(target.supportsResource(() -> typeName, type1.getResource()));
			assertTrue(target.supportsResource(() -> typeName, type2.getResource()));
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	private List<IBreakpoint> getUserBreakpoints(JDIDebugTarget target) {
		List<IBreakpoint> breakpoints = target.getBreakpoints();
		return breakpoints;
	}

	static private class JDIDebugTargetProxy {

		private JDIDebugTarget target;

		public JDIDebugTargetProxy(JDIDebugTarget target) {
			this.target = target;
		}

		public boolean isAvailable() {
			return target.isAvailable();
		}

		public boolean isTerminating() throws Exception {
			return callBooleanGetMethod("isTerminating");
		}

		public boolean isDisconnecting() throws Exception {
			return callBooleanGetMethod("isDisconnecting");
		}

		public void setTerminating(boolean terminating) throws Exception {
			callBooleanSetMethod("setTerminating", terminating);
		}

		public void setDisconnecting(boolean disconnecting) throws Exception {
			callBooleanSetMethod("setDisconnecting", disconnecting);
		}

		private boolean callBooleanGetMethod(String name) throws Exception {
			Method method = JDIDebugTarget.class.getDeclaredMethod(name);
			method.setAccessible(true);
			return (Boolean) method.invoke(target);
		}

		private void callBooleanSetMethod(String name, boolean arg) throws Exception {
			Method method = JDIDebugTarget.class.getDeclaredMethod(name, boolean.class);
			method.setAccessible(true);
			method.invoke(target, Boolean.valueOf(arg));
		}
	}

}
