/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests strata.
 */
public class StratumTests extends AbstractDebugTest {
	
	public StratumTests(String name) {
		super(name);
	}

	/**
	 * Test available strata on a type.
	 * 
	 * @throws Exception
	 */
	public void testAvailableStrata() throws Exception {
		String typeName = "Breakpoints";
		createLineBreakpoint(81, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaClassType type = ((IJavaStackFrame)thread.getTopStackFrame()).getDeclaringType();
			String[] strata = type.getAvailableStrata();
			assertEquals("Wrong number of available strata", 1, strata.length);
			assertEquals("Wrong strata", "Java", strata[0]);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	/**
	 * Test default stratum on a type.
	 * 
	 * @throws Exception
	 */
	public void testDefaultStratum() throws Exception {
		String typeName = "Breakpoints";
		createLineBreakpoint(81, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaClassType type = ((IJavaStackFrame)thread.getTopStackFrame()).getDeclaringType();
			String stratum = type.getDefaultStratum();
			assertEquals("Wrong strata", "Java", stratum);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	

}
