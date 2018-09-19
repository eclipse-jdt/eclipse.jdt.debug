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

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.connectors.MockLaunch;
import org.eclipse.jdt.internal.launching.ClasspathShortener;

public class ClasspathShortenerTests extends AbstractDebugTest {
	private static final String MAIN_CLASS = "my.package.MainClass";
	private static final String ENCODING_ARG = "-Dfile.encoding=UTF-8";
	private static final String JAVA_10_PATH = "/usr/lib/jvm/java-10-jdk-amd64/bin/java";
	private static final String JAVA_8_PATH = "/usr/lib/jvm/java-8-openjdk-amd64/bin/java";
	private ClasspathShortenerForTest classpathShortener;
	private String userHome;

	public ClasspathShortenerTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		userHome = System.getProperty("user.home");
	}

	@Override
	protected void tearDown() throws Exception {
		if (classpathShortener != null) {
			classpathShortener.getProcessTempFiles().forEach(file -> file.delete());
		}
		super.tearDown();
	}

	public void testNoNeedToShortenShortClasspath() {
		// Given
		String classpath = getClasspathOrModulePath(userHomePath("/workspace/myProject/bin"), userHomePath("/workspace/myProject/lib/lib 1.jar"));
		String[] cmdLine = new String[] { JAVA_10_PATH, ENCODING_ARG, "-classpath", classpath, MAIN_CLASS, "-arg1", "arg2" };
		classpathShortener = new ClasspathShortenerForTest(Platform.OS_LINUX, "10.0.1", cmdLine, 4, null);

		// When
		boolean result = classpathShortener.shortenCommandLineIfNecessary();

		// Then
		assertFalse(result);
	}

	public void testShortenClasspathWhenCommandLineIsTooLong() {
		// Given
		String classpath = getClasspathOrModulePath(userHomePath("/workspace/myProject/bin"), userHomePath("/workspace/myProject/lib/lib 1.jar"));
		String[] cmdLine = new String[] { JAVA_10_PATH, ENCODING_ARG, "-classpath", classpath, MAIN_CLASS, "-arg1", "arg2" };
		classpathShortener = new ClasspathShortenerForTest(Platform.OS_LINUX, "10.0.1", cmdLine, 4, null);

		// When
		classpathShortener.setMaxCommandLineLength(100);
		boolean result = classpathShortener.shortenCommandLineIfNecessary();

		// Then
		assertTrue(result);
	}

	public void testShortenClasspathWhenClasspathArgumentIsTooLong() {
		// Given
		String classpath = getClasspathOrModulePath(userHomePath("/workspace/myProject/bin"), userHomePath("/workspace/myProject/lib/lib 1.jar"));
		String[] cmdLine = new String[] { JAVA_10_PATH, ENCODING_ARG, "-classpath", classpath, MAIN_CLASS, "-arg1", "arg2" };
		classpathShortener = new ClasspathShortenerForTest(Platform.OS_LINUX, "10.0.1", cmdLine, 4, null);

		// When
		classpathShortener.setMaxArgLength(40);
		boolean result = classpathShortener.shortenCommandLineIfNecessary();

		// Then
		assertTrue(result);
	}

	public void testForceClasspathOnlyJar() throws Exception {
		// Given
		String classpath = getClasspathOrModulePath(userHomePath("/workspace/myProject/bin"), userHomePath("/workspace/myProject/lib/lib 1.jar"));
		String[] cmdLine = new String[] { JAVA_10_PATH, ENCODING_ARG, "-classpath", classpath, MAIN_CLASS, "-arg1", "arg2" };
		classpathShortener = new ClasspathShortenerForTest(Platform.OS_LINUX, "10.0.1", cmdLine, 4, null);

		// When
		classpathShortener.setForceUseClasspathOnlyJar(true);
		boolean result = classpathShortener.shortenCommandLineIfNecessary();

		// Then
		assertTrue(result);
		assertEquals(1, classpathShortener.getProcessTempFiles().size());
		assertArrayEquals(new String[] { JAVA_10_PATH, ENCODING_ARG, "-classpath", classpathShortener.getProcessTempFiles().get(0).getAbsolutePath(),
				MAIN_CLASS, "-arg1", "arg2" }, classpathShortener.getCmdLine());
		List<File> classpathJars = getClasspathJarsFromJarManifest(classpathShortener.getProcessTempFiles().get(0));
		assertEquals(new File(userHomePath("/workspace/myProject/bin")), classpathJars.get(0).getCanonicalFile());
		assertEquals(new File(userHomePath("/workspace/myProject/lib/lib 1.jar")), classpathJars.get(1).getCanonicalFile());
	}

	public void testArgFileUsedForLongClasspathOnJava9() throws Exception {
		// Given
		String classpath = getClasspathOrModulePath(userHomePath("/workspace/myProject/bin"), userHomePath("/workspace/myProject/lib/lib 1.jar"));
		String[] cmdLine = new String[] { JAVA_10_PATH, ENCODING_ARG, "-cp", classpath, MAIN_CLASS, "-arg1", "arg2" };
		classpathShortener = new ClasspathShortenerForTest(Platform.OS_LINUX, "10.0.1", cmdLine, 4, null);
		classpathShortener.setMaxCommandLineLength(100);

		// When
		boolean result = classpathShortener.shortenCommandLineIfNecessary();

		// Then
		assertTrue(result);
		assertEquals(1, classpathShortener.getProcessTempFiles().size());
		assertArrayEquals(new String[] { JAVA_10_PATH, ENCODING_ARG, "@" + classpathShortener.getProcessTempFiles().get(0).getAbsolutePath(),
				MAIN_CLASS, "-arg1", "arg2" }, classpathShortener.getCmdLine());
		assertEquals("-classpath " + classpath, getFileContents(classpathShortener.getProcessTempFiles().get(0)));
	}

	public void testArgFileUsedForLongModulePath() throws Exception {
		// Given
		String modulepath = getClasspathOrModulePath(userHomePath("/workspace/myProject/bin"), userHomePath("/workspace/myProject/lib/lib 1.jar"));
		String[] cmdLine = new String[] { JAVA_10_PATH, ENCODING_ARG, "-p", modulepath, MAIN_CLASS, "-arg1", "arg2" };
		classpathShortener = new ClasspathShortenerForTest(Platform.OS_WIN32, "10.0.1", cmdLine, 4, null);
		classpathShortener.setMaxCommandLineLength(100);

		// When
		boolean result = classpathShortener.shortenCommandLineIfNecessary();

		// Then
		assertTrue(result);
		assertEquals(1, classpathShortener.getProcessTempFiles().size());
		assertArrayEquals(new String[] { JAVA_10_PATH, ENCODING_ARG, "@" + classpathShortener.getProcessTempFiles().get(0).getAbsolutePath(),
				MAIN_CLASS, "-arg1", "arg2" }, classpathShortener.getCmdLine());
		assertEquals("--module-path " + modulepath, getFileContents(classpathShortener.getProcessTempFiles().get(0)));
	}

	public void testLongClasspathAndLongModulePath() throws Exception {
		// Given
		String classpath = getClasspathOrModulePath(userHomePath("/workspace/myProject/lib/lib 2.jar"), userHomePath("/workspace/myProject/lib/lib 3.jar"));
		String modulepath = getClasspathOrModulePath(userHomePath("/workspace/myProject/bin"), userHomePath("/workspace/myProject/lib/lib 1.jar"));
		String[] cmdLine = new String[] { JAVA_10_PATH, ENCODING_ARG, "-cp", classpath, "-p", modulepath, MAIN_CLASS, "-arg1", "arg2" };
		classpathShortener = new ClasspathShortenerForTest(Platform.OS_WIN32, "10.0.1", cmdLine, 6, null);
		classpathShortener.setMaxCommandLineLength(100);

		// When
		boolean result = classpathShortener.shortenCommandLineIfNecessary();

		// Then
		assertTrue(result);
		assertEquals(2, classpathShortener.getProcessTempFiles().size());
		assertArrayEquals(new String[] { JAVA_10_PATH, ENCODING_ARG, "@" + classpathShortener.getProcessTempFiles().get(0).getAbsolutePath(),
				"@" + classpathShortener.getProcessTempFiles().get(1).getAbsolutePath(), MAIN_CLASS, "-arg1",
				"arg2" }, classpathShortener.getCmdLine());
		assertEquals("-classpath " + classpath, getFileContents(classpathShortener.getProcessTempFiles().get(0)));
		assertEquals("--module-path " + modulepath, getFileContents(classpathShortener.getProcessTempFiles().get(1)));
	}

	public void testClasspathOnlyJarUsedForLongClasspathOnJava8() throws Exception {
		// Given
		String classpath = getClasspathOrModulePath(userHomePath("/workspace/myProject/bin"), userHomePath("/workspace/myProject/lib/lib 1.jar"));
		String[] cmdLine = new String[] { JAVA_8_PATH, ENCODING_ARG, "-cp", classpath, MAIN_CLASS, "-arg1", "arg2" };
		classpathShortener = new ClasspathShortenerForTest(Platform.OS_LINUX, "1.8.0_171", cmdLine, 4, null);
		classpathShortener.setMaxCommandLineLength(100);

		// When
		classpathShortener.setAllowToUseClasspathOnlyJar(true);
		boolean result = classpathShortener.shortenCommandLineIfNecessary();

		// Then
		assertTrue(result);
		assertEquals(1, classpathShortener.getProcessTempFiles().size());
		assertArrayEquals(new String[] { JAVA_8_PATH, ENCODING_ARG, "-cp", classpathShortener.getProcessTempFiles().get(0).getAbsolutePath(),
				MAIN_CLASS, "-arg1", "arg2" }, classpathShortener.getCmdLine());
		List<File> classpathJars = getClasspathJarsFromJarManifest(classpathShortener.getProcessTempFiles().get(0));
		assertEquals(new File(userHomePath("/workspace/myProject/bin")), classpathJars.get(0).getCanonicalFile());
		assertEquals(new File(userHomePath("/workspace/myProject/lib/lib 1.jar")), classpathJars.get(1).getCanonicalFile());
	}

	public void testClasspathEnvVariableUsedForLongClasspathOnJava8OnWindows() {
		// Given
		String classpath = getClasspathOrModulePath(userHomePath("/workspace/myProject/bin"), userHomePath("/workspace/myProject/lib/lib 1.jar"));
		String[] cmdLine = new String[] { JAVA_8_PATH, ENCODING_ARG, "-cp", classpath, MAIN_CLASS, "-arg1", "arg2" };
		classpathShortener = new ClasspathShortenerForTest(Platform.OS_WIN32, "1.8.0_171", cmdLine, 4, null);
		classpathShortener.setMaxCommandLineLength(100);

		// When
		Map<String, String> nativeEnvironment = new LinkedHashMap<>();
		nativeEnvironment.put("PATH", "C:\\WINDOWS\\System32;C:\\WINDOWS");
		classpathShortener.setNativeEnvironment(nativeEnvironment);
		boolean result = classpathShortener.shortenCommandLineIfNecessary();

		// Then
		assertTrue(result);
		assertEquals(0, classpathShortener.getProcessTempFiles().size());
		assertArrayEquals(new String[] { "PATH=C:\\WINDOWS\\System32;C:\\WINDOWS", "CLASSPATH=" + classpath }, classpathShortener.getEnvp());
		assertArrayEquals(new String[] { JAVA_8_PATH, ENCODING_ARG, MAIN_CLASS, "-arg1", "arg2" }, classpathShortener.getCmdLine());
	}

	public void testClasspathUsedOnWindowsWhenEnvp() {
		// Given
		String classpath = getClasspathOrModulePath(userHomePath("/workspace/myProject/bin"), userHomePath("/workspace/myProject/lib/lib 1.jar"));
		String[] cmdLine = new String[] { JAVA_8_PATH, ENCODING_ARG, "-cp", classpath, MAIN_CLASS, "-arg1", "arg2" };
		String[] envp = new String[] { "MYVAR1=value1", "MYVAR2=value2" };
		classpathShortener = new ClasspathShortenerForTest(Platform.OS_WIN32, "1.8.0_171", cmdLine, 4, envp);
		classpathShortener.setMaxCommandLineLength(100);

		// When
		boolean result = classpathShortener.shortenCommandLineIfNecessary();

		// Then
		assertTrue(result);
		assertEquals(0, classpathShortener.getProcessTempFiles().size());
		assertArrayEquals(new String[] { "MYVAR1=value1", "MYVAR2=value2", "CLASSPATH=" + classpath }, classpathShortener.getEnvp());
		assertArrayEquals(new String[] { JAVA_8_PATH, ENCODING_ARG, MAIN_CLASS, "-arg1", "arg2" }, classpathShortener.getCmdLine());
	}

	public void testClasspathInEnvironmentReplacedOnWindows() {
		// Given
		String classpath = getClasspathOrModulePath(userHomePath("/workspace/myProject/bin"), userHomePath("/workspace/myProject/lib/lib 1.jar"));
		String[] cmdLine = new String[] { JAVA_8_PATH, ENCODING_ARG, "-cp", classpath, MAIN_CLASS, "-arg1", "arg2" };
		classpathShortener = new ClasspathShortenerForTest(Platform.OS_WIN32, "1.8.0_171", cmdLine, 4, null);
		classpathShortener.setMaxCommandLineLength(100);

		// When
		Map<String, String> nativeEnvironment = new LinkedHashMap<>();
		nativeEnvironment.put("PATH", "C:\\WINDOWS\\System32;C:\\WINDOWS");
		nativeEnvironment.put("CLASSPATH", "C:\\myJars\\jar1.jar");
		classpathShortener.setNativeEnvironment(nativeEnvironment);
		boolean result = classpathShortener.shortenCommandLineIfNecessary();

		// Then
		assertTrue(result);
		assertEquals(0, classpathShortener.getProcessTempFiles().size());
		assertArrayEquals(new String[] { "PATH=C:\\WINDOWS\\System32;C:\\WINDOWS", "CLASSPATH=" + classpath }, classpathShortener.getEnvp());
		assertArrayEquals(new String[] { JAVA_8_PATH, ENCODING_ARG, MAIN_CLASS, "-arg1", "arg2" }, classpathShortener.getCmdLine());
	}

	private String getFileContents(File file) throws UnsupportedEncodingException, IOException {
		return new String(Files.readAllBytes(file.toPath()), "UTF-8");
	}

	private String getClasspathAttributeFromJarManifest(File jarFile) throws FileNotFoundException, IOException {
		try (JarInputStream jarStream = new JarInputStream(new FileInputStream(jarFile))) {
			Manifest mf = jarStream.getManifest();
			return mf.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
		}
	}

	private List<File> getClasspathJarsFromJarManifest(File jarFile) throws FileNotFoundException, IOException {
		File parentDir = jarFile.getParentFile();
		String classpath = getClasspathAttributeFromJarManifest(jarFile);
		return Arrays.stream(classpath.split(" ")).map(relativePath -> {
			try {
				return new File(URIUtil.makeAbsolute(new URI(relativePath), parentDir.toURI()));
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());
	}

	private String userHomePath(String path) {
		return userHome + path;
	}

	private String getClasspathOrModulePath(String... classpathElements) {
		return String.join(";", classpathElements);
	}

	private static class ClasspathShortenerForTest extends ClasspathShortener {
		private boolean forceUseClasspathOnlyJar = false;
		private boolean allowToUseClasspathOnlyJar = false;
		private int maxArgLength = Integer.MAX_VALUE;
		private int maxCommandLineLength = Integer.MAX_VALUE;
		private Map<String, String> nativeEnvironment = new HashMap<>();

		public ClasspathShortenerForTest(String os, String javaVersion, String[] cmdLine, int classpathArgumentIndex, String[] envp) {
			super(os, javaVersion, new MockLaunch(), cmdLine, classpathArgumentIndex, null, envp);
		}

		public void setAllowToUseClasspathOnlyJar(boolean allowToUseClasspathOnlyJar) {
			this.allowToUseClasspathOnlyJar = allowToUseClasspathOnlyJar;
		}

		public void setForceUseClasspathOnlyJar(boolean forceUseClasspathOnlyJar) {
			this.forceUseClasspathOnlyJar = forceUseClasspathOnlyJar;
		}

		@Override
		protected String getLaunchConfigurationName() {
			return "launch";
		}

		@Override
		protected String getLaunchTimeStamp() {
			return Long.toString(System.currentTimeMillis());
		}

		@Override
		protected boolean handleClasspathTooLongStatus() throws CoreException {
			return allowToUseClasspathOnlyJar;
		}

		@Override
		protected boolean getLaunchConfigurationUseClasspathOnlyJarAttribute() throws CoreException {
			return forceUseClasspathOnlyJar;
		}

		@Override
		protected int getMaxArgLength() {
			return maxArgLength;
		}

		@Override
		protected int getMaxCommandLineLength() {
			return maxCommandLineLength;
		}

		public void setMaxArgLength(int maxArgLength) {
			this.maxArgLength = maxArgLength;
		}

		public void setMaxCommandLineLength(int maxCommandLineLength) {
			this.maxCommandLineLength = maxCommandLineLength;
		}

		public void setNativeEnvironment(Map<String, String> nativeEnvironment) {
			this.nativeEnvironment = nativeEnvironment;
		}

		@Override
		protected Map<String, String> getNativeEnvironment() {
			return nativeEnvironment;
		}

		@Override
		protected char getPathSeparatorChar() {
			// always use ';' as separator (tests would fail on windows otherwise with paths c:\ ...)
			return ';';
		}

	}

}
