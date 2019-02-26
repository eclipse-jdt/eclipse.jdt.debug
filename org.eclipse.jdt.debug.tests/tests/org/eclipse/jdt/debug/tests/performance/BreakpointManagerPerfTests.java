/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.performance;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.core.BreakpointManager;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IEditorPart;

/**
 * Tests the performance of various parts of the {@link BreakpointManager}
 *
 * @since 3.8
 */
public class BreakpointManagerPerfTests extends AbstractDebugPerformanceTest {

	/**
	 * Constructor
	 * @param name
	 */
	public BreakpointManagerPerfTests() {
		super("Breakpoint Manager Performance");
	}

	static IBreakpoint[] NO_BREAKPOINTS = new IBreakpoint[0];
	static String fgTypeName = "BPManagerPerf";

	/**
	 * Create the given number of breakpoints in the given resource starting from the given line
	 *
	 * @param count
	 * @throws Exception
	 * @return the collection of breakpoints
	 */
	IBreakpoint[] generateBreakpoints(int count) throws Exception {
		IType type = getType(fgTypeName);
		assertNotNull("the type "+fgTypeName+" should exist", type);
		assertTrue("The type "+fgTypeName+" must be a file", type.getResource().getType() == IResource.FILE);
		IEditorPart editor = openEditor((IFile)type.getResource());
		assertNotNull("the editor for "+fgTypeName+" should have been created", editor);
		ArrayList<IBreakpoint> bps = new ArrayList<>(count);
		IBreakpoint bp = createClassPrepareBreakpoint(type);
		if(bp != null) {
			bps.add(bp);
		}
		bp = createMethodBreakpoint(fgTypeName, "main", Signature.createMethodSignature(new String[] {"[Ljava.lang.String;"}, Signature.SIG_VOID), true, true);
		if(bp != null) {
			bps.add(bp);
		}
		int end = 12 + count;
		for (int i = 14; i < end; i++) {
			bp = createLineBreakpoint(i, fgTypeName);
			if(bp != null) {
				bps.add(bp);
			}
		}
		assertEquals("the should have been "+count+" breakpoints created", count, bps.size());
		return bps.toArray(new IBreakpoint[bps.size()]);
	}

	/**
	 * Tests the time required to start the {@link BreakpointManager}
	 * @throws Exception
	 */
	public void testStartup200Breakpoints() throws Exception {
		tagAsSummary("Start Breakpoint Manager - 200 BPs", Dimension.ELAPSED_PROCESS);
		try {
			IBreakpoint[] bps = generateBreakpoints(200);
			assertTrue("There should be 200 breakpoints", bps.length == 200);
			BreakpointManager mgr = (BreakpointManager) getBreakpointManager();
			//clean it up before starting
			mgr.shutdown();
			for(int i = 0; i < 6500; i++) {
				try {
					startMeasuring();
					mgr.ensureInitialized();
					stopMeasuring();
				}
				finally {
					mgr.shutdown();
				}
			}
			commitMeasurements();
			assertPerformance();
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the time required to start the {@link BreakpointManager}
	 * @throws Exception
	 */
	public void testStartup50Breakpoints() throws Exception {
		tagAsSummary("Start Breakpoint Manager - 50 BPs", Dimension.ELAPSED_PROCESS);
		try {
			IBreakpoint[] bps = generateBreakpoints(50);
			assertTrue("There should be 50 breakpoints", bps.length == 50);
			BreakpointManager mgr = (BreakpointManager) getBreakpointManager();
			//clean it up before starting
			mgr.shutdown();
			for(int i = 0; i < 6500; i++) {
				try {
					startMeasuring();
					mgr.ensureInitialized();
					stopMeasuring();
				}
				finally {
					mgr.shutdown();
				}
			}
			commitMeasurements();
			assertPerformance();
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the time required to start the {@link BreakpointManager}
	 * @throws Exception
	 */
	public void testStartup100Breakpoints() throws Exception {
		tagAsSummary("Start Breakpoint Manager - 100 BPs", Dimension.ELAPSED_PROCESS);
		try {
			IBreakpoint[] bps = generateBreakpoints(100);
			assertTrue("There should be 100 breakpoints", bps.length == 100);
			BreakpointManager mgr = (BreakpointManager) getBreakpointManager();
			//clean it up before starting
			mgr.shutdown();
			for(int i = 0; i < 6500; i++) {
				try {
					startMeasuring();
					mgr.ensureInitialized();
					stopMeasuring();
				}
				finally {
					mgr.shutdown();
				}
			}
			commitMeasurements();
			assertPerformance();
		}
		finally {
			removeAllBreakpoints();
		}
	}
}
