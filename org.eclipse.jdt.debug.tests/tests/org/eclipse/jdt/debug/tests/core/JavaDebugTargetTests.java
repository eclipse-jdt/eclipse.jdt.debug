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
package org.eclipse.jdt.debug.tests.core;

import java.lang.reflect.Method;

import org.eclipse.jdt.debug.core.IJavaDebugTarget;
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
		createLineBreakpoint(52, typeName);

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
		createLineBreakpoint(52, typeName);

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
		createLineBreakpoint(52, typeName);

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
