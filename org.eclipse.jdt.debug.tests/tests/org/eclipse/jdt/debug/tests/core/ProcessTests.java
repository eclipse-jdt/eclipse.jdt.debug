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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests IProcess.
 */
public class ProcessTests extends AbstractDebugTest {
	
	public ProcessTests(String name) {
		super(name);
	}

	public void testExitValueNormal() throws Exception {
		String typeName = "Breakpoints";
		ILineBreakpoint bp = createLineBreakpoint(43, typeName);
				
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IProcess process = thread.getDebugTarget().getProcess();
			assertNotNull("Missing process", process);
			int exitValue = -1;
			try {
				exitValue = process.getExitValue();
			} catch (DebugException e) {
				exit(thread);
				exitValue = process.getExitValue();
				assertEquals("Exit value not normal", 0, exitValue);
				return;
			}
			assertTrue("Should not be able to get exit value - process not terminated", false);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
}
