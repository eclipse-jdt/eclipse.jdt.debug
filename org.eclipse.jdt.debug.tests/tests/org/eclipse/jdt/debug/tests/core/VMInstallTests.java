/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IVMInstall;
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
        String javaVersion = def.getJavaVersion();
        assertNotNull("default VM is missing java.version", javaVersion);
	}
	
	
}
