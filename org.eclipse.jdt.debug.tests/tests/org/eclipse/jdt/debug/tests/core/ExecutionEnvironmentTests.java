/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;

/**
 * Tests for execution environments
 */
public class ExecutionEnvironmentTests extends AbstractDebugTest {
		
	public ExecutionEnvironmentTests(String name) {
		super(name);
	}
	
	public void testGetEnvironments() throws Exception {
		IExecutionEnvironment[] executionEnvironments = JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments();
		assertTrue("Should be at least one environment", executionEnvironments.length > 0);
		for (int i = 0; i < executionEnvironments.length; i++) {
			IExecutionEnvironment environment = executionEnvironments[i];
			if (environment.getId().equals("org.eclipse.jdt.debug.tests.environment.j2se14x")) {
				return;
			}
		}
		assertTrue("Did not find test environment org.eclipse.jdt.debug.tests.environment.j2se14x", false);
	}
	
	public void testAnalyze() throws Exception {
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		
		IExecutionEnvironment[] environments = manager.getEnvironments(vm);
		assertTrue("Should be at least one environmet", environments.length > 0);
		
		IExecutionEnvironment environment = manager.getEnvironment("org.eclipse.jdt.debug.tests.environment.j2se14x");
		IVMInstall[] installs = manager.getVMInstalls(environment);
		assertTrue("Should be at least one vm install for the environment", installs.length > 0);
		for (int i = 0; i < installs.length; i++) {
			IVMInstall install = installs[i];
			if (install.equals(vm)) {
				return;
			}
		}
		assertTrue("vm should be j2se14x", false);
	}
}
