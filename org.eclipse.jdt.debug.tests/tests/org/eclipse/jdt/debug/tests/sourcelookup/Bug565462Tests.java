/*******************************************************************************
 *  Copyright (c) 2020 Simeon Andreev and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.sourcelookup;

import java.io.File;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.junit.After;

/**
 * Tests for bug 565462.
 */
public class Bug565462Tests extends AbstractDebugTest {

	private static final String MODULE_JRE_PROJECT_NAME = "ModuleJREProject";
	private static final String NON_MODULE_JRE_PROJECT_NAME = "NonModuleJREProject";

	private IExecutionEnvironment javaSE11;
	private IVMInstall defaultJavaSE11VM;

	public Bug565462Tests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.javaSE11 = JavaRuntime.getExecutionEnvironmentsManager().getEnvironment("JavaSE-11");
		this.defaultJavaSE11VM = javaSE11.getDefaultVM();
		Predicate<IVMInstall> hasSource = vm -> vm != null && vm.getInstallLocation() != null && new File(vm.getInstallLocation(), "lib/src.zip").isFile();
		if (!hasSource.test(defaultJavaSE11VM)) {
			Stream.of(javaSE11.getCompatibleVMs()).filter(Objects::nonNull).filter(hasSource)
				.findAny()
				.ifPresent(javaSE11::setDefaultVM);
		}
		assertTrue("Default VM doesn't have source", hasSource.test(javaSE11.getDefaultVM()));
	}

	@After
	public void tearDown() throws Exception {
		javaSE11.setDefaultVM(defaultJavaSE11VM);
		super.tearDown();
	}

	/**
	 * Test for bug 565462.
	 *
	 * Tests searching for a class with 2 Java projects in the workspace, one with {@code module=true} attribute for the JRE container, one without
	 * that attribute.
	 */
	public void testFindDuplicatesBug565462() throws Exception {
		IJavaProject moduleProject = createJavaProject(MODULE_JRE_PROJECT_NAME);
		boolean attributeValue = true;
		addModuleAttribute(moduleProject, attributeValue);
		moduleProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
		IJavaProject nonModuleProject = createJavaProject(NON_MODULE_JRE_PROJECT_NAME);
		removeModuleAttribute(nonModuleProject);
		nonModuleProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());

		waitForBuild();
		assertTrue("Expected Java project to have module=true attribute: " + moduleProject, isModularProject(moduleProject));
		assertFalse("Expected Java project to not have module attribute: " + nonModuleProject, isModularProject(nonModuleProject));

		moduleProject.getProject().close(new NullProgressMonitor());
		nonModuleProject.getProject().close(new NullProgressMonitor());
		waitForBuild();
		moduleProject.getProject().open(new NullProgressMonitor());
		nonModuleProject.getProject().open(new NullProgressMonitor());
		moduleProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
		nonModuleProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
		waitForBuild();
		assertTrue("Expected Java project to have module=true attribute: " + moduleProject, isModularProject(moduleProject));
		assertFalse("Expected Java project to not have module attribute: " + nonModuleProject, isModularProject(nonModuleProject));

		JavaSourceLookupDirector director = new JavaSourceLookupDirector();
		ILaunchConfiguration configuration = createLaunchConfiguration(nonModuleProject, "Main");
		director.initializeDefaults(configuration);
		director.setFindDuplicates(true);

		String className = "java/lang/Class.java";
		File srcFile = new File(JavaRuntime.computeVMInstall(configuration).getInstallLocation(), "lib/src.zip");
		assertTrue(srcFile.getAbsolutePath() + " doesn't exist", srcFile.isFile());
		Object[] foundElements = director.findSourceElements(className);
		assertEquals("Expected only 1 match for class " + className + " in " + JavaRuntime.computeVMInstall(configuration).getInstallLocation() + " but found: " + Arrays.toString(foundElements), 1, foundElements.length);

	}

	private static void removeModuleAttribute(IJavaProject javaProject) throws JavaModelException {
		boolean isModularProject = isModularProject(javaProject);
		if (isModularProject) {
			IClasspathEntry[] classpath = javaProject.getRawClasspath();
			for (int i = 0; i < classpath.length; ++i) {
				IClasspathEntry classpathEntry = classpath[i];
				if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					IPath containerPath = classpathEntry.getPath();
					classpathEntry = JavaCore.newContainerEntry(containerPath);
					classpath[i] = classpathEntry;
				}
			}
			javaProject.setRawClasspath(classpath, new NullProgressMonitor());
		}
	}

	private static void addModuleAttribute(IJavaProject javaProject, boolean attributeValue) throws JavaModelException {
		boolean isModularProject = isModularProject(javaProject);
		if (!isModularProject) {
			IClasspathEntry[] classpath = javaProject.getRawClasspath();
			for (int i = 0; i < classpath.length; ++i) {
				IClasspathEntry classpathEntry = classpath[i];
				if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					IPath containerPath = classpathEntry.getPath();
					IClasspathAttribute[] attributes = classpathEntry.getExtraAttributes();
					IClasspathAttribute[] newAttributes = Arrays.copyOf(attributes, attributes.length + 1);
					IClasspathAttribute moduleAttribute = JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, String.valueOf(attributeValue));
					newAttributes[attributes.length] = moduleAttribute;
					boolean isExported = false;
					classpathEntry = JavaCore.newContainerEntry(containerPath, new IAccessRule[0], newAttributes, isExported);
					classpath[i] = classpathEntry;
				}
			}
			javaProject.setRawClasspath(classpath, new NullProgressMonitor());
		}
	}

	private static boolean isModularProject(IJavaProject javaProject) throws JavaModelException {
		IClasspathEntry[] classpath = javaProject.getRawClasspath();
		return isModularClasspath(classpath);
	}

	private static boolean isModularClasspath(IClasspathEntry[] classpath) {
		for (int i = 0; i < classpath.length; ++i) {
			IClasspathEntry classpathEntry = classpath[i];
			if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				boolean isModule = isModularEntry(classpathEntry);
				return isModule;
			}
		}
		return false;
	}

	private static boolean isModularEntry(IClasspathEntry classpathEntry) {
		IClasspathAttribute[] attributes = classpathEntry.getExtraAttributes();
		for (IClasspathAttribute attribute : attributes) {
			String attributeName = attribute.getName();
			if (IClasspathAttribute.MODULE.equals(attributeName)) {
				String attributeValue = attribute.getValue();
				boolean isModule = Boolean.parseBoolean(attributeValue);
				return isModule;
			}
		}
		return false;
	}

	private IJavaProject createJavaProject(String projectName) throws Exception {
		IPath testrpath = new Path("testresources").append("bug565462");
		IJavaProject javaProject = createJavaProjectClone(projectName, testrpath.append(projectName).toString(), "JavaSE-11", true);
		javaProject.setOption(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_11);
		javaProject.setOption(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
		javaProject.setOption(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);
		javaProject.setOption(CompilerOptions.OPTION_Release, CompilerOptions.ENABLED);
		return javaProject;
	}
}
