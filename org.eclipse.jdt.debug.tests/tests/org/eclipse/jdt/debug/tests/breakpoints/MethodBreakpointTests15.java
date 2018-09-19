/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.ui.IEditorPart;

/**
 * Tests method breakpoints for 1.5 source code.
 */
public class MethodBreakpointTests15 extends AbstractDebugTest {

	public MethodBreakpointTests15(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.tests.AbstractDebugTest#getProjectContext()
	 */
	@Override
	protected IJavaProject getProjectContext() {
		return get15Project();
	}

	public void testStaticTypeParameter() throws Exception {
		IJavaMethodBreakpoint breakpoint = createBreakpoint(28);
		assertEquals("Wrong method", "staticTypeParameter", breakpoint.getMethodName());
		runToBreakpoint(getTypeName(), breakpoint);
	}

	public void testTypeParameter() throws Exception {
		IJavaMethodBreakpoint breakpoint = createBreakpoint(32);
		assertEquals("Wrong method", "typeParameter", breakpoint.getMethodName());
		runToBreakpoint(getTypeName(), breakpoint);
	}

	public void testMethodTypeParameter() throws Exception {
		IJavaMethodBreakpoint breakpoint = createBreakpoint(37);
		assertEquals("Wrong method", "methodTypeParameter", breakpoint.getMethodName());
		runToBreakpoint(getTypeName(), breakpoint);
	}

	private String getTypeName() {
		return "a.b.c.MethodBreakpoints";
	}

	private IJavaMethodBreakpoint createBreakpoint(int line) throws Exception {
		IType type = get15Project().findType(getTypeName());
		assertNotNull("Missing file", type);
		IResource resource = type.getResource();
		assertTrue("Missing file", resource instanceof IFile);
		IEditorPart editor = openEditor((IFile) resource);
		IBreakpoint breakpoint = toggleBreakpoint(editor, line);
		assertTrue("Wrong breakpoint", breakpoint instanceof IJavaMethodBreakpoint);
		return (IJavaMethodBreakpoint) breakpoint;
	}

	private void runToBreakpoint(String typeName, IBreakpoint mbp) throws Exception {
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(get15Project(), typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit entry breakpoint first", mbp,hit);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * @throws Exception
	 */
	public void testGenericArrayEntryBreakpoints() throws Exception {
		String typeName = "a.b.c.GenericMethodEntryTest";
		List<IJavaMethodBreakpoint> bps = new ArrayList<>();
		// func(T[] arr, int m, int n) - entry
		bps.add(createMethodBreakpoint(typeName, "func", "([Ljava/lang/Comparable;II)I", true, false));
		// func(int m, int n)
		bps.add(createMethodBreakpoint(typeName, "func", "(II)I", true, false));
		// func(T t, int m, int n)
		bps.add(createMethodBreakpoint(typeName, "func", "(Ljava/lang/Comparable;II)I", true, false));
		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit entry breakpoint first", bps.get(0), hit);

			// onto the next breakpoint

			thread = resume(thread);
			hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit exit breakpoint second", bps.get(1), hit);

			// onto the next breakpoint

			thread = resume(thread);
			hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit exit breakpoint second", bps.get(2), hit);

		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}