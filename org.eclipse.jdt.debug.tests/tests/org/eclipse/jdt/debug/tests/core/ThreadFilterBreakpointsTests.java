package org.eclipse.jdt.debug.tests.core;

import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests breakpoints with thread filters
 */
public class ThreadFilterBreakpointsTests extends AbstractDebugTest {

	public ThreadFilterBreakpointsTests(String name) {
		super(name);
	}

	public void testSimpleThreadFilterBreakpoint() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createLineBreakpoint(5, typeName);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			bp.setThreadFilter(thread);
			resumeToLineBreakpoint(thread, bp);
						
			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}


}
