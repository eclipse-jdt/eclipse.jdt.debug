package org.eclipse.jdt.debug.tests.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Tests deferred breakpoints.
 */
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class DeferredBreakpointTests extends AbstractDebugTest {
	
	public DeferredBreakpointTests(String name) {
		super(name);
	}

	public void testDeferredBreakpoints() throws Exception {
		String typeName = "Breakpoints";
		List bps = new ArrayList();
		// anonymous class
		bps.add(createLineBreakpoint(32, typeName));
		// blocks
		bps.add(createLineBreakpoint(91, typeName));
		// constructor
		bps.add(createLineBreakpoint(66, typeName));
		// else
		bps.add(createLineBreakpoint(77, typeName));
		//finally after catch
		bps.add(createLineBreakpoint(109, typeName));
		//finally after try
		bps.add(createLineBreakpoint(117, typeName));
		// for loop
		bps.add(createLineBreakpoint(82, typeName));
		// if
		bps.add(createLineBreakpoint(70, typeName));
		// initializer
		bps.add(createLineBreakpoint(6, typeName));
		// inner class
		bps.add(createLineBreakpoint(11, typeName));
		// return true
		bps.add(createLineBreakpoint(61, typeName));
		// instance method
		bps.add(createLineBreakpoint(96, typeName));
		// static method 
		bps.add(createLineBreakpoint(42, typeName));
		// case statement
		bps.add(createLineBreakpoint(122, typeName));
		// default statement
		bps.add(createLineBreakpoint(129, typeName));
		// synchronized blocks
		bps.add(createLineBreakpoint(135, typeName));
		// try
		bps.add(createLineBreakpoint(114, typeName));
		//catch
		bps.add(createLineBreakpoint(107, typeName));
		// while
		bps.add(createLineBreakpoint(86, typeName));
		
		
		IJavaThread thread= null;
		try {
			thread= launch(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			while (!bps.isEmpty()) {
				IBreakpoint hit = getBreakpoint(thread);
				assertNotNull("suspended, but not by breakpoint", hit);
				assertTrue("hit un-registered breakpoint", bps.contains(hit));
				assertTrue("suspended, but not by line breakpoint", hit instanceof ILineBreakpoint);
				ILineBreakpoint breakpoint= (ILineBreakpoint) hit;
				int lineNumber = breakpoint.getLineNumber();
				int stackLine = thread.getTopStackFrame().getLineNumber();
				assertTrue("line numbers of breakpoint and stack frame do not match", lineNumber == stackLine);
				bps.remove(breakpoint);
				breakpoint.delete();
				if (!bps.isEmpty()) {
					thread = resume(thread);
				}
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
}
