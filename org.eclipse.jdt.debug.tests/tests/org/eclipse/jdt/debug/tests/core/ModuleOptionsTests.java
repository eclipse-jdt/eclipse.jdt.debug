/*******************************************************************************
 * Copyright (c) 2019, 2024 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *     IBM Corporation - bug fixes
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

public class ModuleOptionsTests extends AbstractDebugTest {

	private static final String ASSUMED_DEFAULT_MODULES_9 = "java.se," //
			// + "javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web," REMOVED in 10
			+ "jdk.accessibility,jdk.attach,jdk.compiler,jdk.dynalink,jdk.httpserver,"//
			+ "jdk.incubator.vector," // original 9: jdk.incubator.httpclient
			+ "jdk.jartool,jdk.javadoc,jdk.jconsole,jdk.jdi,"//
			+ "jdk.jfr," // all but 10
			+ "jdk.jshell,jdk.jsobject,jdk.management,"//
			// + "jdk.management.cmm,jdk.management.jfr,jdk.management.resource," original ...
			+ "jdk.management.jfr," // ... as seen from 23 with --release 9
			+ "jdk.net," //
			// + "jdk.packager,jdk.packager.services,jdk.plugin.dom," NOT in openjdk
			// + "jdk.scripting.nashorn," // not present in 23
			+ "jdk.nio.mapmode," // new in ?? even at --release 9 ??
			+ "jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
			+ "jdk.unsupported.desktop," // new in ?? even at --release 9 ??
			+ "jdk.xml.dom"; //
			// + "oracle.desktop,oracle.net";  NOT in openjdk
	private static final String ASSUMED_DEFAULT_MODULES_21 = "java.base," //
			+ "java.compiler,java.datatransfer,java.desktop,java.instrument,java.logging," //
			+ "java.management,java.management.rmi,java.naming,java.net.http,java.prefs,java.rmi," //
			+ "java.scripting,java.security.jgss,java.security.sasl,java.smartcardio," //
			+ "java.sql,java.sql.rowset,java.transaction.xa,java.xml,java.xml.crypto," //
			+ "jdk.accessibility,jdk.attach,jdk.compiler,jdk.dynalink,jdk.httpserver," //
			+ "jdk.incubator.vector," // jdk.incubator removed in 21
			+ "jdk.jartool,jdk.javadoc,jdk.jconsole,jdk.jdi," //
			+ "jdk.jfr," //
			+ "jdk.jshell,jdk.jsobject,jdk.management," //
			+ "jdk.management.jfr," //
			+ "jdk.net," //
			+ "jdk.nio.mapmode," //
			+ "jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
			+ "jdk.unsupported.desktop," //
			+ "jdk.xml.dom";

	private IVMInstall defaultVM9;

	public ModuleOptionsTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		defaultVM9 = prepareExecutionEnvironment(JavaProjectHelper.JAVA_SE_9_EE_NAME);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			setExecutionEnvironment(JavaProjectHelper.JAVA_SE_9_EE_NAME, defaultVM9);
		} finally {
			super.tearDown();
		}
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get9Project();
	}

	protected void addClasspathAttributesToSystemLibrary(IJavaProject project, IClasspathAttribute[] extraAttributes) throws JavaModelException {
		IClasspathEntry[] rawClasspath = project.getRawClasspath();
		int i = indexOfJREContainer(rawClasspath);
		rawClasspath[i] = JavaCore.newContainerEntry(rawClasspath[i].getPath(), null, extraAttributes, false);
		project.setRawClasspath(rawClasspath, null);
		waitForBuild();
	}

	protected void removeClasspathAttributesFromSystemLibrary(IJavaProject project) throws JavaModelException {
		IClasspathEntry[] rawClasspath = project.getRawClasspath();
		int i = indexOfJREContainer(rawClasspath);
		rawClasspath[i] = JavaCore.newContainerEntry(rawClasspath[i].getPath(), null, new IClasspathAttribute[0], false);
		project.setRawClasspath(rawClasspath, null);
		waitForBuild();
	}

	private List<String> getDefaultModules(IJavaProject javaProject) throws JavaModelException {
		IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
		int i = indexOfJREContainer(rawClasspath);
		String release = null;
		if (JavaCore.ENABLED.equals(javaProject.getOption(JavaCore.COMPILER_RELEASE, true))) {
			release = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		}
		List<String> list = JavaCore.defaultRootModules(Arrays.asList(javaProject.findUnfilteredPackageFragmentRoots(rawClasspath[i])), release);
		list.sort(String::compareTo);
		return list;
	}

	private int indexOfJREContainer(IClasspathEntry[] rawClasspath) {
		for (int i = 0; i < rawClasspath.length; i++) {
			IClasspathEntry classpathEntry = rawClasspath[i];
			if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_CONTAINER
					&& JavaRuntime.JRE_CONTAINER.equals(classpathEntry.getPath().segment(0))) {
				return i;
			}
		}
		return -1;
	}

	public void testAddModules1() throws Exception {
		IJavaProject javaProject = getProjectContext();
		checkVMInstall(javaProject);
		List<String> defaultModules = getDefaultModules(javaProject);
		defaultModules.add("jdk.crypto.cryptoki"); // requires jdk.crypto.ec up to Java 21
		try {
			IClasspathAttribute[] attributes = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.LIMIT_MODULES, String.join(",", defaultModules))
			};
			addClasspathAttributesToSystemLibrary(javaProject, attributes);

			ILaunchConfiguration launchConfiguration = getLaunchConfiguration(javaProject, "LogicalStructures");
			String cliOptions = JavaRuntime.getModuleCLIOptions(launchConfiguration);
			if (Runtime.version().feature() > 21) {
				// https://www.oracle.com/java/technologies/javase/22all-relnotes.html#JDK-8308398 removed jdk.crypto.ec in Java 22
				assertEquals("Unexpectd cli options", "--add-modules jdk.crypto.cryptoki", cliOptions);
			} else {
				assertEquals("Unexpectd cli options", "--add-modules jdk.crypto.cryptoki,jdk.crypto.ec", cliOptions);
			}
		} finally {
			removeClasspathAttributesFromSystemLibrary(javaProject);
		}
	}

	public void testLimitModules_release9() throws Exception {
		IJavaProject javaProject = getProjectContext();
		checkVMInstall(javaProject);
		try {
			javaProject.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
			List<String> defaultModules = getDefaultModules(javaProject);
			String expectedModules;
			String moduleList = String.join(",", defaultModules);
			assertEquals(ASSUMED_DEFAULT_MODULES_9, moduleList);
			switch (moduleList) {
				case ASSUMED_DEFAULT_MODULES_9:
					expectedModules = //
							"java.instrument,java.net.http,java.scripting,java.sql.rowset,java.xml.crypto," //
							+ "jdk.accessibility,jdk.dynalink,jdk.httpserver,jdk.incubator.vector," //
							+ "jdk.jartool,jdk.jconsole,jdk.jshell," //
							+ "jdk.jsobject,jdk.management.jfr," //
							+ "jdk.net," //
							+ "jdk.nio.mapmode," //
							// + "jdk.packager,jdk.packager.services,jdk.plugin.dom,"
									// + "jdk.scripting.nashorn,"
							+ "jdk.sctp,"
							+ "jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
							+ "jdk.unsupported.desktop,jdk.xml.dom";
					break;
				default:
					fail("Unknown set of default modules " + moduleList);
					return;
			}
			assertTrue("expected module was not in defaultModules", defaultModules.remove("jdk.javadoc")); // requires java.compiler and jdk.compiler
																											// but is required by no default module
			IClasspathAttribute[] attributes = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.LIMIT_MODULES, String.join(",", defaultModules)) };
			addClasspathAttributesToSystemLibrary(javaProject, attributes);

			ILaunchConfiguration launchConfiguration = getLaunchConfiguration(javaProject, "LogicalStructures");
			String cliOptions = JavaRuntime.getModuleCLIOptions(launchConfiguration);
			String add = " --add-modules java.se";
			assertEquals("Unexpectd cli options", "--limit-modules " + expectedModules + add, cliOptions);
		} finally {
			javaProject.setOption(JavaCore.COMPILER_RELEASE, JavaCore.DISABLED);
			removeClasspathAttributesFromSystemLibrary(javaProject);
		}
	}

	public void testLimitModules1() throws Exception {
		IJavaProject javaProject = getProjectContext();
		javaProject.setOption(JavaCore.COMPILER_RELEASE, JavaCore.DISABLED);
		checkVMInstall(javaProject);
		List<String> defaultModules = getDefaultModules(javaProject);
		String expectedModules;
		String moduleList = String.join(",", defaultModules);
		switch (moduleList) {
			case ASSUMED_DEFAULT_MODULES_21:
				expectedModules = "java.instrument,java.net.http,java.scripting,java.smartcardio,java.sql.rowset,java.xml.crypto," //
						+ "jdk.accessibility," //
						+ "jdk.dynalink," //
						+ "jdk.httpserver," //
						+ "jdk.incubator.vector," //
						+ "jdk.jartool,jdk.jconsole,jdk.jshell," //
						+ "jdk.jsobject," //
						+ "jdk.management.jfr,"
						+ "jdk.net," //
						+ "jdk.nio.mapmode," //
						+ "jdk.sctp," //
						+ "jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
						+ "jdk.unsupported.desktop," //
						+ "jdk.xml.dom";
				break;
			default:
				fail("Unknown set of default modules " + moduleList);
				return;
		}
		assertTrue("expected module was not in defaultModules", defaultModules.remove("jdk.javadoc")); // requires java.compiler and jdk.compiler but
																										// is required by no default module
		try {
			IClasspathAttribute[] attributes = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.LIMIT_MODULES, String.join(",", defaultModules)) };
			addClasspathAttributesToSystemLibrary(javaProject, attributes);

			ILaunchConfiguration launchConfiguration = getLaunchConfiguration(javaProject, "LogicalStructures");
			String cliOptions = JavaRuntime.getModuleCLIOptions(launchConfiguration);
			assertEquals("Unexpectd cli options", "--limit-modules " + expectedModules, cliOptions);
		} finally {
			removeClasspathAttributesFromSystemLibrary(javaProject);
		}
	}

	private void checkVMInstall(IJavaProject javaProject) throws CoreException {
		IVMInstall defaultVm = JavaRuntime.getDefaultVMInstall();
		IVMInstall vm = JavaRuntime.getVMInstall(javaProject);
		assertEquals("Expected default VM but got: " + vm.getInstallLocation(), defaultVm.getName(), vm.getName());
	}
}
