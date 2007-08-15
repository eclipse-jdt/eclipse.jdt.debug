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

import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

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
	
}
