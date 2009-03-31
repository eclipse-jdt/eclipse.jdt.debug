/*******************************************************************************
 *  Copyright (c) 2000, 2007 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.JREContainer;
import org.eclipse.jdt.internal.launching.JREContainerInitializer;
import org.eclipse.jdt.internal.launching.JRERuntimeClasspathEntryResolver;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;

/**
 * Tests JRE classpath container
 */
public class ClasspathContainerTests extends AbstractDebugTest {
	
	class FakeContainer implements IClasspathContainer {
		
		IClasspathEntry[] entries = new IClasspathEntry[0];
		/**
		 * @see org.eclipse.jdt.core.IClasspathContainer#getClasspathEntries()
		 */
		public IClasspathEntry[] getClasspathEntries() {
			return entries;
		}

		/**
		 * @see org.eclipse.jdt.core.IClasspathContainer#getDescription()
		 */
		public String getDescription() {
			return "Fake";
		}

		/**
		 * @see org.eclipse.jdt.core.IClasspathContainer#getKind()
		 */
		public int getKind() {
			return IClasspathContainer.K_DEFAULT_SYSTEM;
		}

		/**
		 * @see org.eclipse.jdt.core.IClasspathContainer#getPath()
		 */
		public IPath getPath() {
			return new Path(JavaRuntime.JRE_CONTAINER);
		}
		
		/**
		 * @param cpe
		 */
		public void setEntries(IClasspathEntry[] cpe) {
			entries = cpe;
		}

	}
	
	/**
	 * @param name
	 */
	public ClasspathContainerTests(String name) {
		super(name);
	}

	/**
	 * Tests that the container will accept an update
	 * @throws CoreException
	 */
	public void testCanUpdate() throws CoreException {
		// Create a new VM install that mirrors the current install
		IVMInstall def = JavaRuntime.getDefaultVMInstall();
		String vmId = def.getId() + System.currentTimeMillis();
		VMStandin standin = new VMStandin(def.getVMInstallType(), vmId);
		String vmName = "Alternate JRE";
		IPath containerPath = new Path(JavaRuntime.JRE_CONTAINER);
		containerPath = containerPath.append(new Path(def.getVMInstallType().getId()));
		containerPath = containerPath.append(new Path(vmName));
		standin.setName(vmName);
		standin.setInstallLocation(def.getInstallLocation());
		standin.setJavadocLocation(def.getJavadocLocation());
		standin.setLibraryLocations(JavaRuntime.getLibraryLocations(def));
		standin.convertToRealVM();
		
		// ensure the new VM exists
		IVMInstall newVM = def.getVMInstallType().findVMInstall(vmId);
		assertNotNull("Failed to create new VM", newVM);
		
		JREContainer container = new JREContainer(newVM, containerPath, getJavaProject());
		JREContainerInitializer initializer = new JREContainerInitializer();
		// store the current library settings
		LibraryLocation[] originalLibs = JavaRuntime.getLibraryLocations(newVM);
		assertTrue("Libraries should not be empty", originalLibs.length > 0);
		IClasspathEntry[] originalEntries = container.getClasspathEntries();
		assertEquals("Libraries should be same size as classpath entries", originalLibs.length, originalEntries.length);
		
		// ensure we can update
		assertTrue("Initializer will not accept update", initializer.canUpdateClasspathContainer(containerPath, getJavaProject()));
		
		// update to an empty set of libraries
		FakeContainer fakeContainer = new FakeContainer();
		initializer.requestClasspathContainerUpdate(containerPath, getJavaProject(), fakeContainer);
		
		// ensure the library locations are now empty on the new VM
		LibraryLocation[] newLibs = JavaRuntime.getLibraryLocations(newVM);
		assertEquals("Libraries should be empty", 0, newLibs.length);
		
		// re-set to original libraries
		fakeContainer.setEntries(originalEntries);
		initializer.requestClasspathContainerUpdate(containerPath, getJavaProject(), fakeContainer);
		
		// ensure libraries are restored
		newLibs = JavaRuntime.getLibraryLocations(newVM);
		assertEquals("Libraries should be restored", originalLibs.length, newLibs.length);
		for (int i = 0; i < newLibs.length; i++) {
			LibraryLocation location = newLibs[i];
			LibraryLocation origi = originalLibs[i];
			assertEquals("Library should be the equal", origi.getSystemLibraryPath().toFile(), location.getSystemLibraryPath().toFile());
		} 
	}
	
	
	/**
	 * Tests library comparison case sensitivity.
	 * 
	 * @throws CoreException
	 */
	public void testLibraryCaseSensitivity() {
		IVMInstall def = JavaRuntime.getDefaultVMInstall();
		LibraryLocation[] libs = JavaRuntime.getLibraryLocations(def);
		boolean caseSensitive = !Platform.getOS().equals(Platform.WS_WIN32);
		LibraryLocation[] set1 = new LibraryLocation[libs.length];
		LibraryLocation[] set2 = new LibraryLocation[libs.length];
		for (int i = 0; i < libs.length; i++) {
			LibraryLocation lib = libs[i];
			String s1 = lib.getSystemLibraryPath().toOSString();
			IPath p1 = new Path(s1);
			String s2 = s1.toUpperCase();
			IPath p2 = new Path(s2);
			set1[i] = new LibraryLocation(p1, null, null);
			set2[i] = new LibraryLocation(p2, null, null);
		}
		boolean equal = JRERuntimeClasspathEntryResolver.isSameArchives(set1, set2); 
		if (caseSensitive) {
			assertFalse("Libraries should *not* be equal on case sensitive platform", equal);
		} else {
			assertTrue("Libraries *should* be equal on case sensitive platform", equal);
		}
	}	
	
}
