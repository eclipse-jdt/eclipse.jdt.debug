package org.eclipse.jdt.debug.tests.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.JREContainer;
import org.eclipse.jdt.internal.launching.JREContainerInitializer;
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
		
		public void setEntries(IClasspathEntry[] cpe) {
			entries = cpe;
		}

	}
	
	public ClasspathContainerTests(String name) {
		super(name);
	}

	/**
	 * Tests that the container will accept an update
	 */
	public void testCanUpdate() throws CoreException {
		// Create a new VM install that mirros the current install
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
		
		JREContainer container = new JREContainer(newVM, containerPath);
		JREContainerInitializer initializer = new JREContainerInitializer();
		// store the current library settings
		LibraryLocation[] originalLibs = JavaRuntime.getLibraryLocations(newVM);
		assertTrue("Libraries should not be empty", originalLibs.length > 0);
		IClasspathEntry[] originalEntries = container.getClasspathEntries();
		assertEquals("Libraries should be same size as classpath entries", originalLibs.length, originalEntries.length);
		
		// ensure we can update
		assertTrue("Initializer will not accept update", initializer.canUpdateClasspathContainer(containerPath, getJavaProject()));
		
		// update to an empty set of libs
		FakeContainer fakeContainer = new FakeContainer();
		initializer.requestClasspathContainerUpdate(containerPath, getJavaProject(), fakeContainer);
		
		// ensure the library locations are now empty on the new VM
		LibraryLocation[] newLibs = JavaRuntime.getLibraryLocations(newVM);
		assertEquals("Libraries should be empty", 0, newLibs.length);
		
		// re-set to original libs
		fakeContainer.setEntries(originalEntries);
		initializer.requestClasspathContainerUpdate(containerPath, getJavaProject(), fakeContainer);
		
		// ensure libs are restored
		newLibs = JavaRuntime.getLibraryLocations(newVM);
		assertEquals("Libraries should be restored", originalLibs.length, newLibs.length);
		for (int i = 0; i < newLibs.length; i++) {
			LibraryLocation location = newLibs[i];
			LibraryLocation origi = originalLibs[i];
			assertEquals("Library should be the eqaual", origi.getSystemLibraryPath().toFile(), location.getSystemLibraryPath().toFile());
		} 
	}
	
	
}
