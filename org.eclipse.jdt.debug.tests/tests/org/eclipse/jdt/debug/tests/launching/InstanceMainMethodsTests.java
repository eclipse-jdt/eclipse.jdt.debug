/*******************************************************************************
 * Copyright (c) 2022 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.launching;

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class InstanceMainMethodsTests extends AbstractDebugTest {

	public InstanceMainMethodsTests(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return super.get21Project();
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
		assertNotNull("Missing VM process.", process);
		assertEquals("Process finished with error code", 0, process.getExitValue());
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
		assertNotNull("Missing VM process.", process);
		assertEquals("Process finished with error code", 0, process.getExitValue());
	}

}
