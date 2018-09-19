/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests equality of Java types.
 */
public class TypeTests extends AbstractDebugTest {

	public TypeTests(String name) {
		super(name);
	}

	public void testDeclaringTypes() throws Exception {
		IJavaType[] types = new IJavaType[3];
		int index = 0;
		String typeName = "Breakpoints";
		List<IJavaLineBreakpoint> bps = new ArrayList<>();
		// main
		bps.add(createLineBreakpoint(55, typeName));
		// threading
		bps.add(createLineBreakpoint(69, typeName));
		// InnerRunnable.run
		bps.add(createLineBreakpoint(64, typeName));

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			while (!bps.isEmpty()) {
				IBreakpoint hit = getBreakpoint(thread);
				assertNotNull("suspended, but not by breakpoint", hit);
				assertTrue("hit un-registered breakpoint", bps.contains(hit));
				assertTrue("suspended, but not by line breakpoint", hit instanceof ILineBreakpoint);
				ILineBreakpoint breakpoint= (ILineBreakpoint) hit;
				int lineNumber = breakpoint.getLineNumber();
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				types[index] = frame.getReferenceType();

				if (index == 1) {
					assertEquals("First two types should be the same", types[0], types[1]);
				}

				if (index == 2) {
					assertTrue("Last two types should be different", !types[0].equals(types[2]));
				}

				index++;
				int stackLine = frame.getLineNumber();
				assertTrue("line numbers of breakpoint and stack frame do not match", lineNumber == stackLine);
				bps.remove(breakpoint);
				breakpoint.delete();
				if (!bps.isEmpty()) {
					thread = resume(thread);
				}
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

    public void testClassLoader() throws Exception {
        String typeName = "Breakpoints";
        List<IJavaLineBreakpoint> bps = new ArrayList<>();
        // instance method
		bps.add(createLineBreakpoint(84, typeName));
        bps.add(createLineBreakpoint(81, typeName));
        IJavaObject[] loaders = new IJavaObject[2];
        int index = 0;

        IJavaThread thread= null;
        try {
            thread= launchToBreakpoint(typeName);
            assertNotNull("Breakpoint not hit within timeout period", thread);
            while (!bps.isEmpty()) {
                IBreakpoint hit = getBreakpoint(thread);
                assertNotNull("suspended, but not by breakpoint", hit);
                assertTrue("hit un-registered breakpoint", bps.contains(hit));
                assertTrue("suspended, but not by line breakpoint", hit instanceof ILineBreakpoint);
                ILineBreakpoint breakpoint= (ILineBreakpoint) hit;
                int lineNumber = breakpoint.getLineNumber();
                IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
                loaders[index] = frame.getReferenceType().getClassLoaderObject();
                assertNotNull("class loader cannot be null", loaders[index]);

                if (index == 1) {
                    assertEquals("First two class loaders should be the same", loaders[0], loaders[1]);
                }

                index++;
                int stackLine = frame.getLineNumber();
                assertTrue("line numbers of breakpoint and stack frame do not match", lineNumber == stackLine);
                bps.remove(breakpoint);
                breakpoint.delete();
                if (!bps.isEmpty()) {
                    thread = resume(thread);
                }
            }
        } finally {
            terminateAndRemove(thread);
            removeAllBreakpoints();
        }
    }

}
