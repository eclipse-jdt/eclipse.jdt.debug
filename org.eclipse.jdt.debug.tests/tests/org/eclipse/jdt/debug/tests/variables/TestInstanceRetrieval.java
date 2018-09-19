/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.variables;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests for instance retrieval
 */
public class TestInstanceRetrieval extends AbstractDebugTest {

	/**
	 * Constructs test.
	 *
	 * @param name test name
	 */
	public TestInstanceRetrieval(String name) {
		super(name);
	}

	/**
	 * Test the logical structure for a list.
	 *
	 * @throws Exception
	 */
	public void testGetInstances() throws Exception {
		String typeName = "java6.AllInstancesTests";
		createLineBreakpoint(64, typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(66, typeName);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			IJavaDebugTarget target = (IJavaDebugTarget) thread.getDebugTarget();
			if (target.supportsInstanceRetrieval()) {
				assertNotNull("Breakpoint not hit within timeout period", thread);

				IBreakpoint hit = getBreakpoint(thread);
				assertNotNull("suspended, but not by breakpoint", hit);

				IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertNotNull("missing top frame", frame);

				IJavaVariable variable = frame.findVariable("ro");
				assertNotNull("Missing variable 'ro'", variable);

				IJavaObject object = (IJavaObject) variable.getValue();
				IJavaReferenceType refType = (IJavaReferenceType) object.getJavaType();
				long instanceCount = refType.getInstanceCount();
				IJavaObject[] instances = refType.getInstances(100);

				assertEquals("Wrong instance count", 13, instanceCount);
				assertEquals("Wrong number of instances", 13, instances.length);
				for (int i = 0; i < instances.length; i++) {
					assertEquals("Instance is of unexpected type", refType, instances[i].getJavaType());
				}

				thread = resumeToLineBreakpoint(thread, bp2);
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertNotNull("missing top frame", frame);

				variable = frame.findVariable("rc");
				assertNotNull("Missing variable 'rc'", variable);

				object = (IJavaObject) variable.getValue();
				refType = (IJavaReferenceType) object.getJavaType();
				instanceCount = refType.getInstanceCount();
				instances = refType.getInstances(100);

				assertEquals("Wrong instance count", 1002, instanceCount);
				assertEquals("Wrong number of instances", 100, instances.length);
				for (int i = 0; i < instances.length; i++) {
					assertEquals("Instance is of unexpected type", refType, instances[i].getJavaType());
				}
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

}
