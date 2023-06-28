/*******************************************************************************
 * Copyright (c) 2021, 2023 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class RecordBreakpointTests extends AbstractDebugTest {

	public RecordBreakpointTests(String name) {
		super(name);
	}

	/**
	 * Returns the project context for the current test - each test must implement this method
	 */
	@Override
	protected IJavaProject getProjectContext() {
		return get16_Project();
	}

	public void testRecordClassBreakpoint() throws Exception {

		try {
			// create a classLoad breakpoint to test
			IJavaClassPrepareBreakpoint classPrepareBreakpoint = createClassPrepareBreakpoint("a.b.c.RecordTests");
			assertEquals("wrong type name", "a.b.c.RecordTests", classPrepareBreakpoint.getTypeName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	public void testRecordLineBreakpoint() throws Exception {

		try {
			// create a classLoad breakpoint to test
			IJavaLineBreakpoint lineBreakpoint = createLineBreakpoint(17, "a.b.c.RecordTests");
			assertEquals("wrong type name", "a.b.c.RecordTests", lineBreakpoint.getTypeName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	public void testInnerRecordLineBreakpoint() throws Exception {

		try {
			// create a classLoad breakpoint to test
			IJavaLineBreakpoint lineBreakpoint = createLineBreakpoint(18, "a.b.c.RecordInnerTests");
			assertEquals("wrong type name", "a.b.c.RecordInnerTests", lineBreakpoint.getTypeName());
			assertEquals("Breakpoint not at line number 18", 18, lineBreakpoint.getLineNumber());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

}
