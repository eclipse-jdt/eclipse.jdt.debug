/*******************************************************************************
 * Copyright (c) 2023 Ole Osterhagen and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ole Osterhagen - Issue 327 - Attribute "Without test code" is ignored in the launcher
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Tests for resolving the runtime classpath
 */
public class ResolveRuntimeClasspathTests extends AbstractDebugTest {

	private IProject projectA;
	private IProject projectB;

	public ResolveRuntimeClasspathTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		projectA = root.getProject("projectA");
		projectB = root.getProject("projectB");
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			projectA.delete(true, null);
		} catch (CoreException e) {
			// ignore
		}
		try {
			projectB.delete(true, null);
		} catch (CoreException e) {
			// ignore
		}
		super.tearDown();
	}

	public void testInclusionOfTestCode() throws CoreException {
		IJavaProject project = createProjectWithProjectDependency(false);
		IRuntimeClasspathEntry[] unresolved = JavaRuntime.computeUnresolvedRuntimeClasspath(project);
		// same as "excludeTestCode=false"
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(unresolved[0], project);

		assertTrue(isOnRuntimeClasspath(resolved, "/projectA/bin/main"));
		assertTrue(isOnRuntimeClasspath(resolved, "/projectA/bin/test"));
	}

	public void testExclusionOfTestCode() throws CoreException {
		IJavaProject project = createProjectWithProjectDependency(false);
		IRuntimeClasspathEntry[] unresolved = JavaRuntime.computeUnresolvedRuntimeClasspath(project);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(unresolved[0], project, true);

		assertTrue(isOnRuntimeClasspath(resolved, "/projectA/bin/main"));
		assertFalse(isOnRuntimeClasspath(resolved, "/projectA/bin/test"));
	}

	public void testExclusionOfTestCodeFromDependencyForProject() throws CoreException {
		IJavaProject project = createProjectWithProjectDependency(true);
		IRuntimeClasspathEntry[] unresolved = JavaRuntime.computeUnresolvedRuntimeClasspath(project);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(unresolved[0], project, false);

		assertTrue(isOnRuntimeClasspath(resolved, "/projectA/bin/main"));
		// even with "excludeTestCode=false" test code from project A is not accessible
		assertFalse(isOnRuntimeClasspath(resolved, "/projectA/bin/test"));
	}

	public void testExclusionOfTestCodeFromDependencyForLaunchConfiguration() throws Exception {
		IJavaProject project = createProjectWithProjectDependency(true);
		IRuntimeClasspathEntry[] unresolved = JavaRuntime.computeUnresolvedRuntimeClasspath(project);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(unresolved[0], createLaunchConfiguration("ResolveRuntimeClasspathTests"));

		assertTrue(isOnRuntimeClasspath(resolved, "/projectA/bin/main"));
		assertFalse(isOnRuntimeClasspath(resolved, "/projectA/bin/test"));
	}

	private boolean isOnRuntimeClasspath(IRuntimeClasspathEntry[] runtimeClasspathEntries, String path) {
		for (IRuntimeClasspathEntry runtimeClasspathEntry : runtimeClasspathEntries) {
			if (runtimeClasspathEntry.getPath().equals(new Path(path))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates two Java projects A and B. Project A contains a folder with non-test code and a folder with test code. Project B depends on project A.
	 *
	 * <ol>
	 * <li>Classpath of project A
	 * <ul>
	 * <li>path="main", output="bin/main"
	 * <li>path="test", output="bin/test", test=true
	 * </ul>
	 * <li>Classpath of project B
	 * <ul>
	 * <li>path="/projectA", without_test_code=true or false
	 * </ul>
	 * </ol>
	 *
	 * @param withoutTestCode
	 *            {@code true} if project B can access test code from project A, otherwise {@code false}.
	 * @return Java project B
	 * @throws CoreException
	 *             if this method fails.
	 */
	private IJavaProject createProjectWithProjectDependency(boolean withoutTestCode) throws CoreException {
		// create and configure project A
		IJavaProject javaProjectA = JavaProjectHelper.createJavaProject(projectA.getName());
		IFolder mainFolder = createFolders(projectA, "main");
		IFolder testFolder = createFolders(projectA, "test");
		IFolder binMainFolder = createFolders(projectA, "bin/main");
		IFolder binTestFolder = createFolders(projectA, "bin/test");
		JavaProjectHelper.addToClasspath(javaProjectA, JavaCore.newSourceEntry(mainFolder.getFullPath(), new IPath[0], new IPath[0], binMainFolder.getFullPath(), new IClasspathAttribute[0]));
		JavaProjectHelper.addToClasspath(javaProjectA, JavaCore.newSourceEntry(testFolder.getFullPath(), new IPath[0], new IPath[0], binTestFolder.getFullPath(), new IClasspathAttribute[] {
				JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true") }));

		// create and configure project B
		IJavaProject javaProjectB = JavaProjectHelper.createJavaProject(projectB.getName());
		JavaProjectHelper.addToClasspath(javaProjectB, JavaCore.newProjectEntry(projectA.getFullPath(), new IAccessRule[0], true, new IClasspathAttribute[] {
				JavaCore.newClasspathAttribute(IClasspathAttribute.WITHOUT_TEST_CODE, Boolean.toString(withoutTestCode)) }, false));

		return javaProjectB;
	}

	private IFolder createFolders(IProject project, String path) throws CoreException {
		IFolder folder = null;
		for (String segment : path.split("/")) {
			folder = folder != null ? folder.getFolder(segment) : project.getFolder(segment);
			if (!folder.exists()) {
				folder.create(true, true, null);
			}
		}
		return folder;
	}

}
