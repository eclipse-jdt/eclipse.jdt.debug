/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
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
 *     Jesper S Moller - Bug 421938: [1.8] ExecutionEnvironmentDescription#getVMArguments does not preserve VM arguments
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.EEVMType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jdt.launching.environments.ExecutionEnvironmentDescription;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;

/**
 * Tests for ".ee" files - installed JRE definition files
 */
public class EEDefinitionTests extends AbstractDebugTest {

	public static IPath TEST_EE_FILE = null;
	{
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			TEST_EE_FILE = new Path("testfiles/test-jre/bin/test-foundation11-win32.ee");
		} else {
			TEST_EE_FILE = new Path("testfiles/test-jre/bin/test-foundation11.ee");
		}
	}

	public EEDefinitionTests(String name) {
		super(name);
	}

	/**
	 * Tests that the EE file is a valid file
	 */
	public void testValidateDefinitionFile() throws CoreException {
		File file = getEEFile();
		assertNotNull("Missing EE file", file);
		ExecutionEnvironmentDescription description = new ExecutionEnvironmentDescription(file);
		IStatus status = EEVMType.validateDefinitionFile(description);
		assertTrue("Invalid install location", status.isOK());
	}

	/**
	 * Tests that the EE install location validation returns an INFO status.
	 */
	public void testValidateInstallLocation() {
		File file = getEEFile();
		IVMInstallType vmType = getVMInstallType();
		assertNotNull("Missing EE file", file);
		assertNotNull("Missing EE VM type", vmType);
		IStatus status = vmType.validateInstallLocation(file);
		assertTrue("Invalid install location", status.getSeverity() == IStatus.INFO);
	}

	/**
	 * Tests libraries for the EE file
	 */
	public void testLibraries() throws CoreException {
		File file = getEEFile();
		assertNotNull("Missing EE file", file);
		ExecutionEnvironmentDescription description = new ExecutionEnvironmentDescription(file);
		LibraryLocation[] libs = description.getLibraryLocations();
		String[] expected = new String[]{"end.jar", "classes.txt", "others.txt", "add.jar", "ext1.jar", "ext2.jar", "opt-ext.jar"};
		assertEquals("Wrong number of libraries", expected.length, libs.length);
		for (int i = 0; i < expected.length; i++) {
			if (i == 4) {
				// ext1 and ext2 can be in either order due to file system ordering
				assertTrue("Wrong library", expected[i].equals(libs[i].getSystemLibraryPath().lastSegment()) ||
						expected[i].equals(libs[i+1].getSystemLibraryPath().lastSegment()));
			} else if (i == 5) {
				// ext1 and ext2 can be in either order due to file system ordering
				assertTrue("Wrong library", expected[i].equals(libs[i].getSystemLibraryPath().lastSegment()) ||
						expected[i].equals(libs[i-1].getSystemLibraryPath().lastSegment()));
			} else {
				assertEquals("Wrong library", expected[i], libs[i].getSystemLibraryPath().lastSegment());
			}
			if ("classes.txt".equals(expected[i])) {
				assertEquals("source.txt", libs[i].getSystemLibrarySourcePath().lastSegment());
			}
		}
	}

	/**
	 * Tests source attachments
	 */
	public void testSourceAttachments() throws CoreException {
		File file = getEEFile();
		assertNotNull("Missing EE file", file);
		ExecutionEnvironmentDescription description = new ExecutionEnvironmentDescription(file);
		LibraryLocation[] libs = description.getLibraryLocations();
		String[] expected = new String[]{"end.txt", "source.txt", "source.txt", "sourceaddsource.jar", "extra1-src.txt", "extra2-src.txt", ""};
		assertEquals("Wrong number of libraries", expected.length, libs.length);
		for (int i = 0; i < expected.length; i++) {
			if (i == 4) {
				// ext1 and ext2 can be in either order due to file system ordering
				assertTrue("Wrong attachment", expected[i].equals(libs[i].getSystemLibrarySourcePath().lastSegment()) ||
						expected[i].equals(libs[i+1].getSystemLibrarySourcePath().lastSegment()));
			} else if (i == 5) {
				// ext1 and ext2 can be in either order due to file system ordering
				assertTrue("Wrong attachment", expected[i].equals(libs[i].getSystemLibrarySourcePath().lastSegment()) ||
						expected[i].equals(libs[i-1].getSystemLibrarySourcePath().lastSegment()));
			} else if (i == 6){
				assertEquals("Wrong attachment", Path.EMPTY, libs[i].getSystemLibrarySourcePath());
			} else {
				assertEquals("Wrong attachment", expected[i], libs[i].getSystemLibrarySourcePath().lastSegment());
			}
		}
	}

	/**
	 * Tests default libraries for an EE VM type are empty.
	 */
	public void testDefaultLibraries() {
		File file = getEEFile();
		IVMInstallType vmType = getVMInstallType();
		assertNotNull("Missing EE file", file);
		assertNotNull("Missing EE VM type", vmType);
		LibraryLocation[] libs = vmType.getDefaultLibraryLocations(file);
		assertEquals("Wrong number of libraries", 0, libs.length);
	}

	/**
	 * Tests that a javadoc location can be specified.
	 */
	public void testJavadocLocation() throws CoreException {
		File file = getEEFile();
		assertNotNull("Missing EE file", file);
		ExecutionEnvironmentDescription ee = new ExecutionEnvironmentDescription(file);
		URL location = EEVMType.getJavadocLocation(ee.getProperties());
		URL expectedLocation = null;
		try {
			expectedLocation = new URL("http://a.javadoc.location");
		} catch (MalformedURLException e) {
			fail();
		}
		assertEquals("Incorrect javadoc location", expectedLocation, location);
	}

	/**
	 * Tests asking for the index from the EE file
	 *
	 * @throws Exception
	 * @since 3.9.0
	 */
	public void testIndexLocation() throws Exception {
		File file = getEEFile();
		assertNotNull("Missing EE file", file);
		ExecutionEnvironmentDescription ee = new ExecutionEnvironmentDescription(file);
		URL location = EEVMType.getIndexLocation(ee.getProperties());
		URL expectedLocation = null;
		try {
			expectedLocation = new URL("http://a.index.location");
		} catch (MalformedURLException e) {
			fail();
		}
		assertEquals("Incorrect index location", expectedLocation, location);
	}

	/**
	 * Tests that a name with spaces can be specified.
	 */
	public void testVMName() throws CoreException {
		File file = getEEFile();
		assertNotNull("Missing EE file", file);
		ExecutionEnvironmentDescription ee = new ExecutionEnvironmentDescription(file);
		String name = ee.getProperty(ExecutionEnvironmentDescription.EE_NAME);
		assertEquals("Incorrect vm name", "Eclipse JDT Test JRE Definition", name);
	}

	/**
	 * Tests default VM arguments. All arguments from the EE file should get passed through in the
	 * same order to the command line.
	 */
	public void testVMArguments() throws CoreException {
		File file = getEEFile();
		assertNotNull("Missing EE file", file);
		ExecutionEnvironmentDescription description = new ExecutionEnvironmentDescription(file);
		String defaultVMArguments = description.getVMArguments();
		String[] expected = new String[] {
				"-XspecialArg:123",
				"-XspecialArg2=456",
				"-XspecialArg3=789"
		};
		int prev = -1;
		for (int i = 0; i < expected.length; i++) {
			int next = defaultVMArguments.indexOf(expected[i]);
			assertTrue("Missing argument: " + expected[i] + ": was: " + defaultVMArguments,  next >= 0);
			assertTrue("Wrong argument order: " + expected[i] + ": " + defaultVMArguments,  next > prev);
			prev = next;
		}
	}

	/**
	 * Test compatible environments
	 */
	public void testCompatibleEEs() throws CoreException {
		IVMInstall install = null;
		EEVMType vmType = (EEVMType) getVMInstallType();
		try {
			File file = getEEFile();
			assertNotNull("Missing EE file", file);
			assertNotNull("Missing EE VM type", vmType);
			VMStandin standin = JavaRuntime.createVMFromDefinitionFile(file, "test-ee-file", "test-ee-file-id");
			install = standin.convertToRealVM();

			IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
			IExecutionEnvironment[] envs = manager.getExecutionEnvironments();
			boolean found11 = false;
			for (int i = 0; i < envs.length; i++) {
				IExecutionEnvironment env = envs[i];
				if (env.getId().equals("CDC-1.1/Foundation-1.1")) {
					found11 = true;
					assertTrue("Should be strictly compatible with " + env.getId(), env.isStrictlyCompatible(install));
				} else if (env.getId().indexOf("jdt.debug.tests") < 0) {
					assertFalse("Should *not* be strictly compatible with " + env.getId(), env.isStrictlyCompatible(install));
				}
			}
			assertTrue("Did not find foundation 1.1 environment", found11);
		} finally {
			if (install != null && vmType != null) {
				vmType.disposeVMInstall(install.getId());
			}
		}
	}

	protected File getEEFile() {
		return JavaTestPlugin.getDefault().getFileInPlugin(TEST_EE_FILE);
	}

	protected IVMInstallType getVMInstallType() {
		return JavaRuntime.getVMInstallType(EEVMType.ID_EE_VM_TYPE);
	}

	/**
	 * Tests raw EE properties map.
	 *
	 * @throws CoreException
	 */
	public void testParseProperties() throws CoreException {
		File file = getEEFile();
		assertNotNull("Missing EE file", file);
		ExecutionEnvironmentDescription desc = new ExecutionEnvironmentDescription(file);
		Map<String, String> map = desc.getProperties();

		// validate expected properties
		validateProperty(ExecutionEnvironmentDescription.EXECUTABLE, "jrew.txt" , map);
		validateProperty(ExecutionEnvironmentDescription.EXECUTABLE_CONSOLE, "jre.txt", map);
		validateProperty("-XspecialArg:123", "", map);
		validateProperty("-XspecialArg2", "456", map);

	}

	protected void validateProperty(String key, String value, Map<String, String> properties) {
		assertEquals("Unexpeted value for: " + key, value, properties.get(key));
	}

	/**
	 * Tests that a name with spaces can be specified.
	 */
	public void testEmptyProperty() throws CoreException {
		File file = getEEFile();
		assertNotNull("Missing EE file", file);
		ExecutionEnvironmentDescription ee = new ExecutionEnvironmentDescription(file);
		validateProperty("-Dee.empty", "", ee.getProperties());
	}
}
