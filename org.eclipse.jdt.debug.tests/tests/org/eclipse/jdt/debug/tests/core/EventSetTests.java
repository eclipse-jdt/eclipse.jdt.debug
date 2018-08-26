/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests event sets.
 */
public class EventSetTests extends AbstractDebugTest {

	public EventSetTests(String name) {
		super(name);
	}

	public void testDoubleBreakpoint() throws Exception {
		String typeName = "Breakpoints";
		List<IJavaLineBreakpoint> bps = new ArrayList<>();
		// add two breakpoints at the same location
		bps.add(createLineBreakpoint(91, typeName));
		bps.add(createLineBreakpoint(91, typeName));


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
