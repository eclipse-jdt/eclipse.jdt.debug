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

import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests hit count breakpoints
 */
public class HitCountBreakpointsTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public HitCountBreakpointsTests(String name) {
		super(name);
	}

	/**
	 * Tests resetting the hit count of a line breakpoint
	 * @throws Exception
	 */
	public void testResetHitCountBreakpoint() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createLineBreakpoint(19, typeName);
		bp.setHitCount(3);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable var = findVariable(frame, "i");
			assertNotNull("Could not find variable 'i'", var);

			IJavaPrimitiveValue value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull("variable 'i' has no value", value);
			int iValue = value.getIntValue();
			assertTrue("value of 'i' should be '2', but was " + iValue, iValue == 2);

			bp.setHitCount(2);
			IJavaThread thread2 = resumeToLineBreakpoint(thread, bp);
			assertTrue("second suspended thread not the same as first", thread == thread2);

			frame = (IJavaStackFrame)thread2.getTopStackFrame();
			var = findVariable(frame, "i");
			value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull("variable 'i' has no value", value);
			iValue = value.getIntValue();
			assertTrue("value of 'i' should be '4', but was " + iValue, iValue == 4);

			resumeAndExit(thread2);

			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests an expired hit count breakpoint, i.e. expired meaning that the count is no longer
	 * effectual
	 * @throws Exception
	 */
	public void testExpiredHitCountBreakpoint() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createLineBreakpoint(19, typeName);
		bp.setHitCount(3);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable var = findVariable(frame, "i");
			assertNotNull("Could not find variable 'i'", var);

			IJavaPrimitiveValue value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull("variable 'i' has no value", value);
			int iValue = value.getIntValue();
			assertTrue("value of 'i' should be '2', but was " + iValue, iValue == 2);

			resumeAndExit(thread);

			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
