/*******************************************************************************
 *  Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.index.JavaIndexer;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
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
		@Override
		public IClasspathEntry[] getClasspathEntries() {
			return entries;
		}

		/**
		 * @see org.eclipse.jdt.core.IClasspathContainer#getDescription()
		 */
		@Override
		public String getDescription() {
			return "Fake";
		}

		/**
		 * @see org.eclipse.jdt.core.IClasspathContainer#getKind()
		 */
		@Override
		public int getKind() {
			return IClasspathContainer.K_DEFAULT_SYSTEM;
		}

		/**
		 * @see org.eclipse.jdt.core.IClasspathContainer#getPath()
		 */
		@Override
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

		JREContainer container = new JREContainer(newVM, containerPath, get14Project());
		JREContainerInitializer initializer = new JREContainerInitializer();
		// store the current library settings
		LibraryLocation[] originalLibs = JavaRuntime.getLibraryLocations(newVM);
		assertTrue("Libraries should not be empty", originalLibs.length > 0);
		IClasspathEntry[] originalEntries = container.getClasspathEntries();
		assertEquals("Libraries should be same size as classpath entries", originalLibs.length, originalEntries.length);

		// ensure we can update
		assertTrue("Initializer will not accept update", initializer.canUpdateClasspathContainer(containerPath, get14Project()));

		// update to an empty set of libraries
		FakeContainer fakeContainer = new FakeContainer();
		initializer.requestClasspathContainerUpdate(containerPath, get14Project(), fakeContainer);

		// ensure the library locations are now empty on the new VM
		LibraryLocation[] newLibs = JavaRuntime.getLibraryLocations(newVM);
		assertEquals("Libraries should be empty", 0, newLibs.length);

		// re-set to original libraries
		fakeContainer.setEntries(originalEntries);
		initializer.requestClasspathContainerUpdate(containerPath, get14Project(), fakeContainer);

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

	/**
	 * Tests that an index can be added to a {@link LibraryLocation}
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=399098
	 * @since 3.8.100
	 */
	public void testJREContainerIndex() throws Exception {
		// get the current VM
		IVMInstall def = JavaRuntime.getDefaultVMInstall();
		if (JavaRuntime.isModularJava(def)) {
			return;
		}
		LibraryLocation[] libs = JavaRuntime.getLibraryLocations(def);
		// generate an index for the first library location only (to save time - do not need an index for all libraries)
		URL indexURL = this.getIndexForLibrary(libs[0]);
		// clone the current VM, but only add the one library that was indexed
		VMStandin newVMStandin = new VMStandin(def.getVMInstallType(), "index.jre");
		newVMStandin.setName("JRE with pre-built index");
		newVMStandin.setInstallLocation(def.getInstallLocation());
		final LibraryLocation[] newLibs = new LibraryLocation[1];
		newLibs[0] = new LibraryLocation(libs[0].getSystemLibraryPath(), libs[0].getSystemLibrarySourcePath(), libs[0].getPackageRootPath(), libs[0].getJavadocLocation(), indexURL);
		newVMStandin.setLibraryLocations(newLibs);
		IVMInstall newVM = newVMStandin.convertToRealVM();
		// make sure the index URL is correct
		assertEquals(indexURL, newVM.getLibraryLocations()[0].getIndexLocation());
		// create the JRE container
		IPath containerPath = new Path(JavaRuntime.JRE_CONTAINER);
		JREContainer container = new JREContainer(newVM, containerPath, get14Project());
		IClasspathEntry[] originalEntries = container.getClasspathEntries();
		// There should only be 1 classpath entry (the 1 library added)
		assertEquals(1, originalEntries.length);
		IClasspathAttribute[] cpAttributes = originalEntries[0].getExtraAttributes();
		String indexloc = null;
		for (int i = 0; i < cpAttributes.length; i++) {
			if(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME.equals(cpAttributes[i].getName())) {
				indexloc = cpAttributes[i].getValue();
				break;
			}
		}
		assertNotNull("The index_location classpath attribue was not found", indexloc);
		// and it should have a value of the original indexURL in its string form.
		assertEquals(indexURL.toString(), indexloc);
	}

	/**
	 * Tests that an index can be added to a {@link LibraryLocation} and that successive calls to
	 * {@link JavaRuntime#getLibraryLocations(IVMInstall)} does not erase the index infos
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=399098
	 * @since 3.8.100
	 */
	public void testJREContainerIndex2() throws Exception {
		// get the current VM
		IVMInstall def = JavaRuntime.getDefaultVMInstall();
		if (JavaRuntime.isModularJava(def)) {
			return;
		}
		LibraryLocation[] libs = JavaRuntime.getLibraryLocations(def);
		// generate an index for the first library location only (to save time - do not need an index for all libraries)
		URL indexURL = this.getIndexForLibrary(libs[0]);
		// clone the current VM, but only add the one library that was indexed
		VMStandin newVMStandin = new VMStandin(def.getVMInstallType(), "index.jre");
		newVMStandin.setName("JRE with pre-built index");
		newVMStandin.setInstallLocation(def.getInstallLocation());
		final LibraryLocation[] newLibs = new LibraryLocation[1];
		newLibs[0] = new LibraryLocation(libs[0].getSystemLibraryPath(), libs[0].getSystemLibrarySourcePath(), libs[0].getPackageRootPath(), libs[0].getJavadocLocation(), indexURL);
		newVMStandin.setLibraryLocations(newLibs);
		IVMInstall newVM = newVMStandin.convertToRealVM();
		libs = JavaRuntime.getLibraryLocations(newVM);
		assertNotNull("There should be an index file for: "+libs[0].getSystemLibraryPath(), libs[0].getIndexLocation());
	}

	/**
	 * Generates and returns a {@link URL} to the index file for a given {@link LibraryLocation} into the state location
	 */
	private URL getIndexForLibrary(final LibraryLocation library) throws IOException, MalformedURLException {

		// construct the path to the index file
		File libraryFile = library.getSystemLibraryPath().toFile();
		String indexName = libraryFile.getName() + ".index";
		IPath indexPath = JavaTestPlugin.getDefault().getStateLocation().append(indexName);
		// generate the index using JDT Core API
		JavaIndexer.generateIndexForJar(libraryFile.getAbsolutePath(), indexPath.toOSString());
		// return the index URL
		URL indexURL = indexPath.toFile().toURI().toURL();
		return indexURL;
	}
}