package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Test that a SUSPEND_VM breakpoint suspends all threads
 */
public class SuspendVMBreakpointsTests extends AbstractDebugTest {

	public SuspendVMBreakpointsTests(String name) {
		super(name);
	}

	public void testSuspendVmBreakpoint() throws Exception {
		String typeName = "MultiThreadedLoop";
		IJavaLineBreakpoint bp = createLineBreakpoint(29, typeName);
		bp.setHitCount(10);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			
			IJavaDebugTarget debugTarget = (IJavaDebugTarget)thread.getDebugTarget();
			IThread[] threads = debugTarget.getThreads();
			for (int i = 0; i < threads.length; i++) {
				assertTrue("Thread wasn't suspended when a SUSPEND_VM breakpoint was hit", threads[i].isSuspended());
			}
			
			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
}
