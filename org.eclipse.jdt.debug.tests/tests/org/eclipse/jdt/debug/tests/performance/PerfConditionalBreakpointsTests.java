/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.performance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest;

/**
 * Tests performance of conditional breakpoints.
 */
public class PerfConditionalBreakpointsTests extends
        AbstractDebugPerformanceTest {

    public PerfConditionalBreakpointsTests(String name) {
        super(name);
    }

    public void testConditionalBreakpoint() throws Exception {
        String typeName = "PerfLoop";
        IJavaLineBreakpoint bp = createConditionalLineBreakpoint(22, typeName, "i == 99", true);

        List threads= new ArrayList();
        try {
            
            //cold launch, open editor etc.
            IJavaThread thread  = launchToLineBreakpoint(typeName, bp);
            threads.add(thread);
            for (int i = 0; i < 5; i++) {
                try {
                    startMeasuring();
                    thread = launchToLineBreakpoint(typeName, bp);
                    stopMeasuring();
                } finally {
                   threads.add(thread);
                }
            }

            commitMeasurements();
            assertPerformance();

            //verify actually stopping at the correct location
            IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
            IVariable var = frame.findVariable("i");
            assertNotNull("Could not find variable 'i'", var);

            IJavaPrimitiveValue value = (IJavaPrimitiveValue) var.getValue();
            assertNotNull("variable 'i' has no value", value);
            int iValue = value.getIntValue();
            assertEquals("value of 'i' incorrect", 99, iValue);
            bp.delete();
        } finally {
            Iterator iter= threads.iterator();
            while (iter.hasNext()) {
                IJavaThread thread = (IJavaThread) iter.next();
                terminateAndRemove(thread);    
            }
            
            removeAllBreakpoints();
        }
    }
}
