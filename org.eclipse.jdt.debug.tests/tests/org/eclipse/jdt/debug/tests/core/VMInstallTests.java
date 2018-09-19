/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.ILibraryLocationResolver;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstall3;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;

/**
 * Tests for installed VMs
 */
public class VMInstallTests extends AbstractDebugTest {

	public VMInstallTests() {
		super("VM Install tests");
	}

	/**
	 * Constructor
	 * @param name the name of the test
	 */
	public VMInstallTests(String name) {
		super(name);
	}

	/**
	 * Tests the java version from the VMInstall
	 */
	public void testJavaVersion() {
		IVMInstall def = JavaRuntime.getDefaultVMInstall();
		assertTrue("should be an IVMInstall2", def instanceof IVMInstall2);
		IVMInstall2 vm2 = (IVMInstall2)def;
        String javaVersion = vm2.getJavaVersion();
        assertNotNull("default VM is missing java.version", javaVersion);
	}

	/**
	 * Test acquiring the set of system properties
	 * @throws CoreException
	 */
	public void testSystemProperties() throws CoreException {
		IVMInstall def = JavaRuntime.getDefaultVMInstall();
		assertTrue("should be an IVMInstall3", def instanceof IVMInstall3);
		IVMInstall3 vm3 = (IVMInstall3)def;
		Map<String, String> map = vm3.evaluateSystemProperties(new String[]{"user.home"}, new NullProgressMonitor());
		assertNotNull("No system properties returned", map);
		assertEquals("Wrong number of properties", 1, map.size());
		String value = map.get("user.home");
		assertNotNull("missing user.home", value);
	}

	/**
	 * Test acquiring the set of system properties that have been asked for - they should be cached in JDT launching
	 * @throws CoreException
	 */
	public void testSystemPropertiesCaching() throws CoreException {
		IVMInstall def = JavaRuntime.getDefaultVMInstall();
		assertTrue("should be an IVMInstall3", def instanceof IVMInstall3);
		IVMInstall3 vm3 = (IVMInstall3)def;
		Map<String, String> map = vm3.evaluateSystemProperties(new String[]{"user.home"}, new NullProgressMonitor());
		assertNotNull("No system properties returned", map);
		assertEquals("Wrong number of properties", 1, map.size());
		String value = map.get("user.home");
		assertNotNull("missing user.home", value);
		//check the prefs
		String key = getSystemPropertyKey(def, "user.home");
		value = Platform.getPreferencesService().getString(
				LaunchingPlugin.ID_PLUGIN,
				key,
				null,
				null);
		assertNotNull("'user.home' system property should be cached", value);
	}

	/**
	 * Tests the new support for {@link ILibraryLocationResolver}s asking for {@link LibraryLocation}s
	 * using the {@link JavaRuntime#getLibraryLocations(IVMInstall)}s API
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=399798
	 * @throws Exception
	 */
	public void testLibraryResolver1() throws Exception {
		VMInstallTestsLibraryLocationResolver.isTesting = true;
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		assertNotNull("There must be a default VM", vm);

		//invalidate it, causing a reset, then collect it again
		vm.getVMInstallType().disposeVMInstall(vm.getId());
		vm = JavaRuntime.getDefaultVMInstall();
		assertNotNull("There must be a default VM after a reset", vm);
		try {
			LibraryLocation[] locs = JavaRuntime.getLibraryLocations(vm);
			assertNotNull("there must be some default library locations", locs);
			assertResolvedLibraryLocations(locs);
		}
		finally {
			VMInstallTestsLibraryLocationResolver.isTesting = false;
			//force a re-compute to remove the bogus paths
			vm.getVMInstallType().disposeVMInstall(vm.getId());
		}
	}

	/**
	 * Tests the {@link ILibraryLocationResolver} asking for libs using an EE description file
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=399798
	 * @throws Exception
	 */
	public void testLibraryResolver2() throws Exception {
		VMInstallTestsLibraryLocationResolver.isTesting = true;
		try {
			String filename = "/testfiles/test-jre/bin/test-resolver.ee";
			if(Platform.OS_WIN32.equals(Platform.getOS())) {
				filename = "/testfiles/test-jre/bin/test-resolver-win32.ee";
			}
			VMStandin vm = getEEStandin(filename);
			IVMInstall install = vm.convertToRealVM();
			LibraryLocation[ ] locs = install.getLibraryLocations();
			assertResolvedLibraryLocations(locs);
		}
		finally {
			VMInstallTestsLibraryLocationResolver.isTesting = false;
		}
	}

	/**
	 * Tests the {@link ILibraryLocationResolver} asking for libs directly from the backing type of the {@link IVMInstall}
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=399798
	 * @throws Exception
	 */
	public void testLibraryResolver3() throws Exception {
		VMInstallTestsLibraryLocationResolver.isTesting = true;
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		assertNotNull("There must be a default VM", vm);
		try {
			//reset it
			vm.getVMInstallType().disposeVMInstall(vm.getId());
			vm = JavaRuntime.getDefaultVMInstall();
			assertNotNull("There must be a default VM", vm);

			LibraryLocation[] locs = vm.getVMInstallType().getDefaultLibraryLocations(vm.getInstallLocation());
			assertResolvedLibraryLocations(locs);
		}
		finally {
			VMInstallTestsLibraryLocationResolver.isTesting = false;
			vm.getVMInstallType().disposeVMInstall(vm.getId());
		}
	}

	/**
	 * Tests the {@link ILibraryLocationResolver} asking for libs using an EE description file that provides
	 * a source path for the ext dirs does *not* get overridden by the resolver
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=399798
	 * @throws Exception
	 */
	public void testLibraryResolver4() throws Exception {
		VMInstallTestsLibraryLocationResolver.isTesting = true;
		try {
			String filename = "/testfiles/test-jre/bin/test-resolver2.ee";
			if(Platform.OS_WIN32.equals(Platform.getOS())) {
				filename = "/testfiles/test-jre/bin/test-resolver-win32-2.ee";
			}
			VMStandin vm = getEEStandin(filename);
			IVMInstall install = vm.convertToRealVM();
			LibraryLocation[ ] locs = install.getLibraryLocations();
			String locpath = null;
			for (int i = 0; i < locs.length; i++) {
				IPath path = locs[i].getSystemLibraryPath();
				if(VMInstallTestsLibraryLocationResolver.applies(path)) {
					locpath = path.toString();
					assertTrue("The original source path should be set on the ext lib [" + locpath + "]",
							locs[i].getSystemLibrarySourcePath().toString().indexOf("source.txt") > -1);
				}
			}
		}
		finally {
			VMInstallTestsLibraryLocationResolver.isTesting = false;
		}
	}

	/**
	 * Checks the given {@link LibraryLocation}s to ensure they reference the testing resolver paths
	 *
	 * @param locs
	 */
	void assertResolvedLibraryLocations(LibraryLocation[] locs) {
		String locpath = null;
		for (int i = 0; i < locs.length; i++) {
			IPath path = locs[i].getSystemLibraryPath();
			if(VMInstallTestsLibraryLocationResolver.applies(path)) {
				locpath = path.toString();
				assertTrue("There should be a source path ending in test_resolver_src.zip on the ext lib [" + locpath + "]",
						locs[i].getSystemLibrarySourcePath().toString().indexOf("test_resolver_src.zip") > -1);
				IPath root = locs[i].getPackageRootPath();
				assertTrue("The source root path should be 'src' for ext lib ["+locpath+"]", root.toString().equals("src"));
				URL url = locs[i].getJavadocLocation();
				assertNotNull("There should be a Javadoc URL set for ext lib ["+locpath+"]", url);
				assertTrue("There should be a javadoc path of test_resolver_javadoc.zip on the ext lib ["+locpath+"]",
						url.getPath().indexOf("test_resolver_javadoc.zip") > -1);
				url = locs[i].getIndexLocation();
				assertNotNull("There should be an index path of test_resolver_index.index on the ext lib ["+locpath+"]", url);
				assertTrue("There should be an index path of test_resolver_index.index on the ext lib ["+locpath+"]",
						url.getPath().indexOf("test_resolver_index.index") > -1);
			}
		}
	}

	/**
	 * Creates a {@link VMStandin} for the given EE file. Does not return <code>null</code>
	 * @param filename
	 * @return the {@link VMStandin}
	 * @throws CoreException
	 */
	VMStandin getEEStandin(String filename) throws CoreException {
		File ee = JavaTestPlugin.getDefault().getFileInPlugin(new Path(filename));
		assertNotNull("The EE file "+filename+" was not found", ee);
		VMStandin vm = JavaRuntime.createVMFromDefinitionFile(ee, "resolver-ee", "resolver-ee-id");
		assertNotNull("the VM standin should exist for "+filename, vm);
		return vm;
	}

	/**
	 * Generates a key used to cache system property for this VM in this plug-ins
	 * preference store.
	 *
	 * @param property system property name
	 * @return preference store key
	 */
	private String getSystemPropertyKey(IVMInstall vm, String property) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("PREF_VM_INSTALL_SYSTEM_PROPERTY");
		buffer.append("."); //$NON-NLS-1$
		buffer.append(vm.getVMInstallType().getId());
		buffer.append("."); //$NON-NLS-1$
		buffer.append(vm.getId());
		buffer.append("."); //$NON-NLS-1$
		buffer.append(property);
		return buffer.toString();
	}

}