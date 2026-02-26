/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILogicalStructureType;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

/**
 * Tests for logical structures
 */
public class TestLogicalStructures extends AbstractDebugTest {

	/**
	 * Constructs test.
	 *
	 * @param name test name
	 */
	public TestLogicalStructures(String name) {
		super(name);
	}

	/**
	 * Test the logical structure for a list.
	 */
	public void testListLogicalStructure() throws Exception {
		String typeName = "LogicalStructures";
		createLineBreakpoint(32, typeName);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);

			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("missing top frame", frame);

			IJavaVariable variable = frame.findVariable("list");
			assertNotNull("Missing variable 'list'", variable);

			IValue value = variable.getValue();
			ILogicalStructureType[] types = DebugPlugin.getLogicalStructureTypes(value);
			assertEquals("Should be one logical structure type", 1, types.length);

			IJavaObject logicalValue = (IJavaObject) types[0].getLogicalStructure(value);
			gcInSnippet(frame);
			assertEquals("Logical value should be an array", "java.lang.Object[]", logicalValue.getJavaType().getName());

			IJavaArray array = (IJavaArray) logicalValue;
			assertEquals("Should be two elements in the structure", 2, array.getLength());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Test the logical structure for a map.
	 */
	public void testMapLogicalStructure() throws Exception {
		String typeName = "LogicalStructures";
		createLineBreakpoint(32, typeName);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);

			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("missing top frame", frame);

			IJavaVariable variable = frame.findVariable("map");
			assertNotNull("Missing variable 'map'", variable);

			IValue value = variable.getValue();
			ILogicalStructureType[] types = DebugPlugin.getLogicalStructureTypes(value);
			assertEquals("Should be one logical structure type", 1, types.length);

			IJavaObject logicalValue = (IJavaObject) types[0].getLogicalStructure(value);
			gcInSnippet(frame);
			assertEquals("Logical value should be an array", "java.lang.Object[]", logicalValue.getJavaType().getName());

			IJavaArray array = (IJavaArray) logicalValue;
			assertEquals("Should be two elements in the structure", 2, array.getLength());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Test the logical structure for a map entry.
	 */
	public void testEntryLogicalStructure() throws Exception {
		String typeName = "LogicalStructures";
		createLineBreakpoint(32, typeName);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);

			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("missing top frame", frame);

			IJavaVariable variable = frame.findVariable("entry");
			assertNotNull("Missing variable 'entry'", variable);

			IValue value = variable.getValue();
			ILogicalStructureType[] types = DebugPlugin.getLogicalStructureTypes(value);
			assertEquals("Should be one logical structure type", 1, types.length);

			IJavaObject logicalValue = (IJavaObject) types[0].getLogicalStructure(value);
			gcInSnippet(frame);
			IVariable[] children = logicalValue.getVariables();
			assertEquals("Should be two elements in the structure", 2, children.length);
			assertEquals("First entry should be key", "key", children[0].getName());
			assertEquals("Second entry should be value", "value", children[1].getName());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	private void gcInSnippet(IJavaStackFrame stackFrame) throws CoreException, InterruptedException {
		IAstEvaluationEngine engine = JDIDebugPlugin.getDefault().getEvaluationEngine(getProjectContext(), (IJavaDebugTarget) stackFrame.getDebugTarget());
		EvaluationListener listener = new EvaluationListener();
		engine.evaluate("generateGarbageAndGC()", stackFrame, listener, DebugEvent.EVALUATION_IMPLICIT, false);
		listener.await();

	}

	private static class EvaluationListener implements IEvaluationListener {

		private final AtomicBoolean done = new AtomicBoolean(false);
		private final StringBuilder errors = new StringBuilder();

		@Override
		public void evaluationComplete(IEvaluationResult result) {
			if (result.hasErrors()) {
				errors.append("Evaluation resulted in errors:");
				errors.append(System.lineSeparator());
				for (String error : result.getErrorMessages()) {
					errors.append(error);
					errors.append(System.lineSeparator());
				}
				DebugException exception = result.getException();
				if (exception != null) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					exception.printStackTrace(pw);
					errors.append(sw.getBuffer().toString());
					errors.append(System.lineSeparator());
				}
			}
			done.set(true);

		}

		private void await() throws InterruptedException {
			long start = System.currentTimeMillis();
			while (!done.get() && System.currentTimeMillis() - start <= DEFAULT_TIMEOUT) {
				Thread.sleep(10);
			}
			if (!done.get()) {
				fail("Timeout occurred while waiting on evaluation result");
			}
			if (!errors.isEmpty()) {
				fail(errors.toString());
			}
		}
	}
}
