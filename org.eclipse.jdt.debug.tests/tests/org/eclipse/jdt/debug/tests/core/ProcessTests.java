package org.eclipse.jdt.debug.tests.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Tests IProcess.
 */
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class ProcessTests extends AbstractDebugTest {
	
	public ProcessTests(String name) {
		super(name);
	}

	public void testExitValueNormal() throws Exception {
		String typeName = "Breakpoints";
		ILineBreakpoint bp = createLineBreakpoint(32, typeName);
				
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
