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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

/**
 * Tests for classpath variables
 */
public class ClasspathVariableTests extends AbstractDebugTest {
		
	public ClasspathVariableTests(String name) {
		super(name);
	}

	/**
	 * Tests that we do not fail on a null variable
	 */
	public void testNullVariableResolution() throws CoreException {
		String varName = "NULL_VARIABLE";
		IRuntimeClasspathEntry entry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(varName));
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(entry, getJavaProject());
		// since the variable cannot be resolved, the result should be the same before/after
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Entries should be equal", entry, resolved[0]);
	}
	
	public void testJRELibResolution() throws CoreException {
		String varName = JavaRuntime.JRELIB_VARIABLE;
		IRuntimeClasspathEntry entry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(varName));
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(entry, getJavaProject());
		assertTrue("Should be at least one resolved entry", resolved.length > 0);
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		assertNotNull("no default JRE", vm);
		LibraryLocation[] libs = JavaRuntime.getLibraryLocations(vm);
		assertTrue("no default libs", libs.length > 0);
		assertEquals("Should resolve to location of local JRE", libs[0].getSystemLibraryPath().toOSString().toLowerCase(), resolved[0].getPath().toOSString().toLowerCase());
	}
	
	/**
	 * Test that a variable set to the location of an archive via variable
	 * extension resolves properly, with a null source attachment.
	 */
	public void testVariableExtensionWithNullSourceAttachment() throws Exception {
		IResource archive = getJavaProject().getProject().getFolder("src").getFile("A.jar");
		IProject root = getJavaProject().getProject();
		String varName = "RELATIVE_ARCHIVE";
		JavaCore.setClasspathVariable(varName, root.getFullPath(), null);

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(varName).append(new Path("src")).append(new Path("A.jar")));
		runtimeClasspathEntry.setSourceAttachmentPath(new Path("NULL_VARIABLE"));
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, getJavaProject());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
		assertNull("Should be null source attachment", resolved[0].getSourceAttachmentPath());
	}	
	
	/**
	 * Test that a variable set to the location of an archive via variable
	 * extension resolves properly, with a source attachment rooted with a null
	 * variable with an extension.
	 */
	public void testVariableExtensionWithNullSourceAttachmentWithExtension() throws Exception {
		IResource archive = getJavaProject().getProject().getFolder("src").getFile("A.jar");
		IProject root = getJavaProject().getProject();
		String varName = "RELATIVE_ARCHIVE";
		JavaCore.setClasspathVariable(varName, root.getFullPath(), null);

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(varName).append(new Path("src")).append(new Path("A.jar")));
		runtimeClasspathEntry.setSourceAttachmentPath(new Path("NULL_VARIABLE").append("one").append("two"));
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, getJavaProject());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
		assertNull("Should be null source attachment", resolved[0].getSourceAttachmentPath());
	}	
}
