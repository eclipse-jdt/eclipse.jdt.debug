/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests breakpoint creation/deletion and listener interfaces.
 */
public class BreakpointListenerTests extends AbstractDebugTest implements IBreakpointListener, IBreakpointsListener {
	
	public int fAddCallbacks = 0;
	public int fTotalAdded = 0;
	
	public int fRemoveCallbacks = 0;
	public int fTotalRemoved = 0;
	
	public int fChangeCallabcks = 0;
	public int fTotalChanged = 0;
		
	public BreakpointListenerTests(String name) {
		super(name);
	}

	protected void resetCallbacks() {
		waitForAutoBuild();
		fAddCallbacks = 0;
		fTotalAdded = 0;
	
		fRemoveCallbacks = 0;
		fTotalRemoved = 0;
	
		fChangeCallabcks = 0;
		fTotalChanged = 0;		
	}
	/**
	 * Creates sample breakpoints
	 * 	 * @return List	 */
	protected List createBreakpoints(String typeName) throws Exception {
		List bps = new ArrayList();
		// anonymous class
		bps.add(createUnregisteredLineBreakpoint(43, typeName));
		// blocks
		bps.add(createUnregisteredLineBreakpoint(102, typeName));
		// constructor
		bps.add(createUnregisteredLineBreakpoint(77, typeName));
		// else
		bps.add(createUnregisteredLineBreakpoint(88, typeName));
		//finally after catch
		bps.add(createUnregisteredLineBreakpoint(120, typeName));
		//finally after try
		bps.add(createUnregisteredLineBreakpoint(128, typeName));
		// for loop
		bps.add(createUnregisteredLineBreakpoint(93, typeName));
		// if
		bps.add(createUnregisteredLineBreakpoint(81, typeName));
		// initializer
		bps.add(createUnregisteredLineBreakpoint(17, typeName));
		// inner class
		bps.add(createUnregisteredLineBreakpoint(22, typeName));
		// return true
		bps.add(createUnregisteredLineBreakpoint(72, typeName));
		// instance method
		bps.add(createUnregisteredLineBreakpoint(107, typeName));
		// static method 
		bps.add(createUnregisteredLineBreakpoint(53, typeName));
		// case statement
		bps.add(createUnregisteredLineBreakpoint(133, typeName));
		// default statement
		bps.add(createUnregisteredLineBreakpoint(140, typeName));
		// synchronized blocks
		bps.add(createUnregisteredLineBreakpoint(146, typeName));
		// try
		bps.add(createUnregisteredLineBreakpoint(125, typeName));
		//catch
		bps.add(createUnregisteredLineBreakpoint(118, typeName));
		// while
		bps.add(createUnregisteredLineBreakpoint(97, typeName));
		
		return bps;
	}

	public void testSingleListener() throws Exception {		
		List bps = createBreakpoints("Breakpoints");
		
		try {
			getBreakpointManager().addBreakpointListener((IBreakpointListener)this);		
			
			resetCallbacks();
			getBreakpointManager().addBreakpoints((IBreakpoint[])bps.toArray(new IBreakpoint[bps.size()]));
			assertEquals("Should have received individual add notifications", bps.size(), fAddCallbacks);
			assertEquals("Number of breakpoints added incorrect", bps.size(), fTotalAdded);
			//assertEquals("Should be change callbacks for IMarker.MESSAGE updates", bps.size(), fChangeCallabcks);
			assertEquals("Should be no removes", 0, fRemoveCallbacks);
			
			resetCallbacks();
			getBreakpointManager().removeBreakpoints((IBreakpoint[])bps.toArray(new IBreakpoint[bps.size()]), true);
			assertEquals("Should have received individual remove notifications", bps.size(), fRemoveCallbacks);
			assertEquals("Should of breakpoints removed incorrect", bps.size(), fTotalRemoved);
			assertEquals("Should be no changes", 0, fChangeCallabcks);
			assertEquals("Should be no additions", 0, fAddCallbacks);
			
		} finally {
			getBreakpointManager().removeBreakpointListener((IBreakpointListener)this);
			removeAllBreakpoints();
		}
		
	}
	
	public void testMultiListener() throws Exception {		
		List bps = createBreakpoints("Breakpoints");
		
		try {
			getBreakpointManager().addBreakpointListener((IBreakpointsListener)this);		
			
			resetCallbacks();
			getBreakpointManager().addBreakpoints((IBreakpoint[])bps.toArray(new IBreakpoint[bps.size()]));
			assertEquals("Should have received one add notification", 1, fAddCallbacks);
			assertEquals("Number of breakpoints added incorrect", bps.size(), fTotalAdded);
			//assertEquals("Should be 1 change for IMarker.MESSAGE update", 1, fChangeCallabcks);
			assertEquals("Should be no removes", 0, fRemoveCallbacks);
			
			resetCallbacks();
			getBreakpointManager().removeBreakpoints((IBreakpoint[])bps.toArray(new IBreakpoint[bps.size()]), true);
			assertEquals("Should have received one remove notification", 1, fRemoveCallbacks);
			assertEquals("Should of breakpoints removed incorrect", bps.size(), fTotalRemoved);
			assertEquals("Should be no changes", 0, fChangeCallabcks);
			assertEquals("Should be no additions", 0, fAddCallbacks);
			
		} finally {
			getBreakpointManager().removeBreakpointListener((IBreakpointsListener)this);
			removeAllBreakpoints();
		}
		
	}	
	
	public void testMultiListenerProjectCloseOpen() throws Exception {		
		List bps = createBreakpoints("Breakpoints");
		
		try {
			getBreakpointManager().addBreakpointListener((IBreakpointsListener)this);		
			
			resetCallbacks();
			getBreakpointManager().addBreakpoints((IBreakpoint[])bps.toArray(new IBreakpoint[bps.size()]));
			assertEquals("Should have received one add notification", 1, fAddCallbacks);
			assertEquals("Number of breakpoints added incorrect", bps.size(), fTotalAdded);
			//assertEquals("Should be 1 change for IMarker.MESSAGE update", 1, fChangeCallabcks);
			assertEquals("Should be no removes", 0, fRemoveCallbacks);
			
			resetCallbacks();
			getJavaProject().getProject().close(null);
			waitForAutoBuild();
			assertEquals("Should have received one remove notification", 1, fRemoveCallbacks);
			assertEquals("Should of breakpoints removed incorrect", bps.size(), fTotalRemoved);
			assertEquals("Should be no changes", 0, fChangeCallabcks);
			assertEquals("Should be no additions", 0, fAddCallbacks);
			
			resetCallbacks();
			getJavaProject().getProject().open(null);
			waitForAutoBuild();
			assertEquals("Should have received one add notification", 1, fAddCallbacks);
			assertEquals("Number of breakpoints added incorrect", bps.size(), fTotalAdded);
			assertEquals("Should be no changes", 0, fChangeCallabcks);
			assertEquals("Should be no removes", 0, fRemoveCallbacks);			
			
		} finally {
			getJavaProject().getProject().open(null);
			getBreakpointManager().removeBreakpointListener((IBreakpointsListener)this);
			removeAllBreakpoints();
		}
		
	}		

	public void testMultiListenerMoveCompilationUnit() throws Exception {	
		IJavaProject project = getJavaProject();
		ICompilationUnit cu = (ICompilationUnit)project.findElement(new Path("Breakpoints.java"));
		assertNotNull("Did not find compilation unit", cu);
		cu.copy(cu.getParent(), null, "BreakpointsCopyA.java", false, null);
		waitForAutoBuild();
		List bps = createBreakpoints("BreakpointsCopyA");
		cu = (ICompilationUnit)project.findElement(new Path("BreakpointsCopyA.java"));
		assertNotNull("Did not find compilation unit copy", cu);
		
		try {
			getBreakpointManager().addBreakpointListener((IBreakpointsListener)this);		
			
			resetCallbacks();
			getBreakpointManager().addBreakpoints((IBreakpoint[])bps.toArray(new IBreakpoint[bps.size()]));
			waitForAutoBuild();
			assertEquals("Should have received one add notification", 1, fAddCallbacks);
			assertEquals("Number of breakpoints added incorrect", bps.size(), fTotalAdded);
			//assertEquals("Should be 1 change callback for IMarker.MESSAGE update", 1, fChangeCallabcks);
			assertEquals("Should be no removes", 0, fRemoveCallbacks);
			
			resetCallbacks();
			cu.rename("BreakpointsCopyTwoA.java", false, null);
			waitForAutoBuild();
			assertEquals("Should have received one remove notification", 1, fRemoveCallbacks);
			assertEquals("Number of breakpoints removed incorrect", bps.size(), fTotalRemoved);
			assertEquals("Should be no changes", 0, fChangeCallabcks);
			assertEquals("Should be no additions", 0, fAddCallbacks);
			
			cu = (ICompilationUnit)project.findElement(new Path("BreakpointsCopyTwoA.java"));
			assertNotNull("Did not find CU", cu);
			cu.delete(false, null);
			waitForAutoBuild();
			
		} finally {
			getBreakpointManager().removeBreakpointListener((IBreakpointsListener)this);
			removeAllBreakpoints();
		}
		
	}		
	
	public void testSingleListenerMoveCompilationUnit() throws Exception {	
		IJavaProject project = getJavaProject();
		ICompilationUnit cu = (ICompilationUnit)project.findElement(new Path("Breakpoints.java"));
		assertNotNull("Did not find compilation unit", cu);
		cu.copy(cu.getParent(), null, "BreakpointsCopy.java", false, null);
		waitForAutoBuild();
		List bps = createBreakpoints("BreakpointsCopy");
		cu = (ICompilationUnit)project.findElement(new Path("BreakpointsCopy.java"));
		assertNotNull("Did not find compilation unit copy", cu);
		
		try {
			getBreakpointManager().addBreakpointListener((IBreakpointListener)this);		
			
			resetCallbacks();
			getBreakpointManager().addBreakpoints((IBreakpoint[])bps.toArray(new IBreakpoint[bps.size()]));
			waitForAutoBuild();
			assertEquals("Should have received one add notification", bps.size(), fAddCallbacks);
			assertEquals("Number of breakpoints added incorrect", bps.size(), fTotalAdded);
			//assertEquals("Should be no changes", 0, fChangeCallabcks);
			assertEquals("Should be no removes", 0, fRemoveCallbacks);
			
			resetCallbacks();
			cu.rename("BreakpointsCopyTwo.java", false, null);
			waitForAutoBuild();
			assertEquals("Incorrect number of remove notifications", bps.size(), fRemoveCallbacks);
			assertEquals("Incorrect number of breakpoints removed", bps.size(), fTotalRemoved);
			assertEquals("Should be no changes", 0, fChangeCallabcks);
			assertEquals("Should be no additions", 0, fAddCallbacks);
			
			cu = (ICompilationUnit)project.findElement(new Path("BreakpointsCopyTwo.java"));
			assertNotNull("Did not find CU", cu);			
			cu.delete(false, null);
			waitForAutoBuild();
			
		} finally {
			getBreakpointManager().removeBreakpointListener((IBreakpointListener)this);
			removeAllBreakpoints();
		}
		
	}			

	/**
	 * Creates and returns a line breakpoint at the given line number in the type with the
	 * given name, that is <b>not</b> registered with the breakpoint manager.
	 * 
	 * @param lineNumber line number
	 * @param typeName type name
	 */
	protected IJavaLineBreakpoint createUnregisteredLineBreakpoint(int lineNumber, String typeName) throws Exception {
		return JDIDebugModel.createLineBreakpoint(getBreakpointResource(typeName), typeName, lineNumber, -1, -1, 0, false, null);
	}

	/**
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
		fAddCallbacks++;
		fTotalAdded++;
	}

	/**
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		fChangeCallabcks++;
		fTotalChanged++;
	}

	/**
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		fRemoveCallbacks++;
		fTotalRemoved++;
	}

	/**
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointsAdded(IBreakpoint[] breakpoints) {
		fAddCallbacks++;
		fTotalAdded += breakpoints.length;		
	}

	/**
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointsChanged(
		IBreakpoint[] breakpoints,
		IMarkerDelta[] deltas) {
			fChangeCallabcks++;
			fTotalChanged += breakpoints.length;
	}

	/**
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointsRemoved(
		IBreakpoint[] breakpoints,
		IMarkerDelta[] deltas) {
			fRemoveCallbacks++;
			fTotalRemoved += breakpoints.length;			
	}

}
