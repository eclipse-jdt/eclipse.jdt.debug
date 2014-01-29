/*******************************************************************************
 * Copyright (c) 2014 Jesper S. Møller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     Jesper S. Møller - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.debug.tests.eval;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Group of tests that evaluate operations involving generics
 * 
 * @since 3.8
 */
public class Java8Tests extends AbstractDebugTest {

	/**
	 * @param name
	 */
	public Java8Tests(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get18Project();
	}
	
	/**
	 * Evaluates a generified snippet with a simple single 
	 * generic statement
	 * 
	 * @throws Exception
	 */
	public void testEvalDefaultMethod() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "EvalTest18";
			createLineBreakpoint(22, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "strings.stream()";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
	

}
