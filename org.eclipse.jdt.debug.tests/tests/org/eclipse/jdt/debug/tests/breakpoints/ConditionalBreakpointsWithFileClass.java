/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation -- initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class ConditionalBreakpointsWithFileClass extends AbstractDebugTest {


	public ConditionalBreakpointsWithFileClass(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get14Project();
	}

	public void testFileConditionalBreakpointforFalse() throws Exception {
		String typeName = "FileConditionSnippet2";
		IJavaLineBreakpoint bp3 = createLineBreakpoint(20, typeName);
		IJavaLineBreakpoint bp2 = createConditionalLineBreakpoint(364, "java.io.File", "false", true);
		IJavaThread mainThread = null;
		try {
			Thread.sleep(10);
			mainThread = launchToBreakpoint(typeName);
			int hitLine = 0;
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			assertEquals("Didn't suspend at the expected line", 20, hitLine);

			bp2.delete();
			bp3.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

	public void testFileConditionalBreakpointforTrue() throws Exception {
		String typeName = "FileConditionSnippet2";
		IJavaLineBreakpoint bp3 = createLineBreakpoint(20, typeName);
		IJavaLineBreakpoint bp2;
		IJavaThread mainThread = null;

		try {
			Thread.sleep(10);
			mainThread = launchToBreakpoint(typeName);
			mainThread.getTopStackFrame().stepInto();

			if (JavaProjectHelper.isJava25_Compatible()) {
				bp2 = createConditionalLineBreakpoint(369, "java.io.File", "true", true);
			} else {
				bp2 = createConditionalLineBreakpoint(364, "java.io.File", "true", true);
			}

			int hitLine = 0;
			mainThread.resume();
			Thread.sleep(1000);
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			if (JavaProjectHelper.isJava25_Compatible()) {
				assertEquals("Didn't suspend at the expected line", 369, hitLine);
			} else {
				assertEquals("Didn't suspend at the expected line", 364, hitLine);
			}

			bp2.delete();
			bp3.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

}
