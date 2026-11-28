/*******************************************************************************
 * Copyright (c) 2007, 2026 IBM Corporation and others.
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
import org.eclipse.debug.ui.actions.ToggleBreakpointAction;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.debug.tests.VerticalRulerInfoStub;
import org.eclipse.jdt.debug.tests.ui.AbstractDebugUiTests;
import org.eclipse.ui.IEditorPart;

/**
 * Tests method breakpoints for 1.5 source code.
 */
public class MethodBreakpointTests15 extends AbstractDebugUiTests {

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
		// breakpointAdded fires before the toggle job finishes setting up the breakpoint
		TestUtil.waitForJobs(getName(), 100, DEFAULT_TIMEOUT);
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

	/** Line of the <code>methodTypeParameter</code> declaration in {@link #getTypeName()} */
	private static final int METHOD_LINE = 37;

	/** Line of the closing brace of <code>methodTypeParameter</code> in {@link #getTypeName()} */
	private static final int CLOSING_BRACE_LINE = 39;

	/**
	 * Toggling on the closing brace creates an exit only breakpoint, located on the closing brace line.
	 */
	public void testToggleOnClosingBraceCreatesExitOnlyBreakpoint() throws Exception {
		try {
			IJavaMethodBreakpoint breakpoint = createBreakpoint(CLOSING_BRACE_LINE);
			assertFalse("Should not be an entry breakpoint", breakpoint.isEntry());
			assertTrue("Should be an exit breakpoint", breakpoint.isExit());
			assertEquals("Wrong line", CLOSING_BRACE_LINE, breakpoint.getLineNumber());
		} finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Toggling on the closing brace of an entry breakpoint adds exit to the same breakpoint, which stays on the method line. Toggling again
	 * removes exit.
	 */
	public void testToggleOnClosingBraceOfEntryBreakpoint() throws Exception {
		try {
			IJavaMethodBreakpoint breakpoint = createBreakpoint(METHOD_LINE);
			assertTrue("Should be an entry breakpoint", breakpoint.isEntry());
			assertFalse("Should not be an exit breakpoint", breakpoint.isExit());
			IEditorPart editor = openEditor();

			toggle(editor, CLOSING_BRACE_LINE);
			assertSame("Breakpoint should be kept", breakpoint, getSingleBreakpoint());
			assertTrue("Should be an entry breakpoint", breakpoint.isEntry());
			assertTrue("Should be an exit breakpoint", breakpoint.isExit());
			assertEquals("Should stay on the method line", -1, breakpoint.getLineNumber());

			toggle(editor, CLOSING_BRACE_LINE);
			assertSame("Breakpoint should be kept", breakpoint, getSingleBreakpoint());
			assertTrue("Should be an entry breakpoint", breakpoint.isEntry());
			assertFalse("Should not be an exit breakpoint", breakpoint.isExit());
		} finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Toggling on the method line of an exit only breakpoint adds entry to the same breakpoint and moves it to the method line.
	 */
	public void testToggleOnMethodLineOfExitOnlyBreakpoint() throws Exception {
		try {
			IJavaMethodBreakpoint breakpoint = createBreakpoint(CLOSING_BRACE_LINE);
			assertFalse("Should not be an entry breakpoint", breakpoint.isEntry());
			assertTrue("Should be an exit breakpoint", breakpoint.isExit());

			toggle(openEditor(), METHOD_LINE);
			assertSame("Breakpoint should be kept", breakpoint, getSingleBreakpoint());
			assertTrue("Should be an entry breakpoint", breakpoint.isEntry());
			assertTrue("Should be an exit breakpoint", breakpoint.isExit());
			assertEquals("Should be on the method line", -1, breakpoint.getLineNumber());
		} finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Toggling on the closing brace of an exit only breakpoint removes it.
	 */
	public void testToggleOnClosingBraceOfExitOnlyBreakpoint() throws Exception {
		try {
			createBreakpoint(CLOSING_BRACE_LINE);

			toggle(openEditor(), CLOSING_BRACE_LINE);
			assertEquals("Breakpoint should be removed", 0, getBreakpointManager().getBreakpoints(JDIDebugModel.getPluginIdentifier()).length);
		} finally {
			removeAllBreakpoints();
		}
	}

	private IEditorPart openEditor() throws Exception {
		IType type = get15Project().findType(getTypeName());
		assertNotNull("Missing file", type);
		IResource resource = type.getResource();
		assertTrue("Missing file", resource instanceof IFile);
		return openEditor((IFile) resource);
	}

	/**
	 * Toggles at the given line (1 based) and waits for the asynchronous toggle job.
	 */
	private void toggle(IEditorPart editor, int line) throws Exception {
		sync(() -> new ToggleBreakpointAction(editor, null, new VerticalRulerInfoStub(line - 1)).run());
		TestUtil.waitForJobs(getName(), 100, DEFAULT_TIMEOUT);
	}

	private IBreakpoint getSingleBreakpoint() {
		IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints(JDIDebugModel.getPluginIdentifier());
		assertEquals("Wrong number of breakpoints", 1, breakpoints.length);
		return breakpoints[0];
	}
}