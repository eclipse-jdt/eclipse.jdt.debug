/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.performance;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * Tests performance of the console.
 */
public class PerfConsoleTests extends AbstractDebugPerformanceTest {
	
	public PerfConsoleTests(String name) {
		super(name);
	}

	public void testDefault10k() throws Exception {
		runFixedWidthTest(10000);		
	}
	
	public void testDefault100k() throws Exception {
		runFixedWidthTest(100000);		
	}
		
	protected void runFixedWidthTest(int lines) throws Exception {
	    String typeName = "Console80Chars";
	    ILaunchConfiguration configuration = getLaunchConfiguration(typeName);
	    ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
	    workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, Integer.toString(lines));
	    IJavaLineBreakpoint bp = createLineBreakpoint(18, typeName);
	    createLineBreakpoint(23, typeName);
	    IJavaThread thread = null;
	    try {
	        thread= launchAndSuspend(workingCopy);
	        startMeasuring();
	        thread = resume(thread, 360000);
			stopMeasuring();
			commitMeasurements();
			assertPerformance();	        
	    } finally {
	        terminateAndRemove(thread);
	        removeAllBreakpoints();
	    }
	}
}