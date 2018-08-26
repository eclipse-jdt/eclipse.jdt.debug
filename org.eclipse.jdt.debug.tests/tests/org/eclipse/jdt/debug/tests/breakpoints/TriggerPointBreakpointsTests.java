/*******************************************************************************
 *  Copyright (c) 2016 IBM Corporation and others.
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

import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests trigger point of breakpoints
 */
public class TriggerPointBreakpointsTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public TriggerPointBreakpointsTests(String name) {
		super(name);
	}

	/**
	 * Tests the trigger point
	 *
	 * @throws Exception
	 */
	public void testTriggerPointBreakpoint() throws Exception {
		String typeName = "TriggerPoint_01";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(28, typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(33, typeName);
		bp2.setTriggerPoint(true);

		IJavaThread thread= null;
		try {
			thread = launchToLineBreakpoint(typeName, bp2);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable var = findVariable(frame, "i");
			assertNotNull("Could not find variable 'i'", var);

			IJavaPrimitiveValue value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull("variable 'i' has no value", value);
			int iValue = value.getIntValue();
			assertTrue("value of 'i' should be '1', but was " + iValue, iValue == 1);

			var = findVariable(frame, "j");
			assertNotNull("Could not find variable 'j'", var);

			value = (IJavaPrimitiveValue) var.getValue();
			assertNotNull("variable 'j' has no value", value);
			int jValue = value.getIntValue();
			assertTrue("value of 'j' should be '1', but was " + jValue, jValue == 1);

			bp1.delete();
			bp2.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
