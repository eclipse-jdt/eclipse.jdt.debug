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

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.importexport.breakpoints.ExportOperation;
import org.eclipse.debug.internal.ui.importexport.breakpoints.ImportOperation;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointOrganizerManager;
import org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointOrganizer;
import org.eclipse.debug.internal.ui.views.breakpoints.WorkingSetCategory;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * Tests the import operations of the breakpoint import export feature
 * 
 * @since 3.2
 */
public class ImportBreakpointsTest extends AbstractDebugTest {

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
			ExportOperation op = new ExportOperation(breakpoints.toArray(), path, true);
			op.run(new NullProgressMonitor());
			removeAllBreakpoints();
			File file = path.toFile();
			assertNotNull(file);
			assertEquals(true, file.exists());
			ImportOperation op2 = new ImportOperation(file, true, true);
			op2.run(new NullProgressMonitor());
			assertEquals("manager does not contain 6 breakpoints", 6, getBreakpointManager().getBreakpoints().length);
		} //end try
		finally {
			removeAllBreakpoints();
		}//end finally		
	}//end testBreakpointImportNormal
	
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
			ExportOperation op = new ExportOperation(breakpoints.toArray(), path, true);
			op.run(new NullProgressMonitor());
			File file = path.toFile();
			assertNotNull(file);
			assertEquals(true, file.exists());
			ImportOperation op2 = new ImportOperation(file, true, true);
			op2.run(new NullProgressMonitor());
			assertEquals("manager does not contain 6 breakpoints", 6, getBreakpointManager().getBreakpoints().length);
		}//end try 
		finally {
			removeAllBreakpoints();
		}//end finally
	}
	
	/**
	 * Tests a bad filename passed to the import operation
	 * 
	 * @throws Exception catch all to pass back to framework
	 */
	public void testBreakpointImportBadFilename() throws Exception {
		try {
			ImportOperation op = new ImportOperation(new Path("Badpath").toFile(), true, true);
			op.run(new NullProgressMonitor());
			assertEquals("should be no breakpoints", 0, getBreakpointManager().getBreakpoints().length);
		}//end try
		finally {
			removeAllBreakpoints();
		}//end finally
	}//end testBreakpointImportBadFilename
	
	/**
	 * tests importing breakpoints with working sets
	 * 
	 * @throws Exception catch all to be passed back to the framework
	 */
	public void testBreakpointImportWithWorkingsets() throws Exception {
		try {
		//create the working set and add breakpoints to it
			IBreakpointOrganizer bporg = BreakpointOrganizerManager.getDefault().getOrganizer("org.eclipse.debug.ui.breakpointWorkingSetOrganizer");
			IWorkingSetManager wsmanager = PlatformUI.getWorkbench().getWorkingSetManager();
			String typeName = "DropTests";
			String setName = "ws_name";
			IWorkingSet set = wsmanager.createWorkingSet(setName, new IAdaptable[] {});
			set.setId(IDebugUIConstants.BREAKPOINT_WORKINGSET_ID);
			wsmanager.addWorkingSet(set);
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
			ExportOperation op = new ExportOperation(getBreakpointManager().getBreakpoints(), path, true);
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
			ImportOperation op2 = new ImportOperation(file, true, true);
			op2.run(new NullProgressMonitor());
			set = wsmanager.getWorkingSet(setName);
			assertNotNull("Import did not create working set", set);
			assertEquals("workingset does not contain 6 breakpoints", 6, set.getElements().length);
			assertEquals("manager does not contain 6 breakpoints", 6, getBreakpointManager().getBreakpoints().length);
		}//end try
		finally {
			removeAllBreakpoints();
		}//end finally
	}//end testBreakpointImportWithWorkingsets
	
	/**
	 * Tests importing breakpoints to resources that do not exist
	 * @throws Exception catch all passed to framework
	 */
	public void testBreakpointImportMissingResources() throws Exception {
		try {
			File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/brkpt_missing.bkpt"));
			assertNotNull(file);
			assertEquals(true, file.exists());
			ImportOperation op = new ImportOperation(file, true, true);
			op.run(new NullProgressMonitor());
			assertEquals("should be no breakpoints imported", 0, getBreakpointManager().getBreakpoints().length);
		}//end try 
		finally {
			removeAllBreakpoints();
		}//end finally
	}//end testBreakpointImportMissingResources
	
	/**
	 * Creates a working set and sets the values
	 * @param breakpoint the breakpoint to add to the workingset
	 */
	private void createWorkingSet(String setname, IAdaptable element) {
		IWorkingSetManager wsmanager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet set = wsmanager.getWorkingSet(setname);
		if(set == null) {
			set = wsmanager.createWorkingSet(setname, new IAdaptable[] {});
			set.setId(IDebugUIConstants.BREAKPOINT_WORKINGSET_ID);
			wsmanager.addWorkingSet(set);
		}//end if
		IAdaptable[] elements = set.getElements();
		IAdaptable[] newElements = new IAdaptable[elements.length + 1];
		newElements[newElements.length-1] = (IBreakpoint)element;
		System.arraycopy(elements, 0, newElements, 0, elements.length);
		set.setElements(newElements);
	}//end createWorkingSet
}//end class
