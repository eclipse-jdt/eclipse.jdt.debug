/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
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
		IProject project = get14Project().getProject();
		IRuntimeClasspathEntry entry = JavaRuntime.newProjectRuntimeClasspathEntry(get14Project());

		assertEquals("Paths should be equal", project.getFullPath(), entry.getPath());
		assertEquals("Resources should be equal", project, entry.getResource());
		assertEquals("Should be of type project", IRuntimeClasspathEntry.PROJECT, entry.getType());
		assertEquals("Should be a user entry", IRuntimeClasspathEntry.USER_CLASSES, entry.getClasspathProperty());

		String memento = entry.getMemento();
		IRuntimeClasspathEntry restored = JavaRuntime.newRuntimeClasspathEntry(memento);
		assertEquals("Entries should be equal", entry, restored);
	}

	public void testJRELIBVariableEntry() throws Exception {
		IClasspathEntry cpe = JavaCore.newVariableEntry(new Path(JavaRuntime.JRELIB_VARIABLE), new Path(JavaRuntime.JRESRC_VARIABLE), new Path(JavaRuntime.JRESRCROOT_VARIABLE));

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
	 *//*
	public void testJREContainerEntry() throws Exception {
		ILaunchConfiguration lc = getLaunchConfiguration("Breakpoints");
		ILaunchConfigurationWorkingCopy wc = lc.copy("Breakpoints_JRE_CONTAINER");

		IRuntimeClasspathEntry[] cp = JavaRuntime.getClasspathProvider(lc).computeUnresolvedClasspath(lc);
		IRuntimeClasspathEntry removed = null;
		List entries = new ArrayList(cp.length);
		// replace JRE_LIB with JRE_CONTAINER
		for (int i = 0; i < cp.length; i++) {
			if (cp[i].getType() == IRuntimeClasspathEntry.VARIABLE) {
				removed = cp[i];
				cp[i] = JavaRuntime.newRuntimeContainerClasspathEntry(new Path(JavaRuntime.JRE_CONTAINER), IRuntimeClasspathEntry.STANDARD_CLASSES);
			}
			entries.add(cp[i].getMemento());
		}

		assertNotNull("Did not replace entry", removed);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, entries);
		lc = wc.doSave();

		createLineBreakpoint(52, "Breakpoints");
		IJavaThread thread= null;
		try {
			thread = launchToBreakpoint(lc);
			assertNotNull("Launch failed", thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}	*/

	public void testJREContainerEquality() throws Exception {
		IRuntimeClasspathEntry entry1 = JavaRuntime.newRuntimeContainerClasspathEntry(new Path(JavaRuntime.JRE_CONTAINER), IRuntimeClasspathEntry.STANDARD_CLASSES, get14Project());
		IRuntimeClasspathEntry entry2 = JavaRuntime.newRuntimeContainerClasspathEntry(new Path(JavaRuntime.JRE_CONTAINER), IRuntimeClasspathEntry.STANDARD_CLASSES, getMultiOutputProject());
		assertEquals("JRE containers should be equal no matter which project", entry1, entry2);
	}

	public void testExampleContainerEqualityNegative() throws Exception {
		IRuntimeClasspathEntry entry1 = JavaRuntime.newRuntimeContainerClasspathEntry(new Path("org.eclipse.jdt.debug.tests.TestClasspathContainer"), IRuntimeClasspathEntry.USER_CLASSES, get14Project());
		IRuntimeClasspathEntry entry2 = JavaRuntime.newRuntimeContainerClasspathEntry(new Path("org.eclipse.jdt.debug.tests.TestClasspathContainer"), IRuntimeClasspathEntry.USER_CLASSES, getMultiOutputProject());
		assertFalse("Example containers should *not* be equal for different projects", entry1.equals(entry2));
	}

	public void testExampleContainerEqualityPositive() throws Exception {
		IRuntimeClasspathEntry entry1 = JavaRuntime.newRuntimeContainerClasspathEntry(new Path("org.eclipse.jdt.debug.tests.TestClasspathContainer"), IRuntimeClasspathEntry.USER_CLASSES, get14Project());
		IRuntimeClasspathEntry entry2 = JavaRuntime.newRuntimeContainerClasspathEntry(new Path("org.eclipse.jdt.debug.tests.TestClasspathContainer"), IRuntimeClasspathEntry.USER_CLASSES, get14Project());
		assertEquals("Example containers should be equal for same project", entry1, entry2);
	}
}
