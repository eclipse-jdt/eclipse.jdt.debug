package org.eclipse.jdt.debug.tests.core;

import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
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

	public void testMultiThreadFilterBreakpoint() throws Exception {
		String typeName = "MultiThreadedLoop";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(6, typeName);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp1);
			
			IJavaLineBreakpoint bp2 = createLineBreakpoint(29, typeName);
			bp2.setThreadFilter(thread);
			
			thread = resumeToLineBreakpoint(thread, bp2);
			assertTrue("Suspended thread should have been '1stThread'", thread.getName().equals("1stThread"));
						
			bp1.delete();
			bp2.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}

	public void testExceptionThreadFilterBreakpoint() throws Exception {
		String typeName = "MultiThreadedException";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(11, typeName);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp1);
			IJavaExceptionBreakpoint ex1 = createExceptionBreakpoint("java.lang.NullPointerException", false, true);
			ex1.setThreadFilter(thread);
			
			thread = resume(thread);
			assertTrue("Suspended thread should have been '1stThread'", thread.getName().equals("1stThread"));
			
			bp1.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
			
}
