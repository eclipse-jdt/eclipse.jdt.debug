/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.ILibraryLocationResolver;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstall3;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

/**
 * Tests for installed VMs
 */
public class VMInstallTests extends AbstractDebugTest {
		
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
	 * Test the new support for {@link ILibraryLocationResolver}s
	 * 
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=399798
	 * @throws Exception
	 */
	public void testLibraryResolver1() throws Exception { 
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		assertNotNull("There must be a default VM", vm);
		LibraryLocation[] locs = JavaRuntime.getLibraryLocations(vm);
		assertNotNull("there must be some default library locations", locs);
		//try to find an 'ext' dir to see if it has some lib infos
		//the test resolver sets a source path of ../test_resolver_src.zip on
		//any libs in the ext dir
		String locpath = null;
		for (int i = 0; i < locs.length; i++) {
			locpath = locs[i].getSystemLibraryPath().toString();
			if(locpath.indexOf("ext") > -1) {
				assertTrue("There should be a source path ending in test_resolver_src.zip on the ext lib ["+locpath+"]", 
						locpath.indexOf("test_resolver_src.zip") > -1);
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
	 * Generates a key used to cache system property for this VM in this plug-ins
	 * preference store.
	 * 
	 * @param property system property name
	 * @return preference store key
	 */
	private String getSystemPropertyKey(IVMInstall vm, String property) {
		StringBuffer buffer = new StringBuffer();
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