/*******************************************************************************
 * Copyright (c) 2018 Cedric Chabanois and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Cedric Chabanois (cchabanois@gmail.com) - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.launching;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathSupport;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

/**
 * Test long module-path. OSs have limits in term of command line length or argument length. We use an argument file when module-path is too long.
 *
 */
public class LongModulePathTests extends AbstractDebugTest {
	private static final IPath CLASSPATH_PROJECT_CONTENT_PATH = new Path("testresources/classpathModuleProject");
	private static final String MAIN_TYPE_NAME = "test.classpath.Main";
	private IJavaProject javaProject;
	private IJavaThread thread;
	private ILaunchConfiguration launchConfiguration;

	public LongModulePathTests(String name) {
		super(name);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			if (thread != null) {
				terminateAndRemove(thread);
			}
			if (javaProject != null) {
				javaProject.getProject().delete(true, true, null);
			}
			if (launchConfiguration != null) {
				launchConfiguration.delete();
			}
		} catch (CoreException ce) {
			// ignore
		} finally {
			super.tearDown();
		}
	}

	/*
	 * When JVM > 9, an argument file for the modulepath is created when modulepath is too long
	 */
	public void testVeryLongModulepathWithArgumentFile() throws Exception {
		// Given
		javaProject = createJavaProjectClone("testVeryLongModulePath", CLASSPATH_PROJECT_CONTENT_PATH.toString(), JavaProjectHelper.JAVA_SE_9_EE_NAME, true);
		useComplianceFromExecutionEnvironment(javaProject);
		useModuleForJREContainer(javaProject);
		launchConfiguration = createLaunchConfigurationStopInMain(javaProject, MAIN_TYPE_NAME);
		int minModulePathLength = 300000;
		setLongModulepath(javaProject, minModulePathLength);
		waitForBuild();

		// When
		thread = launchAndSuspend(launchConfiguration);

		// Then
		File tempFile = getTempFile(thread.getLaunch()).orElseThrow(() -> new RuntimeException("No temp file"));
		assertTrue(tempFile.exists());
		assertTrue(tempFile.getName().endsWith(".txt"));
		assertTrue(doEval(thread, "System.getProperty(\"jdk.module.path\")").getValueString().length() >= minModulePathLength);

		// When
		resumeAndExit(thread);

		// Then
		if (!Platform.getOS().equals(Platform.OS_WIN32)) {
			// On windows, temp file deletion may fail
			assertFalse(tempFile.exists());
		}
	}

	private ILaunchConfiguration createLaunchConfigurationStopInMain(IJavaProject javaProject, String mainTypeName) throws Exception, CoreException {
		ILaunchConfiguration launchConfiguration;
		launchConfiguration = createLaunchConfiguration(javaProject, mainTypeName);
		ILaunchConfigurationWorkingCopy wc = launchConfiguration.getWorkingCopy();
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
		launchConfiguration = wc.doSave();
		return launchConfiguration;
	}

	private void setLongModulepath(IJavaProject javaProject, int minModulePathLength) throws Exception {
		StringBuilder sb = new StringBuilder();
		List<IClasspathEntry> classpathEntries = new ArrayList<>();
		int i = 0;
		while (sb.length() < minModulePathLength) {
			String jarName = "library" + i + ".jar";
			IPath targetPath = javaProject.getPath().append("lib/" + jarName);
			javaProject.getProject().getFile("lib/classpath.jar").copy(targetPath, IResource.FORCE, new NullProgressMonitor());
			IClasspathAttribute moduleClasspathAttribute = JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true");
			classpathEntries.add(JavaCore.newLibraryEntry(targetPath, null, null, null, new IClasspathAttribute[] {
					moduleClasspathAttribute }, false));
			if (i != 0) {
				sb.append(File.pathSeparator);
			}
			sb.append(javaProject.getProject().getFile("lib/" + jarName).getLocation().toString());
			i++;
		}
		IClasspathAttribute moduleClasspathAttribute = JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true");
		classpathEntries.add(JavaCore.newLibraryEntry(javaProject.getPath().append("lib/classpath.jar"), null, null, null, new IClasspathAttribute[] {
				moduleClasspathAttribute }, false));
		sb.append(File.pathSeparator);
		sb.append(javaProject.getProject().getFile("lib/classpath.jar").getLocation().toString());
		classpathEntries.addAll(Arrays.asList(javaProject.getRawClasspath()));
		javaProject.setRawClasspath(classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]), null);
	}

	private void useComplianceFromExecutionEnvironment(IJavaProject javaProject) throws JavaModelException {
		IExecutionEnvironment executionEnvironment = getExecutionEnvironment(javaProject);
		Map<String, String> eeOptions = BuildPathSupport.getEEOptions(executionEnvironment);
		eeOptions.forEach((optionName, optionValue) -> javaProject.setOption(optionName, optionValue));
	}

	private IExecutionEnvironment getExecutionEnvironment(IJavaProject javaProject) throws JavaModelException {
		IClasspathEntry[] entries = javaProject.getRawClasspath();
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				String eeId = JavaRuntime.getExecutionEnvironmentId(entry.getPath());
				if (eeId != null) {
					return JavaRuntime.getExecutionEnvironmentsManager().getEnvironment(eeId);
				}
			}
		}
		return null;
	}

	private void useModuleForJREContainer(IJavaProject javaProject) throws JavaModelException {
		IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
		for (int i = 0; i < rawClasspath.length; i++) {
			IClasspathEntry classpathEntry = rawClasspath[i];
			if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				IClasspathAttribute moduleClasspathAttribute = JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true");
				classpathEntry = JavaCore.newContainerEntry(classpathEntry.getPath(), classpathEntry.getAccessRules(), new IClasspathAttribute[] {
						moduleClasspathAttribute }, classpathEntry.isExported());
				rawClasspath[i] = classpathEntry;
			}
		}
		javaProject.setRawClasspath(rawClasspath, null);
	}

	private Optional<File> getTempFile(ILaunch launch) {
		IProcess process = launch.getProcesses()[0];
		String tempFile = process.getAttribute(LaunchingPlugin.ATTR_LAUNCH_TEMP_FILES);
		if (tempFile == null) {
			return Optional.empty();
		}
		return Optional.of(new File(tempFile));
	}

}
