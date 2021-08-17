/*******************************************************************************
 * Copyright (c) 2021 Gayan Perera and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gayan Perera - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.eval;

import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class Java9Tests extends AbstractDebugTest {

	private IJavaThread thread;

	public Java9Tests(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return super.get9Project();
	}

	public void testBug575039_methodBreakpointOnJavaBaseModuleClass_expectSuccessfulEval() throws Exception {
		String type = "Bug575039";
		createMethodBreakpoint("java.lang.Thread", "<init>", "(Ljava/lang/ThreadGroup;Ljava/lang/Runnable;Ljava/lang/String;JLjava/security/AccessControlContext;Z)V",
				true, false);
		thread = launchToBreakpoint(type);
		assertNotNull("The program did not suspend", thread);

		String snippet = "name != null";
		IValue value = doEval(thread, snippet);

		assertNotNull("value is null", value);
		assertEquals("true", value.getValueString());
	}

	@Override
	protected void tearDown() throws Exception {
		removeAllBreakpoints();
		terminateAndRemove(thread);
		super.tearDown();
	}
}
