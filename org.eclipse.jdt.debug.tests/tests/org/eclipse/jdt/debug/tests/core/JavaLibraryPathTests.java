/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Tests for native classpath entries.
 *
 * @since 3.1
 */
public class JavaLibraryPathTests extends AbstractDebugTest {

	public JavaLibraryPathTests(String name) {
		super(name);
	}

	/**
	 * Create test projects "PathTests1/2/3"
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createProject("PathTests1");
		createProject("PathTests2");
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject("PathTests1");
		deleteProject("PathTests2");
		super.tearDown();
	}

	private IJavaProject createProject(String name) throws Exception {
		IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		if (pro.exists()) {
			pro.delete(true, true, null);
		}
		IJavaProject project = JavaProjectHelper.createJavaProject(name, "bin");
		return project;
	}

	private void deleteProject(String name) throws Exception {
		IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		if (pro.exists()) {
			pro.delete(true, true, null);
		}
	}

	private void addToClasspath(IJavaProject jproject, IClasspathEntry cpe) throws JavaModelException {
		IClasspathEntry[] oldEntries= jproject.getRawClasspath();
		for (int i= 0; i < oldEntries.length; i++) {
			if (oldEntries[i].equals(cpe)) {
				return;
			}
		}
		int nEntries= oldEntries.length;
		IClasspathEntry[] newEntries= new IClasspathEntry[nEntries + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, nEntries);
		newEntries[nEntries]= cpe;
		jproject.setRawClasspath(newEntries, null);
	}

	public void testRequiredProjectExplicitPath() throws Exception {
		// add required project with one java library path entry
		IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IClasspathAttribute attribute = JavaRuntime.newLibraryPathsAttribute(new String[] {path.toString()});
		IClasspathEntry entry = JavaCore.newProjectEntry(new Path("PathTests3").makeAbsolute(), new IAccessRule[0], false, new IClasspathAttribute[]{attribute}, false);
		addToClasspath(getJavaProject("PathTests2"), entry);
		entry = JavaCore.newProjectEntry(new Path("PathTests2").makeAbsolute());
		addToClasspath(getJavaProject("PathTests1"), entry);

		String[] strings = JavaRuntime.computeJavaLibraryPath(getJavaProject("PathTests1"), true);
		assertEquals("Wrong number of entries", 1, strings.length);
		assertEquals("Wrong entry", path.toFile().getCanonicalPath(), new File(strings[0]).getCanonicalPath());
	}

	public void testVMArgsForRequiredProjectExplicitPath() throws Exception {
		// add required project with one java library path entry
		IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IClasspathAttribute attribute = JavaRuntime.newLibraryPathsAttribute(new String[]{path.toString()});
		IClasspathEntry entry = JavaCore.newProjectEntry(new Path("PathTests3").makeAbsolute(), new IAccessRule[0], false, new IClasspathAttribute[]{attribute}, false);
		addToClasspath(getJavaProject("PathTests2"), entry);
		entry = JavaCore.newProjectEntry(new Path("PathTests2").makeAbsolute());
		addToClasspath(getJavaProject("PathTests1"), entry);

		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, "testVMArgsForRequiredProjectExplicitPath");
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "PathTests1");
		ILaunchDelegate d = ((LaunchManager) getLaunchManager()).getLaunchDelegate(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfigurationDelegate delegate = d.getDelegate();
		assertTrue(delegate instanceof JavaLaunchDelegate);
		JavaLaunchDelegate launcher = (JavaLaunchDelegate) delegate;
		String arguments = launcher.getVMArguments(workingCopy);
		String expect = "-Djava.library.path=\"" + path.toFile().getAbsolutePath() + "\"";
		assertTrue("wrong VM args", arguments.indexOf(expect) >= 0);
	}

	public void testMultiVMArgsForRequiredProjectExplicitPath() throws Exception {
		// add required project with one java library path entry
		IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IPath path2 = ResourcesPlugin.getWorkspace().getRoot().getProject("PathTests1").getLocation();
		IClasspathAttribute attribute = JavaRuntime.newLibraryPathsAttribute(new String[]{path.toString(), path2.toString()});
		IClasspathEntry entry = JavaCore.newProjectEntry(new Path("PathTests3").makeAbsolute(), new IAccessRule[0], false, new IClasspathAttribute[]{attribute}, false);
		addToClasspath(getJavaProject("PathTests2"), entry);
		entry = JavaCore.newProjectEntry(new Path("PathTests2").makeAbsolute());
		addToClasspath(getJavaProject("PathTests1"), entry);

		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, "testVMArgsForRequiredProjectExplicitPath");
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "PathTests1");
		ILaunchDelegate d = ((LaunchManager) getLaunchManager()).getLaunchDelegate(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfigurationDelegate delegate = d.getDelegate();
		assertTrue(delegate instanceof JavaLaunchDelegate);
		JavaLaunchDelegate launcher = (JavaLaunchDelegate) delegate;
		String arguments = launcher.getVMArguments(workingCopy);
		String expect = "-Djava.library.path=\"" + path.toFile().getAbsolutePath() + File.pathSeparator + path2.toFile().getAbsolutePath() + "\"";
		assertTrue("wrong VM args", arguments.indexOf(expect) >= 0);
	}

	public void testNoRequiredProjectExplicitPath() throws Exception {
		// add required project with one java library path entry
		IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IClasspathAttribute attribute = JavaRuntime.newLibraryPathsAttribute(new String[]{ path.toString()});
		IClasspathEntry entry = JavaCore.newProjectEntry(new Path("PathTests3").makeAbsolute(), new IAccessRule[0], false, new IClasspathAttribute[]{attribute}, false);
		addToClasspath(getJavaProject("PathTests2"), entry);
		entry = JavaCore.newProjectEntry(new Path("PathTests2").makeAbsolute());
		addToClasspath(getJavaProject("PathTests1"), entry);

		String[] strings = JavaRuntime.computeJavaLibraryPath(getJavaProject("PathTests1"), false);
		assertEquals("Wrong number of entries", 0, strings.length);
	}

	public void testStringVariablePath() throws Exception {
		//add A.jar
		File jar = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testjars" + File.separator + "A.jar"));
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		IValueVariable variable = manager.newValueVariable("a-path", "testStringVariablePath");
		IPath rootPath = new Path(jar.getParentFile().getAbsolutePath());
		variable.setValue(rootPath.toPortableString());
		manager.addVariables(new IValueVariable[]{variable});
		try {
			String path = "${a-path}" + File.separator +"A.jar";
			IClasspathAttribute attribute = JavaRuntime.newLibraryPathsAttribute(new String[]{path});
			IClasspathEntry entry = JavaCore.newLibraryEntry(new Path(jar.getAbsolutePath()), null, null, new IAccessRule[0], new IClasspathAttribute[]{attribute}, false);
			addToClasspath(getJavaProject("PathTests1"), entry);

			String[] strings = JavaRuntime.computeJavaLibraryPath(getJavaProject("PathTests1"), false);
			assertEquals("Wrong number of entries", 1, strings.length);
			assertEquals("Wrong entry", jar.getCanonicalFile().getCanonicalPath(), new File(strings[0]).getCanonicalPath());
		} finally {
			manager.removeVariables(new IValueVariable[]{variable});
		}
	}
}
