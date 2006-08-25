/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointSetOrganizer;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.ui.IWorkingSet;

/**
 * Tests adding breakpoints and automatic addition to working sets.
 * 
 * @since 3.2
 */
public class BreakpointWorkingSetTests extends AbstractBreakpointWorkingSetTest {

	public BreakpointWorkingSetTests(String name) {
		super(name);
	}

	public void testAddToDefaultWorkingSet() throws Exception {
		String name = "TEST DEFAULT";
		IWorkingSet set = createSet(name);
		try {
			BreakpointSetOrganizer.setDefaultWorkingSet(set);
			IJavaLineBreakpoint breakpoint = createLineBreakpoint(52, "Breakpoints");
			IAdaptable[] elements = set.getElements();
			assertEquals("Wrong number of breakpoints", 1, elements.length);
			assertEquals("Wrong breakpoint", elements[0], breakpoint);
		} finally {
			removeAllBreakpoints();
			getWorkingSetManager().removeWorkingSet(set);
		}
	}
	
	public void testNoDefaultWorkingSet() throws Exception {
		String name = "TEST DEFAULT";
		IWorkingSet set = createSet(name);
		try {
			BreakpointSetOrganizer.setDefaultWorkingSet(null);
			createLineBreakpoint(52, "Breakpoints");
			IAdaptable[] elements = set.getElements();
			assertEquals("Wrong number of breakpoints", 0, elements.length);
		} finally {
			removeAllBreakpoints();
			getWorkingSetManager().removeWorkingSet(set);
		}
	}	
}
