/*******************************************************************************
 * Copyright (c) 2011, 2015 IBM Corporation and others.
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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTEvaluationEngine;

/**
 * Tests launching / suspending and evaluating within anonymous
 * types
 */
public class TestAnonymousInspect extends AbstractDebugTest {

	static final String TYPE_NAME = "InspectTests";
	static final String SNIPPET = "getchar()";

	class Listener implements IEvaluationListener {
		IEvaluationResult fResult;

		@Override
		public void evaluationComplete(IEvaluationResult result) {
			fResult= result;
		}

		public IEvaluationResult getResult() {
			return fResult;
		}
	}
	Listener listener= new Listener();

	/**
	 * Constructor
	 */
	public TestAnonymousInspect() {
		super("TestAnonymousInspect");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.tests.AbstractDebugTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createLaunchConfiguration(get14Project(), TYPE_NAME);
	}

	/**
	 * Perform the actual evaluation (inspect)
	 * @param thread
	 * @return the result of the evaluation
	 * @throws Exception
	 */
	IValue doEval(IJavaThread thread) throws Exception{
		IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
		assertNotNull("There should be a stackframe", frame);
		ASTEvaluationEngine engine = new ASTEvaluationEngine(get14Project(), (IJavaDebugTarget) thread.getDebugTarget());
		try {
			engine.evaluate(SNIPPET, frame, listener, DebugEvent.EVALUATION_IMPLICIT, false);
			long timeout = System.currentTimeMillis()+5000;
			while(listener.fResult == null && System.currentTimeMillis() < timeout) {
				Thread.sleep(100);
			}
			assertFalse("The evaluation should not have errors", listener.fResult.hasErrors());
			return listener.fResult.getValue();
		}
		finally {
			engine.dispose();
		}
	}

	/**
	 * Tests that we can successfully inspect a method call from an anonymous type declaration that is assigned
	 * to a field
	 *
	 * @throws Exception
	 */
	public void testInspectInAnonField() throws Exception {
		IJavaThread thread = null;
		try {
			createLineBreakpoint(29, TYPE_NAME);
			thread = launchToBreakpoint(TYPE_NAME);
			assertNotNull("The application should have suspended - we cannot have a null thread", thread);
			IValue value = doEval(thread);
			assertNotNull("The evaluation result cannot be null", value);
			assertEquals("The type must be char", "char", value.getReferenceTypeName());
			assertEquals("The value must be 'a'", "a", value.getValueString());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			listener.fResult = null;
		}
	}

	/**
	 * Tests that we can successfully inspect a method call from an anonymous type declaration within a method
	 * declaration
	 *
	 * @throws Exception
	 */
	public void testInspectInAnonMethod() throws Exception {
		IJavaThread thread = null;
		try {
			createLineBreakpoint(37, TYPE_NAME);
			thread = launchToBreakpoint(TYPE_NAME);
			assertNotNull("The application should have suspended - we cannot have a null thread", thread);
			IValue value = doEval(thread);
			assertNotNull("The evaluation result cannot be null", value);
			assertEquals("The type must be char", "char", value.getReferenceTypeName());
			assertEquals("The value must be 'b'", "b", value.getValueString());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			listener.fResult = null;
		}
	}

	/**
	 * Tests that we can successfully inspect a method call from an anonymous type declaration within a static
	 * initializer
	 *
	 * @throws Exception
	 */
	public void testInspectInAnonInitializer() throws Exception {
		IJavaThread thread = null;
		try {
			createLineBreakpoint(20, TYPE_NAME);
			thread = launchToBreakpoint(TYPE_NAME);
			assertNotNull("The application should have suspended - we cannot have a null thread", thread);
			IValue value = doEval(thread);
			assertNotNull("The evaluation result cannot be null", value);
			assertEquals("The type must be char", "char", value.getReferenceTypeName());
			assertEquals("The value must be 's'", "s", value.getValueString());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			listener.fResult = null;
		}
	}
}
