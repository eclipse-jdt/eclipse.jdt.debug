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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

/**
 * Tests runtime classpath entry creation/restoration.
 */
public class RuntimeClasspathEntryTests extends AbstractDebugTest {
	
	public RuntimeClasspathEntryTests(String name) {
		super(name);
	}

	public void testProjectEntry() throws Exception {
		IProject project = getJavaProject().getProject();
		IRuntimeClasspathEntry entry = JavaRuntime.newProjectRuntimeClasspathEntry(getJavaProject());
	
		assertEquals("Paths should be equal", project.getFullPath(), entry.getPath());
		assertEquals("Resources should be equal", project, entry.getResource());
		assertEquals("Should be of type project", IRuntimeClasspathEntry.PROJECT, entry.getType());
		assertEquals("Should be a user entry", IRuntimeClasspathEntry.USER_CLASSES, entry.getClasspathProperty());
		
		String memento = entry.getMemento();
		IRuntimeClasspathEntry restored = JavaRuntime.newRuntimeClasspathEntry(memento);
		assertEquals("Entries should be equal", entry, restored);
	}
	
	public void testJRELIBVariableEntry() throws Exception {
		IClasspathEntry[] cp = getJavaProject().getRawClasspath();
		IClasspathEntry cpe = null;
		for (int i = 0; i < cp.length; i++) {
			if (cp[i].getEntryKind() == IClasspathEntry.CPE_VARIABLE && cp[i].getPath().equals(new Path(JavaRuntime.JRELIB_VARIABLE))) {
				cpe = cp[i];
				break;
			}
		}
		assertNotNull("Did not find a variable entry", cpe);
		IRuntimeClasspathEntry entry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(JavaRuntime.JRELIB_VARIABLE));
		entry.setSourceAttachmentPath(cpe.getSourceAttachmentPath());
		entry.setSourceAttachmentRootPath(cpe.getSourceAttachmentRootPath());
	
		assertEquals("Paths should be equal", cpe.getPath(), entry.getPath());
		assertNull("Resource should be null", entry.getResource());
		assertEquals("Should be of type varirable", IRuntimeClasspathEntry.VARIABLE, entry.getType());
		assertEquals("Should be a standard entry", IRuntimeClasspathEntry.STANDARD_CLASSES, entry.getClasspathProperty());
		
		String memento = entry.getMemento();
		IRuntimeClasspathEntry restored = JavaRuntime.newRuntimeClasspathEntry(memento);
		assertEquals("Entries should be equal", entry, restored);
		
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		LibraryLocation[] libs = vm.getLibraryLocations();
		if (libs == null) {
			libs = vm.getVMInstallType().getDefaultLibraryLocations(vm.getInstallLocation());
		}
		assertTrue("there is at least one system lib", libs.length >= 1);
		
	}	
	
	/**
	 * Tests that a project can be launched if it contains the JRE_CONTAINER variable
	 * instead of JRE_LIB
	 * 
	 * XXX: test waiting for bug fix in JCORE - unable to bind container if there
	 * is no corresponding classpath entry.
	 */
//	public void testJREContainerEntry() throws Exception {
//		ILaunchConfiguration lc = getLaunchConfiguration("Breakpoints");
//		ILaunchConfigurationWorkingCopy wc = lc.copy("Breakpoints_JRE_CONTAINER");
//		
//		IRuntimeClasspathEntry[] cp = JavaRuntime.computeRuntimeClasspath(lc);
//		IRuntimeClasspathEntry removed = null;
//		List entries = new ArrayList(cp.length);
//		// replace JRE_LIB with JRE_CONTAINER
//		for (int i = 0; i < cp.length; i++) {
//			if (cp[i].getType() == IRuntimeClasspathEntry.VARIABLE) {
//				removed = cp[i];
//				cp[i] = JavaRuntime.newRuntimeContainerClasspathEntry(new Path(JavaRuntime.JRE_CONTAINER), getJavaProject().getElementName());	
//			}
//			entries.add(cp[i].getMemento());
//		}
//		
//		assertNotNull("Did not replace entry", removed);
//		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
//		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, entries);
//		lc = wc.doSave();
//		
//		createLineBreakpoint(52, "Breakpoints");
//		IJavaThread thread= null;
//		try {
//			thread = launch(lc);
//			assertNotNull("Launch failed", thread);
//		} finally {
//			terminateAndRemove(thread);
//			removeAllBreakpoints();
//		}
//	}	
	
}
