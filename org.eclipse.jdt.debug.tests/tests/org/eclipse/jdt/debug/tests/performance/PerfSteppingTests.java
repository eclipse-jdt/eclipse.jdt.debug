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
package org.eclipse.jdt.debug.tests.performance;

import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests performance of stepping.
 */
public class PerfSteppingTests extends AbstractDebugTest {
	
	public PerfSteppingTests(String name) {
		super(name);
	}

	public void testRapidStepping() throws Exception {
		String typeName = "PerfLoop";
		IJavaLineBreakpoint bp = createLineBreakpoint(20, typeName);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			for (int i = 0; i < 100; i++) {
				stepOver(frame);
			}

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
}
