package org.eclipse.jdt.debug.tests.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Tests event sets.
 */
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class EventSetTests extends AbstractDebugTest {
	
	public EventSetTests(String name) {
		super(name);
	}

	public void testDoubleBreakpoint() throws Exception {
		String typeName = "Breakpoints";
		List bps = new ArrayList();
		// add two breakpoints at the same location
		bps.add(createLineBreakpoint(77, typeName));
		bps.add(createLineBreakpoint(77, typeName));
		

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			while (!bps.isEmpty()) {
				DebugEvent[] set = getEventSet();
				assertTrue("Should be two events", set!= null && set.length == 2);
				for (int i = 0; i < set.length; i++) {
					assertTrue("should be a breakpoint event", set[i].getDetail() == DebugEvent.BREAKPOINT);
				}
				IBreakpoint[] hits = thread.getBreakpoints();
				assertTrue("should be two breakpoints", hits != null && hits.length == 2);
				for (int i = 0; i < hits.length; i++) {
					bps.remove(hits[i]);
				}
				assertTrue("breakpoint collection should now be empty", bps.isEmpty());
				
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
}
