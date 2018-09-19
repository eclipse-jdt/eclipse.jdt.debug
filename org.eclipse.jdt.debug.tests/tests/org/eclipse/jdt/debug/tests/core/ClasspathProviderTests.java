/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathProvider;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

/**
 * Tests runtime classpath provider extension point
 */
public class ClasspathProviderTests extends AbstractDebugTest {

	public ClasspathProviderTests(String name) {
		super(name);
	}

	public void testEmptyProvider() throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration("Breakpoints");
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();

		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, "org.eclipse.jdt.debug.tests.EmptyClasspathProvider");
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, "org.eclipse.jdt.debug.tests.EmptyClasspathProvider");

		IRuntimeClasspathProvider cpProvider = JavaRuntime.getClasspathProvider(wc);
		IRuntimeClasspathProvider spProvider = JavaRuntime.getSourceLookupPathProvider(wc);

		assertNotNull("Did not retrieve classpath provider", cpProvider);
		assertNotNull("Did not retrieve source path provider", spProvider);

		assertEquals("Classpath should be empty", 0, cpProvider.computeUnresolvedClasspath(config).length);
		assertEquals("Source path should be empty", 0, spProvider.computeUnresolvedClasspath(config).length);
	}

	/**
	 * Test that a variable set to the location of an archive resolves properly.
	 */
	public void testVariableArchiveResolution() throws Exception {
		IResource archive = get14Project().getProject().getFolder("src").getFile("A.jar");
		assertTrue("Archive does not exist", archive.exists());
		String varName = "COMPLETE_ARCHIVE";
		JavaCore.setClasspathVariable(varName, archive.getFullPath(), null);

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(varName));
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
	}

	/**
	 * Test that a variable set to the location of an archive via variable
	 * extension resolves properly.
	 */
	public void testVariableExtensionResolution() throws Exception {
		IResource archive = get14Project().getProject().getFolder("src").getFile("A.jar");
		IProject root = get14Project().getProject();
		String varName = "RELATIVE_ARCHIVE";
		JavaCore.setClasspathVariable(varName, root.getFullPath(), null);

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(varName).append(new Path("src")).append(new Path("A.jar")));
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
	}

	// BOOTPATH TESTS

	/**
	 * Test that a variable added to the bootpath is resolved to be on the bootpath.
	 */
	public void testBootpathVariableResolution() throws Exception {
		IResource archive = get14Project().getProject().getFolder("src").getFile("A.jar");
		assertTrue("Archive does not exist", archive.exists());
		String varName = "bootpathVar";
		JavaCore.setClasspathVariable(varName, archive.getFullPath(), null);

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(varName));
		runtimeClasspathEntry.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
		assertEquals("Resolved entry should be on bootpath", IRuntimeClasspathEntry.BOOTSTRAP_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that an extended variable added to the bootpath is resolved to be on the bootpath.
	 */
	public void testBootpathVariableExtensionResolution() throws Exception {
		IResource archive = get14Project().getProject().getFolder("src").getFile("A.jar");
		IProject root = get14Project().getProject();
		String varName = "bootpathVarRoot";
		JavaCore.setClasspathVariable(varName, root.getFullPath(), null);

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(varName).append(new Path("src")).append(new Path("A.jar")));
		runtimeClasspathEntry.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
		assertEquals("Resolved entry should be on bootpath", IRuntimeClasspathEntry.BOOTSTRAP_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that a project added to the bootpath is resolved to be on the bootpath.
	 */
	public void testBootpathProjectResolution() throws Exception {
		IJavaProject project = get14Project();
		IResource outputFolder = ResourcesPlugin.getWorkspace().getRoot().findMember(project.getOutputLocation());

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newProjectRuntimeClasspathEntry(project);
		runtimeClasspathEntry.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", outputFolder.getLocation().toOSString(), resolved[0].getLocation());
		assertEquals("Resolved entry should be on bootpath", IRuntimeClasspathEntry.BOOTSTRAP_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that a container added to the bootpath is resolved to have all entries on
	 * the boothpath.
	 */
	public void testBootpathContainerResolution() throws Exception {
		IRuntimeClasspathEntry entry = JavaRuntime.newRuntimeContainerClasspathEntry(new Path(JavaRuntime.JRE_CONTAINER), IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(entry, get14Project());
		// each resolved entry should be on the bootpath
		for (int i = 0; i < resolved.length; i++) {
			assertEquals("Entry should be on bootpath", IRuntimeClasspathEntry.BOOTSTRAP_CLASSES, resolved[i].getClasspathProperty());
		}
	}

	/**
	 * Test that a jar added to the bootpath is resolved to be on the bootpath.
	 */
	public void testBootpathJarResolution() throws Exception {
		IResource archive = get14Project().getProject().getFolder("src").getFile("A.jar");
		assertTrue("Archive does not exist", archive.exists());

		IRuntimeClasspathEntry jarEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(archive.getFullPath());
		jarEntry.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(jarEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
		assertEquals("Resolved entry should be on bootpath", IRuntimeClasspathEntry.BOOTSTRAP_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that a folder added to the bootpath is resolved to be on the bootpath.
	 */
	public void testBootpathFolderResolution() throws Exception {
		IResource folder = get14Project().getProject().getFolder("src");
		assertTrue("Folder does not exist", folder.exists());

		IRuntimeClasspathEntry folderEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(folder.getFullPath());
		folderEntry.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(folderEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", folder.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", folder.getLocation(), new Path(resolved[0].getLocation()));
		assertEquals("Resolved entry should be on bootpath", IRuntimeClasspathEntry.BOOTSTRAP_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that a project with non-default output locations placed on the bootpath
	 * resolves to entries on the bootpath.
	 */
	public void testBootpathProjectNonDefaultOutputLocationsResolution() throws Exception {
		IJavaProject project = getMultiOutputProject();

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newProjectRuntimeClasspathEntry(project);
		runtimeClasspathEntry.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, project);
		assertEquals("Should be 3 resolved entries", 3, resolved.length); // two specific entries & default entry
		for (int i = 0; i < resolved.length; i++) {
			IRuntimeClasspathEntry entry = resolved[i];
			assertEquals("Resolved entry should be on bootpath", IRuntimeClasspathEntry.BOOTSTRAP_CLASSES, entry.getClasspathProperty());
		}

	}

	// USER CLASSES TESTS

	/**
	 * Test that a variable added to the user application classpath is resolved to be on
	 * the user application classpath.
	 */
	public void testUserClassesVariableResolution() throws Exception {
		IResource archive = get14Project().getProject().getFolder("src").getFile("A.jar");
		assertTrue("Archive does not exist", archive.exists());
		String varName = "bootpathVar";
		JavaCore.setClasspathVariable(varName, archive.getFullPath(), null);

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(varName));
		runtimeClasspathEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
		assertEquals("Resolved entry should be on user classpath", IRuntimeClasspathEntry.USER_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that an extended variable added to the user classpath is resolved to be
	 * on the user classpath.
	 */
	public void testUserClassesVariableExtensionResolution() throws Exception {
		IResource archive = get14Project().getProject().getFolder("src").getFile("A.jar");
		IProject root = get14Project().getProject();
		String varName = "bootpathVarRoot";
		JavaCore.setClasspathVariable(varName, root.getFullPath(), null);

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(varName).append(new Path("src")).append(new Path("A.jar")));
		runtimeClasspathEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
		assertEquals("Resolved entry should be on user classpath", IRuntimeClasspathEntry.USER_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that a project added to the user classpath is resolved to be on the
	 * user classpath.
	 */
	public void testUserClassesProjectResolution() throws Exception {
		IJavaProject project = get14Project();
		IResource outputFolder = ResourcesPlugin.getWorkspace().getRoot().findMember(project.getOutputLocation());

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newProjectRuntimeClasspathEntry(project);
		runtimeClasspathEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", outputFolder.getLocation().toOSString(), resolved[0].getLocation());
		assertEquals("Resolved entry should be on user classpath", IRuntimeClasspathEntry.USER_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that a container added to the user classpath is resolved to have all
	 * entries on the user classpath.
	 */
	public void testUserClassesContainerResolution() throws Exception {
		IRuntimeClasspathEntry entry = JavaRuntime.newRuntimeContainerClasspathEntry(new Path(JavaRuntime.JRE_CONTAINER), IRuntimeClasspathEntry.USER_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(entry, get14Project());
		// each resolved entry should be on the bootpath
		for (int i = 0; i < resolved.length; i++) {
			assertEquals("Entry should be on user classpath", IRuntimeClasspathEntry.USER_CLASSES, resolved[i].getClasspathProperty());
		}
	}

	/**
	 * Test that a jar added to the user classpath is resolved to be on the user classpath.
	 */
	public void testUserClassesJarResolution() throws Exception {
		IResource archive = get14Project().getProject().getFolder("src").getFile("A.jar");
		assertTrue("Archive does not exist", archive.exists());

		IRuntimeClasspathEntry jarEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(archive.getFullPath());
		jarEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(jarEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
		assertEquals("Resolved entry should be on user classpath", IRuntimeClasspathEntry.USER_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that a folder added to the user classpath is resolved to be on the
	 * user classpath.
	 */
	public void testUserClassesFolderResolution() throws Exception {
		IResource folder = get14Project().getProject().getFolder("src");
		assertTrue("Folder does not exist", folder.exists());

		IRuntimeClasspathEntry folderEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(folder.getFullPath());
		folderEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(folderEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", folder.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", folder.getLocation(), new Path(resolved[0].getLocation()));
		assertEquals("Resolved entry should be on user classpath", IRuntimeClasspathEntry.USER_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that a project with non-default output locations placed on the user classpath
	 * resolves to entries on the user classpath.
	 */
	public void testUserClassesProjectNonDefaultOutputLocationsResolution() throws Exception {
		IJavaProject project = getMultiOutputProject();

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newProjectRuntimeClasspathEntry(project);
		runtimeClasspathEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, project);
		assertEquals("Should be 3 resolved entries", 3, resolved.length); // two specific entries & default entry
		for (int i = 0; i < resolved.length; i++) {
			IRuntimeClasspathEntry entry = resolved[i];
			assertEquals("Resolved entry should be on user classpath", IRuntimeClasspathEntry.USER_CLASSES, entry.getClasspathProperty());
		}

	}

	// STANDARD CLASSES TESTS

	/**
	 * Test that a variable added to the default bootpath is resolved to be on
	 * the default bootpath.
	 */
	public void testStandardClassesVariableResolution() throws Exception {
		IResource archive = get14Project().getProject().getFolder("src").getFile("A.jar");
		assertTrue("Archive does not exist", archive.exists());
		String varName = "bootpathVar";
		JavaCore.setClasspathVariable(varName, archive.getFullPath(), null);

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(varName));
		runtimeClasspathEntry.setClasspathProperty(IRuntimeClasspathEntry.STANDARD_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
		assertEquals("Resolved entry should be on default bootpath", IRuntimeClasspathEntry.STANDARD_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that an extended variable added to the default bootpath is resolved to be
	 * on the default bootpath.
	 */
	public void testStandardClassesVariableExtensionResolution() throws Exception {
		IResource archive = get14Project().getProject().getFolder("src").getFile("A.jar");
		IProject root = get14Project().getProject();
		String varName = "bootpathVarRoot";
		JavaCore.setClasspathVariable(varName, root.getFullPath(), null);

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newVariableRuntimeClasspathEntry(new Path(varName).append(new Path("src")).append(new Path("A.jar")));
		runtimeClasspathEntry.setClasspathProperty(IRuntimeClasspathEntry.STANDARD_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
		assertEquals("Resolved entry should be on default bootpath", IRuntimeClasspathEntry.STANDARD_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that a project added to the default bootpath is resolved to be on the
	 * default bootpath.
	 */
	public void testStandardClassesProjectResolution() throws Exception {
		IJavaProject project = get14Project();
		IResource outputFolder = ResourcesPlugin.getWorkspace().getRoot().findMember(project.getOutputLocation());

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newProjectRuntimeClasspathEntry(project);
		runtimeClasspathEntry.setClasspathProperty(IRuntimeClasspathEntry.STANDARD_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", outputFolder.getLocation().toOSString(), resolved[0].getLocation());
		assertEquals("Resolved entry should be on default bootpath", IRuntimeClasspathEntry.STANDARD_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that a container added to the default bootpath is resolved to have all
	 * entries on the default bootpath.
	 */
	public void testStandardClassesContainerResolution() throws Exception {
		IRuntimeClasspathEntry entry = JavaRuntime.newRuntimeContainerClasspathEntry(new Path(JavaRuntime.JRE_CONTAINER), IRuntimeClasspathEntry.STANDARD_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(entry, get14Project());
		// each resolved entry should be on the bootpath
		for (int i = 0; i < resolved.length; i++) {
			assertEquals("Entry should be on default bootpath", IRuntimeClasspathEntry.STANDARD_CLASSES, resolved[i].getClasspathProperty());
		}
	}

	/**
	 * Test that a jar added to the default bootpath is resolved to be on the
	 * default bootpath.
	 */
	public void testStandardClassesJarResolution() throws Exception {
		IResource archive = get14Project().getProject().getFolder("src").getFile("A.jar");
		assertTrue("Archive does not exist", archive.exists());

		IRuntimeClasspathEntry jarEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(archive.getFullPath());
		jarEntry.setClasspathProperty(IRuntimeClasspathEntry.STANDARD_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(jarEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", archive.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", archive.getLocation(), new Path(resolved[0].getLocation()));
		assertEquals("Resolved entry should be on default bootpath", IRuntimeClasspathEntry.STANDARD_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that a folder added to the default bootpath is resolved to be on the
	 * default bootpath.
	 */
	public void testStandardClassesFolderResolution() throws Exception {
		IResource folder = get14Project().getProject().getFolder("src");
		assertTrue("Folder does not exist", folder.exists());

		IRuntimeClasspathEntry folderEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(folder.getFullPath());
		folderEntry.setClasspathProperty(IRuntimeClasspathEntry.STANDARD_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(folderEntry, get14Project());
		assertEquals("Should be one resolved entry", 1, resolved.length);
		assertEquals("Resolved path not correct", folder.getFullPath(), resolved[0].getPath());
		assertEquals("Resolved path not correct", folder.getLocation(), new Path(resolved[0].getLocation()));
		assertEquals("Resolved entry should be on default bootpath", IRuntimeClasspathEntry.STANDARD_CLASSES, resolved[0].getClasspathProperty());
	}

	/**
	 * Test that a project with non-default output locations placed on the default bootpath
	 * resolves to entries on the default bootpath.
	 */
	public void testStandardClassesProjectNonDefaultOutputLocationsResolution() throws Exception {
		IJavaProject project = getMultiOutputProject();

		IRuntimeClasspathEntry runtimeClasspathEntry = JavaRuntime.newProjectRuntimeClasspathEntry(project);
		runtimeClasspathEntry.setClasspathProperty(IRuntimeClasspathEntry.STANDARD_CLASSES);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(runtimeClasspathEntry, project);
		assertEquals("Should be 3 resolved entries", 3, resolved.length); // two specific entries & default entry
		for (int i = 0; i < resolved.length; i++) {
			IRuntimeClasspathEntry entry = resolved[i];
			assertEquals("Resolved entry should be on default bootpath", IRuntimeClasspathEntry.STANDARD_CLASSES, entry.getClasspathProperty());
		}

	}

	/**
	 * Tests that default classpath computation works for a project with mulitple
	 * output locations.
	 *
	 * @throws Exception
	 */
	public void testMultiOutputDefaultClasspath() throws Exception {
		IJavaProject project = getMultiOutputProject();
		String[] entries = JavaRuntime.computeDefaultRuntimeClassPath(project);
		IProject p = project.getProject();
		IFolder bin1 = p.getFolder("bin1");
		IFolder bin2 = p.getFolder("bin2");
		String location1 = bin1.getLocation().toOSString();
		String location2 = bin2.getLocation().toOSString();

		List<String> list = new ArrayList<>();
		for (int i = 0; i < entries.length; i++) {
			list.add(entries[i]);
		}

		assertTrue("Classpath is missing " + location1, list.contains(location1));
		assertTrue("Classpath is missing " + location2, list.contains(location2));

	}

	/**
	 * Tests that default classpath computation works for a project with a default
	 * output location.
	 *
	 * @throws Exception
	 */
	public void testSingleOutputDefaultClasspath() throws Exception {
		IJavaProject project = get14Project();
		String[] entries = JavaRuntime.computeDefaultRuntimeClassPath(project);

		IFolder bin = ResourcesPlugin.getWorkspace().getRoot().getFolder(project.getOutputLocation());
		String location = bin.getLocation().toOSString();

		List<String> list = new ArrayList<>();
		for (int i = 0; i < entries.length; i++) {
			list.add(entries[i]);
		}

		assertTrue("Classpath is missing " + location, list.contains(location));

	}

	/**
	 * Tests that a buildpath with a relative (../..) classpath entry will resolve properly.
	 *
	 * @throws Exception
	 */
	public void testRelativeClasspathEntry() throws Exception {
        // create a project with a relative classpath entry
        IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject("RelativeCP");
        assertFalse("Project should not exist", pro.exists());

        // create project with source folder and output location
        IJavaProject project = JavaProjectHelper.createJavaProject("RelativeCP");
        JavaProjectHelper.addSourceContainer(project, "src", "bin");
        IExecutionEnvironment j2se14 = JavaRuntime.getExecutionEnvironmentsManager().getEnvironment("J2SE-1.4");
        assertNotNull("Missing J2SE-1.4 environment", j2se14);
        IPath path = JavaRuntime.newJREContainerPath(j2se14);
        JavaProjectHelper.addContainerEntry(project, path);
        // add relative entry - point to A.jar in DebugTests/src
        JavaProjectHelper.addLibrary(project, new Path("../DebugTests/src/A.jar"));

        // test runtime class path resolution
        String[] entries = JavaRuntime.computeDefaultRuntimeClassPath(project);
        String jarPath = get14Project().getProject().getLocation().append("src").append("A.jar").toOSString();
        List<String> list = new ArrayList<>();
		for (int i = 0; i < entries.length; i++) {
			list.add(entries[i]);
		}

        // delete the project
        pro.delete(false, null);

        assertTrue("Classpath is missing " + jarPath, list.contains(jarPath));
	}

	/**
	 * Tests that a variable with a relative (../..) path will resolve properly.
	 *
	 * @throws Exception
	 */
	public void testVariableWithRelativePath() throws Exception {
        // create a project with a relative classpath entry
        IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject("RelativeVar");
        assertFalse("Project should not exist", pro.exists());

        // create project with source folder and output location
        IJavaProject project = JavaProjectHelper.createJavaProject("RelativeVar");
        JavaProjectHelper.addSourceContainer(project, "src", "bin");
        IExecutionEnvironment j2se14 = JavaRuntime.getExecutionEnvironmentsManager().getEnvironment("J2SE-1.4");
        assertNotNull("Missing J2SE-1.4 environment", j2se14);
        IPath path = JavaRuntime.newJREContainerPath(j2se14);
        JavaProjectHelper.addContainerEntry(project, path);
        // add relative entry - point to A.jar in DebugTests/src
        String varName = "RELATIVE_DEBUG_TESTS";
		JavaCore.setClasspathVariable(varName, new Path("../DebugTests"), null);
		JavaProjectHelper.addVariableEntry(project, new Path("RELATIVE_DEBUG_TESTS/src/A.jar"), null, null);

        // test runtime class path resolution
        String[] entries = JavaRuntime.computeDefaultRuntimeClassPath(project);
        String jarPath = get14Project().getProject().getLocation().append("src").append("A.jar").toOSString();
        List<String> list = new ArrayList<>();
		for (int i = 0; i < entries.length; i++) {
			list.add(entries[i]);
		}

        // delete the project
        pro.delete(false, null);

        assertTrue("Classpath is missing " + jarPath, list.contains(jarPath));
	}
}
