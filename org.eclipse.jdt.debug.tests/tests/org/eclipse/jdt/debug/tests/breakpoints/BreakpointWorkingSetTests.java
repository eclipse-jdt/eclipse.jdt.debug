/*******************************************************************************
 *  Copyright (c) 2005, 2013 IBM Corporation and others.
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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointOrganizer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointSetOrganizer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Tests adding breakpoints and automatic addition to working sets.
 *
 * @since 3.2
 */
public class BreakpointWorkingSetTests extends AbstractBreakpointWorkingSetTest {

	/**
	 * Constructor
	 * @param name
	 */
	public BreakpointWorkingSetTests(String name) {
		super(name);
	}

	/**
	 * Tests adding a breakpoint to the default working set
	 * @throws Exception
	 */
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

	/**
	 * Tests adding a breakpoint with no default working set
	 * @throws Exception
	 */
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

	/**
	 * Tests that we can have N working set shown in the view and add additional breakpoints
	 * to the default ones without causing exceptions
	 *
	 * In this test we have to open / show the view to cause the insert update to occur
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=380614
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testInsertIntoDefaultSet() throws Exception {
		IWorkingSet set1 = createSet("One");
		try {
			BreakpointSetOrganizer.setDefaultWorkingSet(set1);
			IDebugView bpview = openDebugView("org.eclipse.debug.ui.BreakpointView");
			assertNotNull("we should have opened the breakpoints view", bpview);
			final BreakpointsView view = (BreakpointsView) bpview;
			final IBreakpointOrganizer o = getOrganizer("org.eclipse.debug.ui.breakpointWorkingSetOrganizer");
			assertNotNull("we should have found the working set breakpoint organizer", o);
			//update the view organizers on the UI thread because it spawns viewer updates that require it
			Display display = DebugUIPlugin.getStandardDisplay();
			if (!Thread.currentThread().equals(display.getThread())) {
				WorkbenchJob job = new WorkbenchJob("updating working sets in the breakpoint view") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						view.setBreakpointOrganizers(new IBreakpointOrganizer[] {o});
						return Status.OK_STATUS;
					}
				};
				job.schedule();
				job.join();
			}
			else {
				view.setBreakpointOrganizers(new IBreakpointOrganizer[] {o});
			}
			//add a bp
			createLineBreakpoint(52, "Breakpoints");
			IAdaptable[] elements = set1.getElements();
			assertEquals("Wrong number of breakpoints", 1, elements.length);
			BreakpointSetOrganizer.setDefaultWorkingSet(null);
			//add another bp which will wind up in the 'Others' working set
			createLineBreakpoint(53, "Breakpoints");
			createLineBreakpoint(30, "Breakpoints");
		}
		catch(IndexOutOfBoundsException ioobe) {
			fail("testInsertIntoDefaultSet: bug 380614 has not been fixed");
		}
		finally {
			removeAllBreakpoints();
			getWorkingSetManager().removeWorkingSet(set1);
		}
	}
}
