/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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

import static org.junit.Assert.assertThrows;
import static org.junit.Assume.assumeNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.launching.ILibraryLocationResolver;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstall3;
import org.eclipse.jdt.launching.IVMInstallType;
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
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=399798"
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
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=399798"
	 */
	public void testLibraryResolver2() throws Exception {
		VMInstallTestsLibraryLocationResolver.isTesting = true;
		try {
			String filename = "/testfiles/test-jre/bin/test-resolver.ee";
			if (Platform.OS.isWindows()) {
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
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=399798"
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
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=399798"
	 */
	public void testLibraryResolver4() throws Exception {
		VMInstallTestsLibraryLocationResolver.isTesting = true;
		try {
			String filename = "/testfiles/test-jre/bin/test-resolver2.ee";
			if (Platform.OS.isWindows()) {
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

	private static final Set<String> COMMON_JAVA_PACKAGES = Set.of("java.lang", "java.lang.reflect", "java.util", "java.io");
	private static final String JAVA9_OR_LATER_PACKAGE = "java.lang.module";
	private static final String JAVA11_OR_LATER_PACKAGE = "java.net.http";
	private static final String NON_MODULAR_JDK_PACKAGE = "javax.annotation";

	public void testJavaRuntimeQuerySystemPackages_modularJDK() throws CoreException {
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		String javaVersion = ((IVMInstall2) vm).getJavaVersion();
		assertTrue("At least a JDK 11 expected", JavaRuntime.compareJavaVersions(vm, JavaCore.VERSION_11) > 0);

		Set<String> defaultVersionPackages = JavaRuntime.getProvidedVMPackages(vm, null);

		assertContainsAllCommonPackages(vm, defaultVersionPackages);
		assertTrue(defaultVersionPackages.contains(JAVA9_OR_LATER_PACKAGE));
		assertTrue(defaultVersionPackages.contains(JAVA11_OR_LATER_PACKAGE));
		assertFalse(defaultVersionPackages.contains(NON_MODULAR_JDK_PACKAGE));

		Set<String> java9Packages = JavaRuntime.getProvidedVMPackages(vm, "9");

		assertContainsAllCommonPackages(vm, java9Packages);
		assertTrue(defaultVersionPackages.contains(JAVA9_OR_LATER_PACKAGE));
		assertFalse(java9Packages.contains(JAVA11_OR_LATER_PACKAGE));
		assertFalse(java9Packages.contains(NON_MODULAR_JDK_PACKAGE));

		// Test that a null release is equals to the actual version of the VM
		assertEquals(defaultVersionPackages, JavaRuntime.getProvidedVMPackages(vm, javaVersion));

		// Test failure if non-modular VM is asked for packages of Java<=1.8 or not yet available versions
		CoreException e1 = assertThrows(CoreException.class, () -> JavaRuntime.getProvidedVMPackages(vm, "1.8"));
		assertEquals("Cannot query a modular VM (JavaSE-9 or higher) for packages of release: 1.8", e1.getMessage());

		int majorJavaVersion = Integer.parseInt(javaVersion.contains(".") ? javaVersion.substring(0, javaVersion.indexOf('.')) : javaVersion);
		int latestSupportedJavaVersion = Integer.parseInt(JavaCore.latestSupportedJavaVersion());
		if (majorJavaVersion < latestSupportedJavaVersion) {
			// The following test-cases don't work if the VMInstall has the latest supported version. Internally jdt.core limits higher versions to
			// the latest supported one. Consequently these intended error-scenarios, testing too high versions don't fail (as expected).
			String nextJavaVersion = Integer.toString(majorJavaVersion + 1);
			CoreException e2 = assertThrows(CoreException.class, () -> JavaRuntime.getProvidedVMPackages(vm, nextJavaVersion));
			assertEquals("release " + nextJavaVersion + " is not found in the system", e2.getMessage());

			String versionAfterLatestSupported = String.valueOf(latestSupportedJavaVersion + 1);
			CoreException e3 = assertThrows(CoreException.class, () -> JavaRuntime.getProvidedVMPackages(vm, versionAfterLatestSupported));
			// Passing a release not yet supported by JDT should not fail if the JDK actually provides it (e.g. if one uses early-access builds).
			// Since EA-builds are not generally available in all test setups we check at least that the method passes the initial validation
			assertEquals("release " + versionAfterLatestSupported + " is not found in the system", e3.getMessage());
		}
		CoreException e4 = assertThrows(CoreException.class, () -> JavaRuntime.getProvidedVMPackages(vm, "definitivly-not-a-version"));
		assertEquals("Invalid release: definitivly-not-a-version", e4.getMessage());
	}

	public void testJavaRuntimeQuerySystemPackages_nonModularJDK() throws Exception {
		try (AutoCloseableSupplier<IVMInstall> nonModularVM = searchFirstNonModularVM()) {
			IVMInstall vm = nonModularVM.get();

			Set<String> packages = JavaRuntime.getProvidedVMPackages(vm, null);

			assertContainsAllCommonPackages(vm, packages);
			assertTrue("Not found: " + NON_MODULAR_JDK_PACKAGE, packages.contains(NON_MODULAR_JDK_PACKAGE));
			assertFalse("Found unexpected package: " + JAVA9_OR_LATER_PACKAGE, packages.contains(JAVA9_OR_LATER_PACKAGE));
			assertFalse("Found unexpected package: " + JAVA11_OR_LATER_PACKAGE, packages.contains(JAVA11_OR_LATER_PACKAGE));

			// Test that for non-modular VMs the release information is just ignored
			assertEquals(packages, JavaRuntime.getProvidedVMPackages(vm, "1.6"));
			assertEquals(packages, JavaRuntime.getProvidedVMPackages(vm, "definitivly-not-a-version"));
		} catch (org.junit.AssumptionViolatedException e) {
			// Ignore this test. TODO: remove this once this is a JUnit-4 or later test that handles assumptions correctly
		}
	}

	public void testLatestJavadocLocation() {
		String latest = JavaCore.latestSupportedJavaVersion();
		URL javadocLocation = StandardVMType.getDefaultJavadocLocation(latest);
		assertNotNull(javadocLocation);
		if (!javadocLocation.getPath().contains(latest)) {
			System.err.println("****************************WARNING!!**********************************");
			System.err.println("Update for Java " + latest + " needed in StandardVMType.getDefaultJavadocLocation()");
			System.err.println("***********************************************************************");
		}
	}

	private void assertContainsAllCommonPackages(IVMInstall vm, Set<String> packages) {
		Set<String> missing = new LinkedHashSet<>(COMMON_JAVA_PACKAGES);
		missing.removeAll(packages);
		assertTrue("Not all packages found in " + vm.getInstallLocation() + ", missing " + missing, missing.isEmpty());
	}

	private AutoCloseableSupplier<IVMInstall> searchFirstNonModularVM() throws IOException {
		IVMInstallType vmType = JavaRuntime.getVMInstallType(StandardVMType.ID_STANDARD_VM_TYPE);

		Set<File> candidateDirectories;
		String nonModularJavaHome = System.getenv("NON_MODULAR_JAVA_HOME");
		if (nonModularJavaHome != null) {
			System.out.println(getName() + ": NON_MODULAR_JAVA_HOME: " + nonModularJavaHome);
			candidateDirectories = Set.of(new File(nonModularJavaHome));
		} else {
			Path defaultVMLocation = JavaRuntime.getDefaultVMInstall().getInstallLocation().toPath();
			int maxSearchDepth = 1;
			Path root = Stream.iterate(defaultVMLocation, Objects::nonNull, Path::getParent) //
					.skip(maxSearchDepth).findFirst().orElseThrow();
			try (Stream<Path> directories = Files.walk(root, maxSearchDepth);) {
				candidateDirectories = directories.map(Path::toFile).collect(Collectors.toSet());
			}
			Arrays.stream(JavaRuntime.getVMInstallTypes()).flatMap(t -> Arrays.stream(t.getVMInstalls())) //
					.map(IVMInstall::getInstallLocation) //
					.forEach(candidateDirectories::remove);
		}
		System.out.println(getName() + ": found JVM candidates: " + candidateDirectories);

		IVMInstall candidate = null;
		for (File file : candidateDirectories) {
			if (vmType.validateInstallLocation(file).isOK()) {
				String id = "test-vm-" + System.nanoTime();
				IVMInstall vmCandidate = vmType.createVMInstall(id);
				vmCandidate.setInstallLocation(file);
				if (!JavaRuntime.isModularJava(vmCandidate)) {
					candidate = (vmCandidate);
					break;
				}
				vmType.disposeVMInstall(id);
			}
		}
		if (nonModularJavaHome != null) {
			assertNotNull("Non-modular VM (<=Java-1.8) not found.", candidate);
		} else {
			// Disable test if no non-modular VM is found if non is explicitly specified
			if (candidate == null) {
				System.err.println("No non-modular VM (<=Java-1.8) found.");
			}
			assumeNotNull(candidate);
		}
		IVMInstall vm = candidate;
		System.out.println(getName() + ": found non-modular JVM: " + vm.getInstallLocation());
		return new AutoCloseableSupplier<>() {
			@Override
			public IVMInstall get() {
				return vm;
			}

			@Override
			public void close() throws Exception {
				vmType.disposeVMInstall(vm.getId());
			}
		};
	}

	private interface AutoCloseableSupplier<T> extends AutoCloseable, Supplier<T> {
	}

	/**
	 * Checks the given {@link LibraryLocation}s to ensure they reference the testing resolver paths
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
				assertEquals("The source root path should be 'src' for ext lib [" + locpath + "]", "src", root.toString());
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
	 * @return the {@link VMStandin}
	 */
	VMStandin getEEStandin(String filename) throws CoreException {
		File ee = JavaTestPlugin.getDefault().getFileInPlugin(IPath.fromOSString(filename));
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