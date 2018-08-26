/*******************************************************************************
 *  Copyright (c) 2000, 2007 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests breakpoints with thread filters
 */
public class ThreadFilterBreakpointsTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public ThreadFilterBreakpointsTests(String name) {
		super(name);
	}

	/**
	 * Tests that a simple thread filter is working for a specific line breakpoint
	 * @throws Exception
	 */
	public void testSimpleThreadFilterBreakpoint() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createLineBreakpoint(19, typeName);

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

	/**
	 * Tests multiple thread filters are working for a specific line breakpoint
	 * @throws Exception
	 */
	public void testMultiThreadFilterBreakpoint() throws Exception {
		String typeName = "MultiThreadedLoop";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(20, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp1);

			IJavaLineBreakpoint bp2 = createLineBreakpoint(43, typeName);
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

	/**
	 * Tests that a thread works for specific exception breakpoint
	 * @throws Exception
	 */
	public void testExceptionThreadFilterBreakpoint() throws Exception {
		String typeName = "MultiThreadedException";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(17, typeName);

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

	/**
	 * Tests that a thread filter works for a specific watchpoint
	 * @throws Exception
	 */
	public void testAccessWatchpointThreadFilterBreakpoint() throws Exception {
		String typeName = "MultiThreadedList";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(21, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp1);
			IJavaWatchpoint wp = createWatchpoint(typeName, "list", true, false);
			wp.setThreadFilter(thread);

			thread = resume(thread);
			assertTrue("Suspended thread should have been '1stThread'", thread.getName().equals("1stThread"));

			bp1.delete();
			wp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that a thread filter is working for a specific modification watchpoint
	 * @throws Exception
	 */
	public void testModificationWatchpointThreadFilterBreakpoint() throws Exception {
		String typeName = "MultiThreadedList";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(25, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp1);
			IJavaWatchpoint wp = createWatchpoint(typeName, "i", false, true);
			wp.setThreadFilter(thread);

			thread = resume(thread);
			assertTrue("Suspended thread should have been '1stThread'", thread.getName().equals("1stThread"));

			bp1.delete();
			wp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
