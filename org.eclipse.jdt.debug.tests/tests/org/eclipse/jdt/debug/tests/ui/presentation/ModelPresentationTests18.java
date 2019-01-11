/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Paul Pazderski - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui.presentation;

import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDILambdaVariable;
import org.eclipse.jdt.internal.debug.core.model.LambdaUtils;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;

/**
 * Tests for some of the methods of the model presentation requiring/using Java 1.8 features.
 *
 * @see JDIModelPresentation
 * @see ModelPresentationTests
 */
public class ModelPresentationTests18 extends AbstractDebugTest {

	/**
	 * Constructor
	 */
	public ModelPresentationTests18() {
		super("Model Presentation tests using Java 1.8 features");
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get18Project();
	}

	/**
	 * Tests a closure/lambda variable text including variable type name.
	 *
	 * Test for Bug 542989.
	 *
	 * @throws Exception
	 */
	public void testClosureVariableText() throws Exception {
		String typeName = "ClosureVariableTest_Bug542989";
		ILineBreakpoint bp = createLineBreakpoint(18, typeName);

		JDIModelPresentation pres = new JDIModelPresentation();
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertTrue("Did not stopped in lambda context.", LambdaUtils.isLambdaFrame(frame));
			String closureVariableName = new JDILambdaVariable(null).getName();
			IVariable closure = findVariable(frame, closureVariableName);
			assertNotNull("Could not find variable '" + closureVariableName + "'", closure);

			pres.setAttribute(IDebugModelPresentation.DISPLAY_VARIABLE_TYPE_NAMES, Boolean.TRUE);
			String text = pres.getText(closure);
			assertNotNull(text);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			pres.dispose();
		}
	}
}
