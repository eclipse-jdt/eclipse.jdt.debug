/*******************************************************************************
 * Copyright (c) 2023, 2026 Ole Osterhagen and others.
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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Tests for resolving the runtime classpath
 */
public class ResolveRuntimeClasspathTests extends AbstractDebugTest {

	/**
	 * Container registered by the test plug-in, without a runtime classpath entry resolver
	 */
	private static final IPath CONTAINER_PATH = new Path("org.eclipse.jdt.debug.tests.TestClasspathContainer");

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

	public void testInclusionOfTestCodeFromContainer() throws CoreException {
		IJavaProject project = createProjectWithContainer();
		IRuntimeClasspathEntry container = JavaRuntime.newRuntimeContainerClasspathEntry(CONTAINER_PATH, IRuntimeClasspathEntry.USER_CLASSES, project);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(container, project, false);

		assertTrue(isOnRuntimeClasspath(resolved, "/projectB/lib/main"));
		assertTrue(isOnRuntimeClasspath(resolved, "/projectB/lib/test"));
		assertTrue(isOnRuntimeClasspath(resolved, "/projectA/bin/main"));
	}

	public void testExclusionOfTestCodeFromContainer() throws CoreException {
		IJavaProject project = createProjectWithContainer();
		IRuntimeClasspathEntry container = JavaRuntime.newRuntimeContainerClasspathEntry(CONTAINER_PATH, IRuntimeClasspathEntry.USER_CLASSES, project);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(container, project, true);

		assertTrue(isOnRuntimeClasspath(resolved, "/projectB/lib/main"));
		assertFalse(isOnRuntimeClasspath(resolved, "/projectB/lib/test"));
		assertFalse(isOnRuntimeClasspath(resolved, "/projectA/bin/main"));
	}

	public void testExclusionOfTestCodeFromContainerForLaunchConfiguration() throws CoreException {
		IJavaProject project = createProjectWithContainer();
		IRuntimeClasspathEntry container = JavaRuntime.newRuntimeContainerClasspathEntry(CONTAINER_PATH, IRuntimeClasspathEntry.USER_CLASSES, project);
		// not saved, only the attribute is needed; the container entry already knows its project
		ILaunchConfigurationWorkingCopy configuration = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION).newInstance(null, "ResolveRuntimeClasspathTests");
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_EXCLUDE_TEST_CODE, true);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(container, configuration);

		assertTrue(isOnRuntimeClasspath(resolved, "/projectB/lib/main"));
		assertFalse(isOnRuntimeClasspath(resolved, "/projectB/lib/test"));
		assertFalse(isOnRuntimeClasspath(resolved, "/projectA/bin/main"));
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

	/**
	 * Creates two Java projects A and B. Project B has a classpath container that has no runtime classpath entry resolver. The container contains a
	 * library with non-test code, a library with test code and project A as test code.
	 *
	 * <ol>
	 * <li>Classpath of project A
	 * <ul>
	 * <li>path="main", output="bin/main"
	 * </ul>
	 * <li>Entries of the container
	 * <ul>
	 * <li>path="/projectB/lib/main"
	 * <li>path="/projectB/lib/test", test=true
	 * <li>path="/projectA", test=true
	 * </ul>
	 * </ol>
	 *
	 * @return Java project B
	 * @throws CoreException
	 *             if this method fails.
	 */
	private IJavaProject createProjectWithContainer() throws CoreException {
		// create and configure project A
		IJavaProject javaProjectA = JavaProjectHelper.createJavaProject(projectA.getName());
		IFolder mainFolder = createFolders(projectA, "main");
		IFolder binMainFolder = createFolders(projectA, "bin/main");
		JavaProjectHelper.addToClasspath(javaProjectA, JavaCore.newSourceEntry(mainFolder.getFullPath(), new IPath[0], new IPath[0], binMainFolder.getFullPath(), new IClasspathAttribute[0]));

		// create and configure project B
		IJavaProject javaProjectB = JavaProjectHelper.createJavaProject(projectB.getName());
		IFolder libMainFolder = createFolders(projectB, "lib/main");
		IFolder libTestFolder = createFolders(projectB, "lib/test");
		IClasspathAttribute[] testAttributes = new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true") };
		IClasspathEntry[] containerEntries = new IClasspathEntry[] { JavaCore.newLibraryEntry(libMainFolder.getFullPath(), null, null),
				JavaCore.newLibraryEntry(libTestFolder.getFullPath(), null, null, new IAccessRule[0], testAttributes, false),
				JavaCore.newProjectEntry(projectA.getFullPath(), new IAccessRule[0], true, testAttributes, false) };
		IClasspathContainer container = new IClasspathContainer() {
			@Override
			public IClasspathEntry[] getClasspathEntries() {
				return containerEntries;
			}

			@Override
			public String getDescription() {
				return "Test Container";
			}

			@Override
			public int getKind() {
				return IClasspathContainer.K_APPLICATION;
			}

			@Override
			public IPath getPath() {
				return CONTAINER_PATH;
			}
		};
		JavaCore.setClasspathContainer(CONTAINER_PATH, new IJavaProject[] { javaProjectB }, new IClasspathContainer[] { container }, null);
		JavaProjectHelper.addContainerEntry(javaProjectB, CONTAINER_PATH);
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
