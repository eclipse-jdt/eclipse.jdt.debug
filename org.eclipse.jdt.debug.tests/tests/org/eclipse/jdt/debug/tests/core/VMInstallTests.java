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

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstall3;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Tests for installed VMs
 */
public class VMInstallTests extends AbstractDebugTest {
		
	public VMInstallTests(String name) {
		super(name);
	}

	public void testJavaVersion() throws CoreException {
		IVMInstall def = JavaRuntime.getDefaultVMInstall();
		assertTrue("should be an IVMInstall2", def instanceof IVMInstall2);
		IVMInstall2 vm2 = (IVMInstall2)def;
        String javaVersion = vm2.getJavaVersion();
        assertNotNull("default VM is missing java.version", javaVersion);
	}
	
	public void testSystemProperties() throws CoreException {
		IVMInstall def = JavaRuntime.getDefaultVMInstall();
		assertTrue("should be an IVMInstall3", def instanceof IVMInstall3);
		IVMInstall3 vm3 = (IVMInstall3)def;
		Map map = vm3.evaluateSystemProperties(new String[]{"user.home"}, new NullProgressMonitor());
		assertNotNull("No system properties returned", map);
		assertEquals("Wrong number of properties", 1, map.size());
		String value = (String) map.get("user.home");
		assertNotNull("missing user.home", value);
	}
	
	
}
