/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
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
public class VMInstallTests extends AbstractDebugTest implements ILibraryLocationResolver {
	
	boolean isTesting = false;
	
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
	 * Test the new support for {@link ILibraryLocationResolver}s
	 * 
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=399798
	 * @throws Exception
	 */
	public void _testLibraryResolver1() throws Exception {
		//set this flag when testing the resolver to avoid it setting bogus
		//testing paths in a target workspace
		isTesting = true;
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		assertNotNull("There must be a default VM", vm);
		try {
			//force a re-compute
			vm.setLibraryLocations(null);
			LibraryLocation[] locs = JavaRuntime.getLibraryLocations(vm);
			assertNotNull("there must be some default library locations", locs);
			//try to find an 'ext' dir to see if it has some lib infos
			//the test resolver sets a source path of ../test_resolver_src.zip on
			//any libs in the ext dir
			String locpath = null;
			for (int i = 0; i < locs.length; i++) {
				IPath path = locs[i].getSystemLibraryPath();
				if(applies(path)) {
					locpath = path.toString();
					assertTrue("There should be a source path ending in test_resolver_src.zip on the ext lib ["+locpath+"]", 
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
		finally {
			isTesting = false;
			//force a re-compute to remove the bogus paths
			vm.setLibraryLocations(null);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.ILibraryLocationResolver#getPackageRoot(org.eclipse.core.runtime.IPath)
	 */
	public IPath getPackageRoot(IPath libraryPath) {
		if(applies(libraryPath)) {
	 		return new Path("src");
		}
		return Path.EMPTY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.ILibraryLocationResolver#getSourcePath(org.eclipse.core.runtime.IPath)
	 */
	public IPath getSourcePath(IPath libraryPath) {
		if(applies(libraryPath)) {
			File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/test_resolver_src.zip"));
			if(file.isFile()) {
				return new Path(file.getAbsolutePath());
			}
		}
		return Path.EMPTY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.ILibraryLocationResolver#getJavadocLocation(org.eclipse.core.runtime.IPath)
	 */
	public URL getJavadocLocation(IPath libraryPath) {
		if(applies(libraryPath)) {
			File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/test_resolver_javadoc.zip"));
			if(file.isFile()) {
				URI uri;
				try {
					uri = new URI("file", null, file.getAbsolutePath(), null);
					return URIUtil.toURL(uri);
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
				catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.ILibraryLocationResolver#getIndexLocation(org.eclipse.core.runtime.IPath)
	 */
	public URL getIndexLocation(IPath libraryPath) {
		if(applies(libraryPath)) {
			File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/test_resolver_index.index"));
			if(file.isFile()) {
				URI uri;
				try {
					uri = new URI("file", null, file.getAbsolutePath(), null);
					return URIUtil.toURL(uri);
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
				catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	boolean applies(IPath path) {
		for (int i = 0; i < path.segmentCount(); i++) {
			if("ext".equals(path.segment(i))) {
				return isTesting;
			}
		}
		return false;
	}
}