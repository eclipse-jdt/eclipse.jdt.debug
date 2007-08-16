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
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests method breakpoints for 1.5 source code.
 */
public class MethodBreakpointTests15 extends AbstractDebugTest {
	
	public MethodBreakpointTests15(String name) {
		super(name);
	}

	public void testStaticTypeParameter() throws Exception {
		String typeName = "a.b.c.MethodBreakpoints";
		IJavaMethodBreakpoint mbp = createMethodBreakpoint(get15Project(), typeName, "staticTypeParameter", "(Ljava/util/List;)V", true, false);
		runToBreakpoint(typeName, mbp);		
	}
	
	public void testTypeParameter() throws Exception {
		String typeName = "a.b.c.MethodBreakpoints";
		IJavaMethodBreakpoint mbp = createMethodBreakpoint(get15Project(), typeName, "typeParameter", "(Ljava/lang/Object;)Ljava/lang/Object;", true, false);
		runToBreakpoint(typeName, mbp);		
	}
	
	public void testMethodTypeParameter() throws Exception {
		String typeName = "a.b.c.MethodBreakpoints";
		IJavaMethodBreakpoint mbp = createMethodBreakpoint(get15Project(), typeName, "methodTypeParameter", "(Ljava/lang/Object;)V", true, false);
		runToBreakpoint(typeName, mbp);		
	}	

	private void runToBreakpoint(String typeName, IJavaMethodBreakpoint mbp) throws Exception {
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(get15Project(), typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit entry breakpoint first", mbp,hit);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}	
		
}
