/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointOrganizerManager;
import org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointOrganizer;
import org.eclipse.debug.internal.ui.views.breakpoints.WorkingSetCategory;
import org.eclipse.debug.ui.actions.ExportBreakpointsOperation;
import org.eclipse.debug.ui.actions.ImportBreakpointsOperation;
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
	 * Tests the normal import operation 
	 * @throws Exception catch all passed to framework
	 */
	public void testBreakpointImport() throws Exception {
		try {
			ArrayList breakpoints = new ArrayList();
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
			ExportBreakpointsOperation op = new ExportBreakpointsOperation((IBreakpoint[]) breakpoints.toArray(new IBreakpoint[breakpoints.size()]), path.toOSString());
			op.run(new NullProgressMonitor());
			removeAllBreakpoints();
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
	 * tests and overwrote without remove all
	 * @throws Exception catch all to pass back to framework
	 */
	public void testBreakpointImportOverwrite() throws Exception {
		try {
			ArrayList breakpoints = new ArrayList();
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
			ExportBreakpointsOperation op = new ExportBreakpointsOperation((IBreakpoint[]) breakpoints.toArray(new IBreakpoint[breakpoints.size()]), path.toOSString());
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
	
}
