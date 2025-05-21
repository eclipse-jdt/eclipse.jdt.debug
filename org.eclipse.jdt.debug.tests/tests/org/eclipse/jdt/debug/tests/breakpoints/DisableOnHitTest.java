/*******************************************************************************
 *  Copyright (c) 2025 IBM Corporation and others.
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

import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests for disable on hit breakpoints.
 */

public class DisableOnHitTest extends AbstractDebugTest {

	/**
	 * Constructor
	 *
	 * @param name
	 *            the name of the test
	 */
	public DisableOnHitTest(String name) {
		super(name);
	}

	public void testBreakpointHitWith() throws Exception {
		String typeName = "TriggerPoint_01";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(19, typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(20, typeName);
		bp1.setDisableOnHit(true);
		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			thread.resume();
			boolean isBp1Disabled = bp1.isEnabled();
			assertFalse("Breakpoint should be disabled after hit", isBp1Disabled);
			bp1.delete();
			bp2.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBreakpointHitWithout() throws Exception {
		String typeName = "TriggerPoint_01";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(19, typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(20, typeName);
		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			thread.resume();
			boolean isBp1Disabled = bp1.isEnabled();
			assertTrue("Breakpoint should be not be disabled after hit", isBp1Disabled);
			bp1.delete();
			bp2.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}