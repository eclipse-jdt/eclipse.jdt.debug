/*******************************************************************************
 * Copyright (c) 2025 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.tests.ui.AbstractDebugUiTests;
import org.eclipse.jdt.internal.launching.DetectVMInstallationsJob;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.TextConsole;

/**
 * <b>IMPORTANT</b> This test requires some different JVM installs to be present (see {@link #JAVA_11}, {@link #JAVA_17}, {@link #JAVA_21})) if such
 * JVMs can not be found, the test will fail! One can specify a basedir to search for such jvms with the {@link #JVM_SEARCH_BASE} system property.
 */
public class MultiReleaseLaunchTests extends AbstractDebugUiTests {

	private static final String JVM_SEARCH_BASE = "MultiReleaseLaunchTests.rootDir";
	private static final RequiredJavaVersion JAVA_11 = new RequiredJavaVersion(11, 16);
	private static final RequiredJavaVersion JAVA_17 = new RequiredJavaVersion(17, 20);
	private static final RequiredJavaVersion JAVA_21 = new RequiredJavaVersion(21, Integer.MAX_VALUE);

	private List<Runnable> disposeVms = new ArrayList<>();

	public MultiReleaseLaunchTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// see https://github.com/eclipse-jdt/eclipse.jdt.debug/issues/843
		if (!Platform.OS.isLinux()) { // JUnit 3, don't use assumeTrue, the thrown AssumptionViolatedException is treated as an error
			return;
		}
		final Set<File> existingLocations = new HashSet<>();
		List<RequiredJavaVersion> requiredJavaVersions = new ArrayList<>(List.of(JAVA_11, JAVA_17, JAVA_21));
		removeExistingJavaVersions(requiredJavaVersions, existingLocations);
		if (!requiredJavaVersions.isEmpty()) {
			final File rootDir = new File(System.getProperty(JVM_SEARCH_BASE, "/opt/tools/java/openjdk/"));
			matchInstallationsFrom(rootDir, requiredJavaVersions, existingLocations);
			if (!requiredJavaVersions.isEmpty()) {
				// another fallback: search the parent dir of java.home of the running JVM:
				File parentDir = new File(System.getProperty("java.home")).getParentFile();
				if (!parentDir.equals(rootDir)) {
					matchInstallationsFrom(parentDir, requiredJavaVersions, existingLocations);
				}
			}
		}
		assertTrue("The following java versions are required by this test but can not be found: "
				+ requiredJavaVersions, requiredJavaVersions.isEmpty());
	}

	private void matchInstallationsFrom(final File rootDir, List<RequiredJavaVersion> requiredJavaVersions, final Set<File> existingLocations) {
		final List<File> locations = new ArrayList<>();
		final List<IVMInstallType> types = new ArrayList<>();
		DetectVMInstallationsJob.search(rootDir, locations, types, existingLocations, new NullProgressMonitor());
		for (int i = 0; i < locations.size(); i++) {
			File location = locations.get(i);
			IVMInstallType type = types.get(i);
			String id = "MultiReleaseLaunchTests-" + UUID.randomUUID() + "-" + i;
			VMStandin workingCopy = new VMStandin(type, id);
			workingCopy.setInstallLocation(location);
			workingCopy.setName(id);
			IVMInstall install = workingCopy.convertToRealVM();
			if (removeIfMatch(requiredJavaVersions, install)) {
				disposeVms.add(() -> type.disposeVMInstall(id));
			} else {
				type.disposeVMInstall(id);
			}
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		disposeVms.forEach(Runnable::run);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return getMultireleaseProject();
	}

	public void testMultiReleaseLaunch() throws Exception {
		// see https://github.com/eclipse-jdt/eclipse.jdt.debug/issues/843
		if (!Platform.OS.isLinux()) { // JUnit 3, don't use assumeTrue, the thrown AssumptionViolatedException is treated as an error
			return;
		}
		ILaunchConfiguration config = getLaunchConfiguration("p.Main");
		Properties result = launchAndReadResult(config, 11);
		assertTrue("Was not launched with a proper Java installation " + result, JAVA_11.matches(result.getProperty("Java")));
		assertEquals("X should be executed from Java 11 version: " + result, "11", result.get("X"));
		assertNull("Y should not be executed from Java 11 version: " + result, result.get("Y"));
		assertNull("Z should not be executed from Java 11 version: " + result, result.get("Z"));
		Properties result17 = launchAndReadResult(config, 17);
		assertTrue("Was not launched with a proper Java installation " + result17, JAVA_17.matches(result17.getProperty("Java")));
		assertEquals("X should be executed from Java 17 version: " + result17, "17", result17.get("X"));
		assertEquals("Y should be executed from Java 11 version: " + result17, "11", result17.get("Y"));
		assertNull("Z should not be executed from Java 17 version: " + result17, result17.get("Z"));
		Properties result21 = launchAndReadResult(config, 21);
		assertTrue("Was not launched with a proper Java installation " + result21, JAVA_21.matches(result21.getProperty("Java")));
		assertEquals("X should be executed from Java 17 version: " + result21, "17", result21.get("X"));
		assertEquals("Y should be executed from Java 21 version: " + result21, "21", result21.get("Y"));
		assertEquals("Z should be executed from Java 17 version: " + result21, "17", result21.get("Z"));
	}

	private Properties launchAndReadResult(ILaunchConfiguration config, int javaVersion) throws Exception {
		ILaunchConfigurationWorkingCopy workingCopy = config.getWorkingCopy();
		workingCopy.setAttribute("org.eclipse.jdt.launching.JRE_CONTAINER", "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-"
				+ javaVersion + "/");
		Properties properties = new Properties();
		IJavaDebugTarget target = launchAndTerminate(workingCopy.doSave(), DEFAULT_TIMEOUT);
		processUiEvents();
		final IConsole console = DebugUITools.getConsole(target.getProcess());
		final TextConsole textConsole = (TextConsole) console;
		final IDocument consoleDocument = textConsole.getDocument();
		String content = consoleDocument.get();
		properties.load(new StringReader(content));
		DebugPlugin.getDefault().getLaunchManager().removeLaunch(target.getLaunch());
		return properties;
	}

	private static int getJavaVersion(IVMInstall install) {
		if (install instanceof IVMInstall2 vm) {
			try {
				String javaVersion = vm.getJavaVersion().split("\\.")[0]; //$NON-NLS-1$
				return Integer.parseInt(javaVersion);
			} catch (RuntimeException rte) {
				// can't know then...
			}
		}
		return -1;
	}

	private static void removeExistingJavaVersions(Collection<RequiredJavaVersion> requiredJavaVersions, Set<File> existingLocations) {
		IVMInstallType[] installTypes = JavaRuntime.getVMInstallTypes();
		for (IVMInstallType installType : installTypes) {
			IVMInstall[] vmInstalls = installType.getVMInstalls();
			for (IVMInstall install : vmInstalls) {
				if (requiredJavaVersions.isEmpty()) {
					return;
				}
				existingLocations.add(install.getInstallLocation());
				removeIfMatch(requiredJavaVersions, install);
			}
		}
	}

	protected static boolean removeIfMatch(Collection<RequiredJavaVersion> requiredJavaVersions, IVMInstall install) {
		int javaVersion = getJavaVersion(install);
		for (Iterator<RequiredJavaVersion> iterator = requiredJavaVersions.iterator(); iterator.hasNext();) {
			if (iterator.next().matches(javaVersion)) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}

	private static record RequiredJavaVersion(int from, int to) {

		public boolean matches(int version) {
			return (version >= from() && version <= to());
		}

		public boolean matches(String v) {
			return matches(Integer.parseInt(v));
		}
	}

}
