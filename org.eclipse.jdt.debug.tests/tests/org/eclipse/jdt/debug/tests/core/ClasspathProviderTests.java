package org.eclipse.jdt.debug.tests.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
*********************************************************************/

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathProvider;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Tests runtime classpath provider extension point
 */
public class ClasspathProviderTests extends AbstractDebugTest {
	
	public ClasspathProviderTests(String name) {
		super(name);
	}

	public void testEmptyProvider() throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration("Breakpoints");
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, "org.eclipse.jdt.debug.tests.EmptyClasspathProvider");
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, "org.eclipse.jdt.debug.tests.EmptyClasspathProvider");
		
		IRuntimeClasspathProvider cpProvider = JavaRuntime.getClasspathProvider(wc);
		IRuntimeClasspathProvider spProvider = JavaRuntime.getSourceLookupPathProvider(wc);
		
		assertNotNull("Did not retrieve classpath provider", cpProvider);
		assertNotNull("Did not retrieve source path provider", spProvider);
		
		assertEquals("Classpath should be empty", 0, cpProvider.computeUnresolvedClasspath(config).length);
		assertEquals("Source path should be empty", 0, spProvider.computeUnresolvedClasspath(config).length);
	}
	
	/**
	 * Test that a variable set to the location of an archive resolves properly.
	 */
	public void testVariableArchiveResolution() throws Exception {
		IResource archive = getJavaProject().getProject().getFolder("src").getFile("A.jar");
		assertTrue("Archive does not exist", archive.exists());
		String varName = "COMPLETE_ARCHIVE";
		JavaCore.setClasspathVariable(varName, archive.getFullPath(), null);

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(varName));
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, getJavaProject());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
	}
	
	/**
	 * Test that a variable set to the location of an archive via variable
	 * extension resolves properly.
	 */
	public void testVariableExtensionResolution() throws Exception {
		IResource archive = getJavaProject().getProject().getFolder("src").getFile("A.jar");
		IProject root = getJavaProject().getProject();
		String varName = "RELATIVE_ARCHIVE";
		JavaCore.setClasspathVariable(varName, root.getFullPath(), null);

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(varName).append(new Path("src")).append(new Path("A.jar")));
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, getJavaProject());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
	}	
}
