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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.JavaRuntime;

public class ModuleOptionsTests extends AbstractDebugTest {

	private static final String ASSUMED_DEFAULT_MODULES_9 = "java.se," //
			+ "javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web," // REMOVED in 10
			+ "jdk.accessibility,jdk.attach,jdk.compiler,jdk.dynalink,jdk.httpserver,"//
			+ "jdk.incubator.httpclient," //
			+ "jdk.jartool,jdk.javadoc,jdk.jconsole,jdk.jdi,"//
			+ "jdk.jfr," // all but 10
			+ "jdk.jshell,jdk.jsobject,jdk.management,"//
			+ "jdk.management.cmm,jdk.management.jfr,jdk.management.resource," // REMOVED later
			+ "jdk.net," //
			+ "jdk.packager,jdk.packager.services,jdk.plugin.dom," // NOT in openjdk
			+ "jdk.scripting.nashorn,jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
			+ "jdk.xml.dom,"//
			+ "oracle.desktop,oracle.net"; // NOT in openjdk
	private static final String ASSUMED_DEFAULT_MODULES_10 = "java.se," //
			+ "jdk.accessibility,jdk.attach,jdk.compiler,jdk.dynalink,jdk.httpserver," //
			+ "jdk.incubator.httpclient," // REMOVED later
			+ "jdk.jartool,jdk.javadoc,jdk.jconsole,jdk.jdi," //
			+ "jdk.jshell,jdk.jsobject,jdk.management," //
			+ "jdk.net," //
			+ "jdk.scripting.nashorn,jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
			+ "jdk.xml.dom";
	private static final String ASSUMED_DEFAULT_MODULES_11 = "java.se," //
			+ "jdk.accessibility,jdk.attach,jdk.compiler,jdk.dynalink,jdk.httpserver," //
			+ "jdk.jartool,jdk.javadoc,jdk.jconsole,jdk.jdi," //
			+ "jdk.jfr," //
			+ "jdk.jshell,jdk.jsobject,jdk.management," //
			+ "jdk.management.jfr," //
			+ "jdk.naming.ldap," //
			+ "jdk.net," //
			+ "jdk.scripting.nashorn,jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
			+ "jdk.unsupported.desktop," //
			+ "jdk.xml.dom";
	private static final String ASSUMED_DEFAULT_MODULES_12 = "java.se," //
			+ "jdk.accessibility,jdk.attach,jdk.compiler,jdk.dynalink,jdk.httpserver," //
			+ "jdk.jartool,jdk.javadoc,jdk.jconsole,jdk.jdi," //
			+ "jdk.jfr," // all but 10
			+ "jdk.jshell,jdk.jsobject,jdk.management," //
			+ "jdk.management.jfr," // all but 10
			+ "jdk.net," //
			+ "jdk.scripting.nashorn,jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
			+ "jdk.unsupported.desktop," // NEW
			+ "jdk.xml.dom";
	private static final String ASSUMED_DEFAULT_MODULES_14 = "java.se," //
			+ "jdk.accessibility,jdk.attach,jdk.compiler,jdk.dynalink,jdk.httpserver," //
			+ "jdk.incubator.foreign," // NEW in 14
			+ "jdk.jartool,jdk.javadoc,jdk.jconsole,jdk.jdi," //
			+ "jdk.jfr," //
			+ "jdk.jshell,jdk.jsobject,jdk.management," //
			+ "jdk.management.jfr," //
			+ "jdk.net," //
			+ "jdk.nio.mapmode," // NEW in 14
			+ "jdk.scripting.nashorn,jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
			+ "jdk.unsupported.desktop," // since 12
			+ "jdk.xml.dom";
	private static final String ASSUMED_DEFAULT_MODULES_15 = "java.se," //
			+ "jdk.accessibility,jdk.attach,jdk.compiler,jdk.dynalink,jdk.httpserver," //
			+ "jdk.incubator.foreign," //
			+ "jdk.jartool,jdk.javadoc,jdk.jconsole,jdk.jdi," //
			+ "jdk.jfr," //
			+ "jdk.jshell,jdk.jsobject,jdk.management," //
			+ "jdk.management.jfr," //
			+ "jdk.net," //
			+ "jdk.nio.mapmode," //
			+ "jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported," // jdk.scripting.nashorn removed in 15
			+ "jdk.unsupported.desktop," //
			+ "jdk.xml.dom";
	private static final String ASSUMED_DEFAULT_MODULES_16 = "java.se," //
			+ "jdk.accessibility,jdk.attach,jdk.compiler,jdk.dynalink,jdk.httpserver," //
			+ "jdk.incubator.foreign,jdk.incubator.vector," // jdk.incubator.vector added in 16
			+ "jdk.jartool,jdk.javadoc,jdk.jconsole,jdk.jdi," //
			+ "jdk.jfr," //
			+ "jdk.jshell,jdk.jsobject,jdk.management," //
			+ "jdk.management.jfr," //
			+ "jdk.net," //
			+ "jdk.nio.mapmode," //
			+ "jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported,"
			+ "jdk.unsupported.desktop," //
			+ "jdk.xml.dom";
	private static final String ASSUMED_DEFAULT_MODULES_19 = "java.se," //
			+ "jdk.accessibility,jdk.attach,jdk.compiler,jdk.dynalink,jdk.httpserver," //
			+ "jdk.incubator.concurrent,jdk.incubator.vector," // jdk.incubator.foreign removed and jdk.incubator.concurrent added in 19
			+ "jdk.jartool,jdk.javadoc,jdk.jconsole,jdk.jdi," //
			+ "jdk.jfr," //
			+ "jdk.jshell,jdk.jsobject,jdk.management," //
			+ "jdk.management.jfr," //
			+ "jdk.net," //
			+ "jdk.nio.mapmode," //
			+ "jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported," + "jdk.unsupported.desktop," //
			+ "jdk.xml.dom";
	private static final String ASSUMED_DEFAULT_MODULES_21 = "java.se," //
			+ "jdk.accessibility,jdk.attach,jdk.compiler,jdk.dynalink,jdk.httpserver," //
			+ "jdk.incubator.vector," // jdk.incubator removed in 21
			+ "jdk.jartool,jdk.javadoc,jdk.jconsole,jdk.jdi," //
			+ "jdk.jfr," //
			+ "jdk.jshell,jdk.jsobject,jdk.management," //
			+ "jdk.management.jfr," //
			+ "jdk.net," //
			+ "jdk.nio.mapmode," //
			+ "jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported," + "jdk.unsupported.desktop," //
			+ "jdk.xml.dom";


	public ModuleOptionsTests(String name) {
		super(name);
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
		List<String> list = JavaCore.defaultRootModules(Arrays.asList(javaProject.findUnfilteredPackageFragmentRoots(rawClasspath[i])));
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

	public void testAddModules1() throws JavaModelException {
		IJavaProject javaProject = getProjectContext();
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

	public void testLimitModules1() throws JavaModelException {
		IJavaProject javaProject = getProjectContext();
		List<String> defaultModules = getDefaultModules(javaProject);
		String expectedModules;
		String moduleList = String.join(",", defaultModules);
		switch (moduleList) {
			case ASSUMED_DEFAULT_MODULES_9:
				expectedModules = "java.se," //
						+ "javafx.fxml,javafx.swing,javafx.web," //
						+ "jdk.accessibility,jdk.httpserver,jdk.incubator.httpclient,"
						+ "jdk.jartool,jdk.jconsole,jdk.jshell," //
						+ "jdk.management.cmm,jdk.management.jfr,jdk.management.resource," //
						+ "jdk.net," //
						+ "jdk.packager,jdk.packager.services,jdk.plugin.dom," //
						+ "jdk.scripting.nashorn,jdk.sctp,"
						+ "jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
						+ "oracle.desktop,oracle.net";
				break;
			case ASSUMED_DEFAULT_MODULES_10:
				expectedModules = "java.se," //
						+ "jdk.accessibility,jdk.httpserver,jdk.incubator.httpclient," //
						+ "jdk.jartool,jdk.jconsole,jdk.jshell," //
						+ "jdk.jsobject," // previously pulled in via javafx.*
						+ "jdk.net," //
						+ "jdk.scripting.nashorn,jdk.sctp," //
						+ "jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
						+ "jdk.xml.dom";
				break;
			case ASSUMED_DEFAULT_MODULES_11:
				expectedModules = "java.se," //
						+ "jdk.accessibility,jdk.httpserver," //
						+ "jdk.jartool,jdk.jconsole," //
						+ "jdk.jshell,jdk.jsobject,jdk.management.jfr,jdk.naming.ldap," //
						+ "jdk.net,jdk.scripting.nashorn,jdk.sctp," //
						+ "jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
						+ "jdk.unsupported.desktop," //
						+ "jdk.xml.dom";
				break;
			case ASSUMED_DEFAULT_MODULES_12:
				expectedModules = "java.se," //
						+ "jdk.accessibility,jdk.httpserver," //
						+ "jdk.jartool,jdk.jconsole,jdk.jshell," //
						+ "jdk.jsobject," //
						+ "jdk.management.jfr," // all but 10
						+ "jdk.net," //
						+ "jdk.scripting.nashorn,jdk.sctp," //
						+ "jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
						+ "jdk.unsupported.desktop," // NEW
						+ "jdk.xml.dom";
				break;
			case ASSUMED_DEFAULT_MODULES_14:
				expectedModules = "java.se," //
						+ "jdk.accessibility,jdk.httpserver," //
						+ "jdk.incubator.foreign," // NEW in 14
						+ "jdk.jartool,jdk.jconsole,jdk.jshell," //
						+ "jdk.jsobject," //
						+ "jdk.management.jfr," //
						+ "jdk.net," //
						+ "jdk.nio.mapmode," // NEW in 14
						+ "jdk.scripting.nashorn,jdk.sctp," //
						+ "jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
						+ "jdk.unsupported.desktop," //
						+ "jdk.xml.dom";
				break;
			case ASSUMED_DEFAULT_MODULES_15:
				expectedModules = "java.se," //
						+ "jdk.accessibility," //
						+ "jdk.dynalink," // New in 15
						+ "jdk.httpserver," //
						+ "jdk.incubator.foreign," //
						+ "jdk.jartool,jdk.jconsole,jdk.jshell," //
						+ "jdk.jsobject," //
						+ "jdk.management.jfr," //
						+ "jdk.net," //
						+ "jdk.nio.mapmode," //
						+ "jdk.sctp," // Removed jdk.scripting.nashorn in 15
						+ "jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
						+ "jdk.unsupported.desktop," //
						+ "jdk.xml.dom";
				break;
			case ASSUMED_DEFAULT_MODULES_16:
				expectedModules = "java.se," //
						+ "jdk.accessibility," //
						+ "jdk.dynalink,"
						+ "jdk.httpserver," //
						+ "jdk.incubator.foreign,jdk.incubator.vector," // jdk.incubator.vector added in 16
						+ "jdk.jartool,jdk.jconsole,jdk.jshell," //
						+ "jdk.jsobject," //
						+ "jdk.management.jfr," //
						+ "jdk.net," //
						+ "jdk.nio.mapmode," //
						+ "jdk.sctp,"
						+ "jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
						+ "jdk.unsupported.desktop," //
						+ "jdk.xml.dom";
				break;
			case ASSUMED_DEFAULT_MODULES_19:
				expectedModules = "java.se," //
						+ "jdk.accessibility," //
						+ "jdk.dynalink," + "jdk.httpserver," //
						+ "jdk.incubator.concurrent,jdk.incubator.vector," // jdk.incubator.foreign removed and jdk.incubator.concurrent added in 19
						+ "jdk.jartool,jdk.jconsole,jdk.jshell," //
						+ "jdk.jsobject," //
						+ "jdk.management.jfr," //
						+ "jdk.net," //
						+ "jdk.nio.mapmode," //
						+ "jdk.sctp," + "jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
						+ "jdk.unsupported.desktop," //
						+ "jdk.xml.dom";
				break;
			case ASSUMED_DEFAULT_MODULES_21:
				expectedModules = "java.se," //
						+ "jdk.accessibility," //
						+ "jdk.dynalink," + "jdk.httpserver," //
						+ "jdk.incubator.vector," //
						+ "jdk.jartool,jdk.jconsole,jdk.jshell," //
						+ "jdk.jsobject," //
						+ "jdk.management.jfr,"
						+ "jdk.net," //
						+ "jdk.nio.mapmode," //
						+ "jdk.sctp," + "jdk.security.auth,jdk.security.jgss,jdk.unsupported," //
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
}
