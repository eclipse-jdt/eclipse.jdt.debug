/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;

/**
 * Tests for execution environments
 */
public class ExecutionEnvironmentTests extends AbstractDebugTest {

	public ExecutionEnvironmentTests(String name) {
		super(name);
	}

	public void testGetEnvironments() throws Exception {
		IExecutionEnvironment[] executionEnvironments = JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments();
		assertTrue("Should be at least one environment", executionEnvironments.length > 0);
		for (int i = 0; i < executionEnvironments.length; i++) {
			IExecutionEnvironment environment = executionEnvironments[i];
			if (environment.getId().equals(JavaProjectHelper.JAVA_SE_1_7_EE_NAME)) {
				return;
			}
		}
		assertTrue("Did not find environment J2SE-1.4", false);
	}

	public void testAnalyze() throws Exception {
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();

		IExecutionEnvironment environment = manager.getEnvironment(JavaProjectHelper.JAVA_SE_1_7_EE_NAME);
		assertNotNull("Missing environment J2SE-1.4", environment);
		IVMInstall[] installs = environment.getCompatibleVMs();
		assertTrue("Should be at least one vm install for the environment", installs.length > 0);
		for (int i = 0; i < installs.length; i++) {
			IVMInstall install = installs[i];
			if (install.equals(vm)) {
				return;
			}
		}
		assertTrue("vm should be J2SE-1.4 compliant", false);
	}

	private int compareJavaVersions(IVMInstall vm, String ver) {
		if (vm instanceof AbstractVMInstall) {
			AbstractVMInstall install = (AbstractVMInstall) vm;
			String vmver = install.getJavaVersion();
			if (vmver == null) {
				return -1;
			}
			// versionToJdkLevel only handles 3 char versions = 1.5, 1.6, 1.7, etc
			if (vmver.length() > 3) {
				vmver = vmver.substring(0, 3);
			}
			return JavaCore.compareJavaVersions(vmver, ver);
		}
		return -1;

	}
	public void testAccessRuleParticipants() throws Exception {
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		IExecutionEnvironment environment = manager.getEnvironment("org.eclipse.jdt.debug.tests.environment.j2se14x");
		assertNotNull("Missing environment j2se14x", environment);
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		LibraryLocation[] libraries = JavaRuntime.getLibraryLocations(vm);
		IAccessRule[][] accessRules = environment.getAccessRules(vm, libraries, get14Project());
		assertNotNull("Missing access rules", accessRules);
		if (compareJavaVersions(vm, "9") >= 0) {
			assertEquals("Wrong number of rules", 1, accessRules.length);
			return;
		}
		assertEquals("Wrong number of rules", libraries.length, accessRules.length);
		for (int i = 0; i < accessRules.length; i++) {
			IAccessRule[] rules = accessRules[i];
			assertEquals("wrong number of rules for lib", 4, rules.length);
			assertEquals("Wrong rule", "secondary", rules[0].getPattern().toString());
			assertEquals("Wrong rule", "discouraged", rules[1].getPattern().toString());
			assertEquals("Wrong rule", "accessible", rules[2].getPattern().toString());
			assertEquals("Wrong rule", "non_accessible", rules[3].getPattern().toString());
		}
	}

	public void testNoAccessRuleParticipants() throws Exception {
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		IExecutionEnvironment environment = manager.getEnvironment("org.eclipse.jdt.debug.tests.environment.j2se13x");
		assertNotNull("Missing environment j2se13x", environment);
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		LibraryLocation[] libraries = JavaRuntime.getLibraryLocations(vm);
		IAccessRule[][] accessRules = environment.getAccessRules(vm, libraries, get14Project());
		assertNotNull("Missing access rules", accessRules);
		if (compareJavaVersions(vm, "9") >= 0) {
			assertEquals("Wrong number of rules", 1, accessRules.length);
			return;
		}
		assertEquals("Wrong number of rules", libraries.length, accessRules.length);
		for (int i = 0; i < accessRules.length; i++) {
			IAccessRule[] rules = accessRules[i];
			assertEquals("wrong number of rules for lib", 0, rules.length);
		}
	}

	public void testAccessRuleParticipantsWithShortCircut() throws Exception {
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		IExecutionEnvironment environment = manager.getEnvironment("org.eclipse.jdt.debug.tests.environment.j2se15x");
		assertNotNull("Missing environment j2se15x", environment);
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		LibraryLocation[] libraries = JavaRuntime.getLibraryLocations(vm);
		IAccessRule[][] accessRules = environment.getAccessRules(vm, libraries, get14Project());
		assertNotNull("Missing access rules", accessRules);
		if (compareJavaVersions(vm, "9") >= 0) {
			assertEquals("Wrong number of rules", 1, accessRules.length);
			return;
		}
		assertEquals("Wrong number of rules", libraries.length, accessRules.length);
		for (int i = 0; i < accessRules.length; i++) {
			IAccessRule[] rules = accessRules[i];
			assertEquals("wrong number of rules for lib", 1, rules.length);
			assertEquals("Wrong rule", "**/*", rules[0].getPattern().toString());
		}
	}

	/**
	 * Tests that a project bound to an EE has access rules.
	 */
	public void testAccessRulesPresentOnEEProject() throws Exception {
		boolean foundLib = false;
		IJavaProject project = getBoundEeProject();
		assertTrue("BoundEE project does not exist", project.exists());
		IClasspathEntry[] rawClasspath = project.getRawClasspath();
		for (int i = 0; i < rawClasspath.length; i++) {
			IClasspathEntry entry = rawClasspath[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				String environmentId = JavaRuntime.getExecutionEnvironmentId(entry.getPath());
				if (environmentId != null) {
					IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
					assertNotNull("missing classpath container", container);
					IClasspathEntry[] entries = container.getClasspathEntries();
					for (int j = 0; j < entries.length; j++) {
						foundLib = true;
						IClasspathEntry containerEntry = entries[j];
						assertTrue("Missing access rules", containerEntry.getAccessRules().length > 0);
					}
				}
			}
		}
		assertTrue("did not find JRE libs on classpath", foundLib);
	}

	/**
	 * Tests that a project bound to a specific JRE has no access rules.
	 */
	public void testAccessRulesNotPresentOnJREProject() throws Exception {
		boolean foundLib = false;
		IJavaProject project = getBoundJreProject();
		assertTrue("BoundJRE project does not exist", project.exists());
		IClasspathEntry[] rawClasspath = project.getRawClasspath();
		for (int i = 0; i < rawClasspath.length; i++) {
			IClasspathEntry entry = rawClasspath[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				IVMInstall install = JavaRuntime.getVMInstall(entry.getPath());
				if (install != null) {
					IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
					assertNotNull("missing classpath container", container);
					IClasspathEntry[] entries = container.getClasspathEntries();
					for (int j = 0; j < entries.length; j++) {
						foundLib = true;
						IClasspathEntry containerEntry = entries[j];
						assertEquals("access rules should not be present", 0, containerEntry.getAccessRules().length);
					}
				}
			}
		}
		assertTrue("did not find JRE library on classpath", foundLib);
	}

	/**
	 * Tests that default access rules appear for system packages when a profile file is specified.
	 *
	 * @throws Exception
	 */
	public void testDefaultSystemPackageAccessRules() throws Exception {
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		IExecutionEnvironment environment = manager.getEnvironment("org.eclipse.jdt.debug.tests.systemPackages");
		assertNotNull("Missing environment systemPackages", environment);
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		LibraryLocation[] libraries = JavaRuntime.getLibraryLocations(vm);
		IAccessRule[][] accessRules = environment.getAccessRules(vm, libraries, get14Project());
		assertNotNull("Missing access rules", accessRules);
		assertEquals("Wrong number of rules", libraries.length, accessRules.length);
		for (int i = 0; i < accessRules.length; i++) {
			IAccessRule[] rules = accessRules[i];
			assertEquals("wrong number of rules for lib", 4, rules.length);
			assertEquals("Wrong rule", "java/**", rules[0].getPattern().toString());
			assertEquals("Wrong rule", "one/two/*", rules[1].getPattern().toString());
			assertEquals("Wrong rule", "three/four/*", rules[2].getPattern().toString());
			assertEquals("Wrong rule", "**/*", rules[3].getPattern().toString());
		}
	}

	/**
	 * Tests that a location can be resolved for ${ee_home:J2SE-1.4}
	 *
	 * @throws Exception
	 */
	public void testEEHomeVariable() throws Exception {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		String result = manager.performStringSubstitution("${ee_home:J2SE-1.4}");
		assertNotNull(result);
		IExecutionEnvironment ee = JavaRuntime.getExecutionEnvironmentsManager().getEnvironment(JavaProjectHelper.JAVA_SE_1_7_EE_NAME);
		IVMInstall install = JavaRuntime.getVMInstall(JavaRuntime.newJREContainerPath(ee));
		assertEquals(install.getInstallLocation().getAbsolutePath(), result);
	}

	/**
	 * Tests that a location cannot be resolved for ${ee_home}
	 *
	 * @throws Exception
	 */
	public void testEEHomeVariableMissingArgument() throws Exception {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		try {
			manager.performStringSubstitution("${ee_home}");
		} catch (CoreException e) {
			return; // expected
		}
		assertNotNull("Test should have thrown an exception", null);
	}

	/**
	 * Tests that a location cannot be resolved for ${ee_home:bogus}
	 *
	 * @throws Exception
	 */
	public void testEEHomeVariableInvalidArgument() throws Exception {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		try {
			manager.performStringSubstitution("${ee_home:bogus}");
		} catch (CoreException e) {
			return; // expected
		}
		assertNotNull("Test should have thrown an exception", null);
	}
}
