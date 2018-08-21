/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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

package org.eclipse.jdt.debug.tests.breakpoints;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointOrganizer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointOrganizerManager;
import org.eclipse.debug.internal.ui.views.breakpoints.WorkingSetCategory;
import org.eclipse.debug.ui.actions.ExportBreakpointsOperation;
import org.eclipse.debug.ui.actions.ImportBreakpointsOperation;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;

/**
 * Tests the import operations of the breakpoint import export feature
 *
 * @since 3.2
 */
public class ImportBreakpointsTest extends AbstractBreakpointWorkingSetTest {

	/**
	 * Default constructor
	 * @param name the name for the test
	 */
	public ImportBreakpointsTest(String name) {super(name);}

	/**
	 * Tests import operation from a file.
	 * @throws Exception catch all passed to framework
	 */
	public void testBreakpointImportFile() throws Exception {
		try {
			ArrayList<IJavaBreakpoint> breakpoints = new ArrayList<>();
			String typeName = "DropTests";
			breakpoints.add(createClassPrepareBreakpoint(typeName));
			breakpoints.add(createLineBreakpoint(32, typeName));
			breakpoints.add(createLineBreakpoint(28, typeName));
			breakpoints.add(createLineBreakpoint(24, typeName));
			breakpoints.add(createExceptionBreakpoint("Exception", true, false));
			breakpoints.add(createMethodBreakpoint(typeName, "method4", "()V", true, false));
			assertEquals("manager does not contain 6 breakpoints for exporting", getBreakpointManager().getBreakpoints().length, 6);
			Path path = new Path("exbkptA.bkpt");
			assertNotNull("Invalid path", path);
			ExportBreakpointsOperation op = new ExportBreakpointsOperation(breakpoints.toArray(new IBreakpoint[breakpoints.size()]), path.toOSString());
			op.run(new NullProgressMonitor());
			removeAllBreakpoints();
			File file = path.toFile();
			assertNotNull(file);
			assertEquals(true, file.exists());
			ImportBreakpointsOperation op2 = new ImportBreakpointsOperation(path.toOSString(), true, true);
			op2.run(new NullProgressMonitor());
			assertEquals("manager does not contain 6 breakpoints", 6, getBreakpointManager().getBreakpoints().length);
			IBreakpoint[] importedBreakpoints = op2.getImportedBreakpoints();
			assertEquals("imported list should contain same breakpoints", 6, importedBreakpoints.length);
			for (int i = 0; i < importedBreakpoints.length; i++) {
				IBreakpoint breakpoint = importedBreakpoints[i];
				assertEquals("Missing imported breakpoint", breakpoint, getBreakpointManager().getBreakpoint(breakpoint.getMarker()));
			}
			file.delete();
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests import operation from a string buffer.
	 *
	 * @throws Exception catch all passed to framework
	 */
	public void testBreakpointImportBuffer() throws Exception {
		try {
			ArrayList<IJavaBreakpoint> breakpoints = new ArrayList<>();
			String typeName = "DropTests";
			breakpoints.add(createClassPrepareBreakpoint(typeName));
			breakpoints.add(createLineBreakpoint(32, typeName));
			breakpoints.add(createLineBreakpoint(28, typeName));
			breakpoints.add(createLineBreakpoint(24, typeName));
			breakpoints.add(createExceptionBreakpoint("Exception", true, false));
			breakpoints.add(createMethodBreakpoint(typeName, "method4", "()V", true, false));
			assertEquals("manager does not contain 6 breakpoints for exporting", getBreakpointManager().getBreakpoints().length, 6);
			ExportBreakpointsOperation op = new ExportBreakpointsOperation(breakpoints.toArray(new IBreakpoint[breakpoints.size()]));
			op.run(new NullProgressMonitor());
			StringBuffer buffer = op.getBuffer();
			assertNotNull("Missing buffer", buffer);
			removeAllBreakpoints();
			ImportBreakpointsOperation op2 = new ImportBreakpointsOperation(buffer, true, true);
			op2.run(new NullProgressMonitor());
			assertEquals("manager does not contain 6 breakpoints", 6, getBreakpointManager().getBreakpoints().length);
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * tests and overwrote without remove all
	 * @throws Exception catch all to pass back to framework
	 */
	public void testBreakpointImportOverwrite() throws Exception {
		try {
			ArrayList<IJavaBreakpoint> breakpoints = new ArrayList<>();
			String typeName = "DropTests";
			breakpoints.add(createClassPrepareBreakpoint(typeName));
			breakpoints.add(createLineBreakpoint(32, typeName));
			breakpoints.add(createLineBreakpoint(28, typeName));
			breakpoints.add(createLineBreakpoint(24, typeName));
			breakpoints.add(createExceptionBreakpoint("Exception", true, false));
			breakpoints.add(createMethodBreakpoint(typeName, "method4", "()V", true, false));
			assertEquals("manager does not contain 6 breakpoints for exporting", getBreakpointManager().getBreakpoints().length, 6);
			Path path = new Path("exbkptB.bkpt");
			assertNotNull("Invalid path", path);
			ExportBreakpointsOperation op = new ExportBreakpointsOperation(breakpoints.toArray(new IBreakpoint[breakpoints.size()]), path.toOSString());
			op.run(new NullProgressMonitor());
			File file = path.toFile();
			assertNotNull(file);
			assertEquals(true, file.exists());
			ImportBreakpointsOperation op2 = new ImportBreakpointsOperation(path.toOSString(), true, true);
			op2.run(new NullProgressMonitor());
			assertEquals("manager does not contain 6 breakpoints", 6, getBreakpointManager().getBreakpoints().length);
			file.delete();
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a bad filename passed to the import operation
	 *
	 * @throws Exception catch all to pass back to framework
	 */
	public void testBreakpointImportBadFilename() throws Exception {
		try {
			ImportBreakpointsOperation op = new ImportBreakpointsOperation("Badpath", true, true);
			try {
				op.run(new NullProgressMonitor());
			} catch (InvocationTargetException e) {
				assertEquals("should be no breakpoints", 0, getBreakpointManager().getBreakpoints().length);
				return;
			}
			assertTrue("Import should have failed with exception", false);
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * tests importing breakpoints with working sets
	 *
	 * @throws Exception catch all to be passed back to the framework
	 */
	public void testBreakpointImportWithWorkingsets() throws Exception {
		try {
		//create the working set and add breakpoints to it
			IBreakpointOrganizer bporg = BreakpointOrganizerManager.getDefault().getOrganizer("org.eclipse.debug.ui.breakpointWorkingSetOrganizer");
			IWorkingSetManager wsmanager = getWorkingSetManager();
			String typeName = "DropTests";
			String setName = "ws_name";
			IWorkingSet set = createSet(setName);
			assertNotNull("workingset does not exist", wsmanager.getWorkingSet(setName));
			WorkingSetCategory category = new WorkingSetCategory(set);

			bporg.addBreakpoint(createClassPrepareBreakpoint(typeName), category);
			bporg.addBreakpoint(createLineBreakpoint(32, typeName), category);
			bporg.addBreakpoint(createLineBreakpoint(28, typeName), category);
			bporg.addBreakpoint(createLineBreakpoint(24, typeName), category);
			bporg.addBreakpoint(createExceptionBreakpoint("Exception", true, false), category);
			bporg.addBreakpoint(createMethodBreakpoint(typeName, "method4", "()V", true, false), category);
			assertEquals("workingset does not have 6 elements", 6, set.getElements().length);
			assertEquals("manager does not have 6 breakpoints", getBreakpointManager().getBreakpoints().length, 6);
			Path path = new Path("exbkptC.bkpt");
			assertNotNull("Invalid path", path);
			ExportBreakpointsOperation op = new ExportBreakpointsOperation(getBreakpointManager().getBreakpoints(), path.toOSString());
			op.run(new NullProgressMonitor());

			//remove bps and working set and do the import
			removeAllBreakpoints();
			set.setElements(new IAdaptable[] {});
			wsmanager.removeWorkingSet(set);
			set = wsmanager.getWorkingSet(setName);
			assertNull("workingset was not removed", set);
			set = null;
			File file = path.toFile();
			assertNotNull(file);
			assertEquals(true, file.exists());
			ImportBreakpointsOperation op2 = new ImportBreakpointsOperation(path.toOSString(), true, true);
			op2.run(new NullProgressMonitor());
			set = wsmanager.getWorkingSet(setName);
			assertNotNull("Import did not create working set", set);
			assertEquals("workingset does not contain 6 breakpoints", 6, set.getElements().length);
			assertEquals("manager does not contain 6 breakpoints", 6, getBreakpointManager().getBreakpoints().length);
			file.delete();
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests importing breakpoints to resources that do not exist
	 * @throws Exception catch all passed to framework
	 */
	public void testBreakpointImportMissingResources() throws Exception {
		try {
			File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/brkpt_missing.bkpt"));
			assertNotNull(file);
			assertEquals(true, file.exists());
			ImportBreakpointsOperation op = new ImportBreakpointsOperation(file.getCanonicalPath(), true, true);
			op.run(new NullProgressMonitor());
			assertEquals("should be no breakpoints imported", 0, getBreakpointManager().getBreakpoints().length);
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * tests importing breakpoints with working sets where the breakpoints are restored to
	 * another working set and removed from the current one
	 *
	 * @throws Exception catch all to be passed back to the framework
	 */
	public void testImportWithWorkingsets() throws Exception {
		try {
		//create the working set and add breakpoints to it
			IBreakpointOrganizer bporg = BreakpointOrganizerManager.getDefault().getOrganizer("org.eclipse.debug.ui.breakpointWorkingSetOrganizer");
			IWorkingSetManager wsmanager = getWorkingSetManager();
			String typeName = "DropTests";
			String setname1 = "ws1";
			String setname2 = "ws2";
			IWorkingSet set = createSet(setname1);
			assertNotNull("workingset does not exist", wsmanager.getWorkingSet(setname1));
			WorkingSetCategory category = new WorkingSetCategory(set);

			bporg.addBreakpoint(createClassPrepareBreakpoint(typeName), category);
			bporg.addBreakpoint(createLineBreakpoint(32, typeName), category);
			bporg.addBreakpoint(createLineBreakpoint(28, typeName), category);
			bporg.addBreakpoint(createLineBreakpoint(24, typeName), category);
			bporg.addBreakpoint(createExceptionBreakpoint("Exception", true, false), category);
			bporg.addBreakpoint(createMethodBreakpoint(typeName, "method4", "()V", true, false), category);
			assertEquals("workingset does not have 6 elements", 6, set.getElements().length);
			assertEquals("manager does not have 6 breakpoints", getBreakpointManager().getBreakpoints().length, 6);
			Path path = new Path("exbkptC.bkpt");
			assertNotNull("Invalid path", path);
			ExportBreakpointsOperation op = new ExportBreakpointsOperation(getBreakpointManager().getBreakpoints(), path.toOSString());
			op.run(new NullProgressMonitor());

			//copy items to the alternate working set
			IWorkingSet set2 = createSet(setname2);
			set2.setElements(set.getElements());
			assertNotNull("workingset does not exist", wsmanager.getWorkingSet(setname2));

			//remove bps and working set and do the import
			removeAllBreakpoints();
			set.setElements(new IAdaptable[] {});
			wsmanager.removeWorkingSet(set);
			set = wsmanager.getWorkingSet(setname1);
			assertNull("workingset was not removed", set);
			set = null;
			File file = path.toFile();
			assertNotNull(file);
			assertEquals(true, file.exists());
			ImportBreakpointsOperation op2 = new ImportBreakpointsOperation(path.toOSString(), true, true);
			op2.run(new NullProgressMonitor());
			set = wsmanager.getWorkingSet(setname1);
			assertNotNull("Import did not create working set", set);
			assertEquals("workingset does not contain 6 breakpoints", 6, set.getElements().length);
			assertEquals("alternate working set should contain no breakpoints", 0, set2.getElements().length);
			assertEquals("manager does not contain 6 breakpoints", 6, getBreakpointManager().getBreakpoints().length);
			file.delete();
		}
		finally {
			removeAllBreakpoints();
		}
	}
}
