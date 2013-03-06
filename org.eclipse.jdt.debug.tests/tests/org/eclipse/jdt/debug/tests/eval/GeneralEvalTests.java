/*******************************************************************************
 * Copyright (c) Mar 6, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.eval;

import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests that evaluations in non-generified source
 * 
 * @since 3.8.100
 */
public class GeneralEvalTests extends AbstractDebugTest {

	
	/**
	 * @param name
	 */
	public GeneralEvalTests(String name) {
		super(name);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
	public void testInnerType1() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug329294";
			createLineBreakpoint(22, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "inner";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
	public void testInnerType2() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug329294";
			createLineBreakpoint(26, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "fInner1.innerBool";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
	public void testInnerType3() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug329294";
			createLineBreakpoint(30, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "!fInner1.innerBool";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
	public void testInnerAnonymousType() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug329294";
			createLineBreakpoint(7, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "fInner1.innerBool";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
}
