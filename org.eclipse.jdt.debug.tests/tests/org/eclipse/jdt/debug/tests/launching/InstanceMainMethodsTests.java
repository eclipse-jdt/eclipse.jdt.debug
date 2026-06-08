/*******************************************************************************
 * Copyright (c) 2022, 2025 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.launching;

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.swt.widgets.Display;

public class InstanceMainMethodsTests extends AbstractDebugTest {

	public InstanceMainMethodsTests(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return super.get27Project();
	}

	public void testStaticMainWithoutArgs() throws Exception {
		String type = "Main1";
		IJavaDebugTarget target = null;
		IProcess process = null;
		try {
			target = launchAndTerminate(type);
			process = target.getProcess();
		} finally {
			if (target != null) {
				terminateAndRemove(target);
			}
		}
		waitForExit(process, 10_000L);
	}

	public void testDefaultMainWithoutArgs() throws Exception {
		String type = "Main2";
		IJavaDebugTarget target = null;
		IProcess process = null;
		try {
			target = launchAndTerminate(type);
			process = target.getProcess();
		} finally {
			if (target != null) {
				terminateAndRemove(target);
			}
		}
		waitForExit(process, 10_000L);
	}

	private static void waitForExit(IProcess process, long timeoutMs) throws Exception {
		assertNotNull("Missing VM process.", process);
		long start = System.currentTimeMillis();
		while (!process.isTerminated() && System.currentTimeMillis() - start < timeoutMs) {
			if (Display.getCurrent() != null) {
				TestUtil.runEventLoop();
			}
			Thread.sleep(50L);
		}
		assertEquals("Process finished with error code", 0, process.getExitValue());
	}

}
