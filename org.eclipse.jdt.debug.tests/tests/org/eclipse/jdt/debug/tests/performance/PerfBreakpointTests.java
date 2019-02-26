/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.jdt.debug.tests.performance;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest;
import org.eclipse.test.performance.Dimension;

/**
 * Tests performance of several breakpoints
 */
public class PerfBreakpointTests extends AbstractDebugPerformanceTest implements IBreakpointListener {

    int breakpointCount = 0;

    /**
     * Constructor
     * @param name
     */
    public PerfBreakpointTests(String name) {
        super(name);
    }

    /**
     * Tests the performance of line breakpoint creation
     * @throws Exception
     */
    public void testLineBreakpointCreation() throws Exception {
        tagAsSummary("Install Line Breakpoints", Dimension.ELAPSED_PROCESS);
        String typeName = "LargeSourceFile";
        IResource resource = getBreakpointResource(typeName);

        IJavaLineBreakpoint bp = createLineBreakpoint(17, typeName);
        IJavaThread thread = launchToBreakpoint(typeName, false);
        bp.delete();

        try {
            DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);

            int[] lineNumbers = new int[150];
            for (int i = 0; i < lineNumbers.length; i++) {
                lineNumbers[i] = 18 + i;
            }

            for (int i = 0; i < 10; i++) {
                createLineBreakpoints(resource, typeName, lineNumbers);
                waitForBreakpointCount(lineNumbers.length);
                removeAllBreakpoints();
                waitForBreakpointCount(0);
                breakpointCount = 0;
            }

            for (int i = 0; i < 100; i++) {
                System.gc();
                startMeasuring();
                createLineBreakpoints(resource, typeName, lineNumbers);
                waitForBreakpointCount(lineNumbers.length);
                stopMeasuring();
                removeAllBreakpoints();
                waitForBreakpointCount(0);
                breakpointCount = 0;
            }
            commitMeasurements();
            assertPerformance();
        } finally {
            DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
            removeAllBreakpoints();

            terminateAndRemove(thread);
        }
    }

    /**
     * Tests the performance of breakpoint removal
     * @throws Exception
     */
    public void testBreakpointRemoval() throws Exception {
        String typeName = "LargeSourceFile";
        IResource resource = getBreakpointResource(typeName);

        IJavaLineBreakpoint bp = createLineBreakpoint(17, typeName);
        IJavaThread thread = launchToBreakpoint(typeName, false);
        bp.delete();

        IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
        try {
            manager.addBreakpointListener(this);

            int[] lineNumbers = new int[50];
            for (int i = 0; i < lineNumbers.length; i++) {
                lineNumbers[i] = 18 + i;
            }

            for (int i = 0; i < 10; i++) {
                createLineBreakpoints(resource, typeName, lineNumbers);
                waitForBreakpointCount(lineNumbers.length);
                IBreakpoint[] breakpoints = manager.getBreakpoints();
                manager.removeBreakpoints(breakpoints, true);
                waitForBreakpointCount(0);

            }

            lineNumbers = new int[250];
            for (int i = 0; i < lineNumbers.length; i++) {
                lineNumbers[i] = 15 + i;
            }

            for (int i = 0; i < 150; i++) {
                createLineBreakpoints(resource, typeName, lineNumbers);
                waitForBreakpointCount(lineNumbers.length);
                IBreakpoint[] breakpoints = manager.getBreakpoints();
                System.gc();
                startMeasuring();
                manager.removeBreakpoints(breakpoints, true);
                waitForBreakpointCount(0);
                stopMeasuring();

            }
            commitMeasurements();
            assertPerformance();
        } finally {
            manager.removeBreakpointListener(this);
            removeAllBreakpoints();

            terminateAndRemove(thread);
        }
    }

    /**
     * Tests the performance of method entry breakpoint creation
     * @throws Exception
     */
    public void testMethodEntryBreakpointCreation() throws Exception {
        tagAsSummary("Install Method Entry Breakpoints", Dimension.ELAPSED_PROCESS);
        String typeName = "LargeSourceFile";
        IProject project = get14Project().getProject();

        IJavaLineBreakpoint bp = createLineBreakpoint(17, typeName);
        IJavaThread thread = launchToBreakpoint(typeName, false);
        bp.delete();

        IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
        try {
            manager.addBreakpointListener(this);

            String[] methods = new String[300];
            for (int i = 0; i < methods.length; i++) {
                methods[i] = "method"+(i+1);
            }

            for (int i = 0; i < 10; i++) {
                createMethodEntryBreakpoints(project, typeName, methods);
                waitForBreakpointCount(methods.length);
                removeAllBreakpoints();
                waitForBreakpointCount(0);

            }

            for (int i = 0; i < 100; i++) {
                System.gc();
                startMeasuring();
                createMethodEntryBreakpoints(project, typeName, methods);
                waitForBreakpointCount(methods.length);
                stopMeasuring();
                removeAllBreakpoints();

                breakpointCount = 0;
            }
            commitMeasurements();
            assertPerformance();
        } finally {
            DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
            removeAllBreakpoints();

            terminateAndRemove(thread);
        }
    }

    /**
     * Tests the performance of watchpoint creation
     * @throws Exception
     */
    public void testWatchpointCreation() throws Exception {
        tagAsSummary("Install Watchpoints", Dimension.ELAPSED_PROCESS);
        String typeName = "LotsOfFields";
        IResource resource = getBreakpointResource(typeName);

        IJavaLineBreakpoint bp = createLineBreakpoint(519, typeName);
        IJavaThread thread = launchToBreakpoint(typeName, false);
        bp.delete();

        IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
        try {
            manager.addBreakpointListener(this);

            String[] fields = new String[300];
            for (int i = 0; i < fields.length; i++) {
                fields[i] = "field_"+(i+1);
            }

            for (int i = 0; i < 10; i++) {
                createWatchpoints(resource, typeName, fields);
                waitForBreakpointCount(fields.length);
                removeAllBreakpoints();
                waitForBreakpointCount(0);
            }

            for (int i = 0; i < 100; i++) {
                System.gc();
                startMeasuring();
                createWatchpoints(resource, typeName, fields);
                waitForBreakpointCount(fields.length);
                stopMeasuring();
                removeAllBreakpoints();

                breakpointCount = 0;
            }
            commitMeasurements();
            assertPerformance();
        } finally {
            DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
            removeAllBreakpoints();

            terminateAndRemove(thread);
        }
    }

    /**
     * Waits for the specified breakpoint count to be hit
     * @param i
     * @throws Exception
     */
    private synchronized void waitForBreakpointCount(int i) throws Exception {
        long end = System.currentTimeMillis() + 60000;
        while (breakpointCount != i && System.currentTimeMillis() < end) {
            wait(30000);
        }
        assertEquals("Expected " + i + " breakpoints, notified of " + breakpointCount, i, breakpointCount);
    }

    /**
     * Creates line breakpoints on the specified line numbers in the given type name with the given resource
     * @param resource
     * @param typeName
     * @param lineNumbers
     * @throws CoreException
     */
    private void createLineBreakpoints(IResource resource, String typeName, int[] lineNumbers) throws CoreException {
        for (int i = 0; i < lineNumbers.length; i++) {
            JDIDebugModel.createLineBreakpoint(resource, typeName, lineNumbers[i], -1, -1, 0, true, null);
        }
    }

    /**
     * Creates method entry breakpoints on the specified methods in the given type name in the specified project
     * @param project
     * @param typeName
     * @param methods
     * @throws CoreException
     */
    private void createMethodEntryBreakpoints(IProject project, String typeName, String[] methods) throws CoreException {
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i];
            JDIDebugModel.createMethodBreakpoint(project, typeName, methodName, "()V", true, false, false, -1, -1, -1, 0, true, null);
        }
    }

    /**
     * Creates watchpoints on the specified fields of the specified resource with the given type name
     * @param resource
     * @param typeName
     * @param fields
     * @throws Exception
     */
    private void createWatchpoints(IResource resource, String typeName, String[] fields) throws Exception {
        for(int i = 0; i < fields.length; i++) {
            IJavaWatchpoint wp = JDIDebugModel.createWatchpoint(resource, typeName, fields[i], -1, -1, -1, 0, true, null);
            wp.setAccess(true);
            wp.setModification(true);
        }
    }

    /**
     * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse.debug.core.model.IBreakpoint)
     */
    @Override
	public synchronized void breakpointAdded(IBreakpoint breakpoint) {
        breakpointCount++;
        notifyAll();
    }

    /**
     * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
     */
    @Override
	public synchronized void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
        breakpointCount--;
        notifyAll();
    }

    /**
     * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
     */
    @Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
    }
}
