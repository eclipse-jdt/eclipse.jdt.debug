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
 *     Cedric Chabanois (cchabanois@gmail.com) - Launching command line exceeds the process creation command limit on *nix - https://bugs.eclipse.org/bugs/show_bug.cgi?id=385738
 *     IBM Corporation - Launching command line exceeds the process creation command limit on Windows - https://bugs.eclipse.org/bugs/show_bug.cgi?id=327193
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import static org.eclipse.jdt.internal.launching.LaunchingPlugin.LAUNCH_TEMP_FILE_PREFIX;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;

/**
 * Shorten the classpath/modulepath if necessary.
 *
 * Depending on the java version, os and launch configuration, the classpath argument will be replaced by an argument file, a classpath-only jar or
 * env variable. The modulepath is replaced by an argument file if necessary.
 *
 */
public class ClasspathShortener {
	private static final String CLASSPATH_ENV_VAR_PREFIX = "CLASSPATH="; //$NON-NLS-1$
	public static final int ARG_MAX_LINUX = 2097152;
	public static final int ARG_MAX_WINDOWS = 32767;
	public static final int ARG_MAX_MACOS = 262144;
	public static final int MAX_ARG_STRLEN_LINUX = 131072;
	private final String os;
	private final String javaVersion;
	private final ILaunch launch;
	private final List<String> cmdLine;
	private int lastJavaArgumentIndex;
	private String[] envp;
	private File processTempFilesDir;
	private final List<File> processTempFiles = new ArrayList<>();

	/**
	 *
	 * @param vmInstall
	 *            the vm installation
	 * @param launch
	 *            the launch
	 * @param cmdLine
	 *            the command line (java executable + VM arguments + program arguments)
	 * @param lastJavaArgumentIndex
	 *            the index of the last java argument in cmdLine (next arguments if any are program arguments)
	 * @param workingDir
	 *            the working dir to use for the launched VM or null
	 * @param envp
	 *            array of strings, each element of which has environment variable settings in the format name=value, or null if the subprocess should
	 *            inherit the environment of the current process.
	 */
	public ClasspathShortener(IVMInstall vmInstall, ILaunch launch, String[] cmdLine, int lastJavaArgumentIndex, File workingDir, String[] envp) {
		this(Platform.getOS(), getJavaVersion(vmInstall), launch, cmdLine, lastJavaArgumentIndex, workingDir, envp);
	}

	protected ClasspathShortener(String os, String javaVersion, ILaunch launch, String[] cmdLine, int lastJavaArgumentIndex, File workingDir, String[] envp) {
		Assert.isNotNull(os);
		Assert.isNotNull(javaVersion);
		Assert.isNotNull(launch);
		Assert.isNotNull(cmdLine);
		this.os = os;
		this.javaVersion = javaVersion;
		this.launch = launch;
		this.cmdLine = new ArrayList<>(Arrays.asList(cmdLine));
		this.lastJavaArgumentIndex = lastJavaArgumentIndex;
		this.envp = envp == null ? null : Arrays.copyOf(envp, envp.length);
		this.processTempFilesDir = workingDir != null ? workingDir : Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
	}

	/**
	 * The directory to use to create temp files needed when shortening the classpath. By default, the working directory is used
	 *
	 * The java.io.tmpdir should not be used on MacOs (does not work for classpath-only jars)
	 *
	 * @param processTempFilesDir
	 */
	public void setProcessTempFilesDir(File processTempFilesDir) {
		this.processTempFilesDir = processTempFilesDir;
	}

	public File getProcessTempFilesDir() {
		return processTempFilesDir;
	}

	/**
	 * Get the new envp. May have been modified to shorten the classpath
	 *
	 * @return environment variables in the format name=value or null
	 */
	public String[] getEnvp() {
		return envp;
	}

	/**
	 * Get the new command line. Modified if command line or classpath argument were too long
	 *
	 * @return the command line (java executable + VM arguments + program arguments)
	 */
	public String[] getCmdLine() {
		return cmdLine.toArray(new String[cmdLine.size()]);
	}

	/**
	 * The files that were created while shortening the path. They can be deleted once the process is terminated
	 *
	 * @return created files
	 */
	public List<File> getProcessTempFiles() {
		return new ArrayList<>(processTempFiles);
	}

	/**
	 * Shorten the command line if necessary. Each OS has different limits for command line length or command line argument length. And depending on
	 * the OS, JVM version and launch configuration, we shorten the classpath using an argument file, a classpath-only jar or env variable.
	 *
	 * If we need to use a classpath-only jar to shorten the classpath, we ask confirmation from the user because it can have side effects
	 * (System.getProperty("java.class.path") will return a classpath with only one jar). If
	 * {@link IJavaLaunchConfigurationConstants#ATTR_USE_CLASSPATH_ONLY_JAR} is set, a classpath-only jar is used (without asking confirmation).
	 *
	 * @return true if command line has been shortened or false if it was not necessary or not possible. Use {@link #getCmdLine()} and
	 *         {@link #getEnvp()} to get the new command line/environment.
	 */
	public boolean shortenCommandLineIfNecessary() {
		// '|' used on purpose (not short-circuiting)
		return shortenClasspathIfNecessary() | shortenModulePathIfNecessary();
	}

	private int getClasspathArgumentIndex() {
		for (int i = 0; i <= lastJavaArgumentIndex; i++) {
			String element = cmdLine.get(i);
			if ("-cp".equals(element) || "-classpath".equals(element) || "--class-path".equals(element)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return i + 1;
			}
		}
		return -1;
	}

	private int getModulepathArgumentIndex() {
		for (int i = 0; i <= lastJavaArgumentIndex; i++) {
			String element = cmdLine.get(i);
			if ("-p".equals(element) || "--module-path".equals(element)) { //$NON-NLS-1$ //$NON-NLS-2$
				return i + 1;
			}
		}
		return -1;
	}

	private boolean shortenModulePathIfNecessary() {
		int modulePathArgumentIndex = getModulepathArgumentIndex();
		if (modulePathArgumentIndex == -1) {
			return false;
		}
		try {
			String modulePath = cmdLine.get(modulePathArgumentIndex);
			if (getCommandLineLength() <= getMaxCommandLineLength() && modulePath.length() <= getMaxArgLength()) {
				return false;
			}
			if (isArgumentFileSupported()) {
				shortenModulePathUsingModulePathArgumentFile(modulePathArgumentIndex);
				return true;
			}
		} catch (CoreException e) {
			LaunchingPlugin.log(e.getStatus());
		}
		return false;
	}

	private boolean shortenClasspathIfNecessary() {
		int classpathArgumentIndex = getClasspathArgumentIndex();
		if (classpathArgumentIndex == -1) {
			return false;
		}
		try {
			boolean forceUseClasspathOnlyJar = getLaunchConfigurationUseClasspathOnlyJarAttribute();
			if (forceUseClasspathOnlyJar) {
				shortenClasspathUsingClasspathOnlyJar(classpathArgumentIndex);
				return true;
			}
			String classpath = cmdLine.get(classpathArgumentIndex);
			if (getCommandLineLength() <= getMaxCommandLineLength() && classpath.length() <= getMaxArgLength()) {
				return false;
			}
			if (isArgumentFileSupported()) {
				shortenClasspathUsingClasspathArgumentFile(classpathArgumentIndex);
				return true;
			}
			if (os.equals(Platform.OS_WIN32)) {
				shortenClasspathUsingClasspathEnvVariable(classpathArgumentIndex);
				return true;
			} else if (handleClasspathTooLongStatus()) {
				shortenClasspathUsingClasspathOnlyJar(classpathArgumentIndex);
				return true;
			}
		} catch (CoreException e) {
			LaunchingPlugin.log(e.getStatus());
		}
		return false;
	}

	protected boolean getLaunchConfigurationUseClasspathOnlyJarAttribute() throws CoreException {
		ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
		if (launchConfiguration == null) {
			return false;
		}
		return launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_CLASSPATH_ONLY_JAR, false);
	}

	public static String getJavaVersion(IVMInstall vmInstall) {
		if (vmInstall instanceof IVMInstall2) {
			IVMInstall2 install = (IVMInstall2) vmInstall;
			return install.getJavaVersion();
		}
		return null;
	}

	private boolean isArgumentFileSupported() {
		return JavaCore.compareJavaVersions(javaVersion, JavaCore.VERSION_9) >= 0;
	}

	private int getCommandLineLength() {
		return cmdLine.stream().map(argument -> argument.length() + 1).reduce((a, b) -> a + b).get();
	}

	private int getEnvironmentLength() {
		if (envp == null) {
			return 0;
		}
		return Arrays.stream(envp).map(element -> element.length() + 1).reduce((a, b) -> a + b).orElse(0);
	}

	protected int getMaxCommandLineLength() {
		// for Posix systems, ARG_MAX is the maximum length of argument to the exec functions including environment data.
		// POSIX suggests to subtract 2048 additionally so that the process may safely modify its environment.
		// see https://www.in-ulm.de/~mascheck/various/argmax/
		switch (os) {
			case Platform.OS_LINUX:
				// ARG_MAX will be 1/4 of the stack size. 2097152 by default
				return ARG_MAX_LINUX - getEnvironmentLength() - 2048;
			case Platform.OS_MACOSX:
				// on MacOs, ARG_MAX is 262144
				return ARG_MAX_MACOS - getEnvironmentLength() - 2048;
			case Platform.OS_WIN32:
				// On Windows, the maximum length of the command line is 32,768 characters, including the Unicode terminating null character.
				// see http://msdn.microsoft.com/en-us/library/windows/desktop/ms682425(v=vs.85).aspx
				return ARG_MAX_WINDOWS - 2048;
			default:
				return Integer.MAX_VALUE;
		}
	}

	protected int getMaxArgLength() {
		if (os.equals(Platform.OS_LINUX)) {
			// On Linux, MAX_ARG_STRLEN (kernel >= 2.6.23) is the maximum length of a command line argument (or environment variable). Its value
			// cannot be changed without recompiling the kernel.
			return MAX_ARG_STRLEN_LINUX - 2048;
		}
		return Integer.MAX_VALUE;
	}

	private void shortenClasspathUsingClasspathArgumentFile(int classpathArgumentIndex) throws CoreException {
		String classpath = cmdLine.get(classpathArgumentIndex);
		File file = createClassPathArgumentFile(classpath);
		removeCmdLineArgs(classpathArgumentIndex - 1, 2);
		addCmdLineArgs(classpathArgumentIndex - 1, '@' + file.getAbsolutePath());
		addProcessTempFile(file);
	}

	private void shortenModulePathUsingModulePathArgumentFile(int modulePathArgumentIndex) throws CoreException {
		String modulePath = cmdLine.get(modulePathArgumentIndex);
		File file = createModulePathArgumentFile(modulePath);
		removeCmdLineArgs(modulePathArgumentIndex - 1, 2);
		addCmdLineArgs(modulePathArgumentIndex - 1, '@' + file.getAbsolutePath());
		addProcessTempFile(file);
	}

	private void shortenClasspathUsingClasspathOnlyJar(int classpathArgumentIndex) throws CoreException {
		String classpath = cmdLine.get(classpathArgumentIndex);
		File classpathOnlyJar = createClasspathOnlyJar(classpath);
		removeCmdLineArgs(classpathArgumentIndex, 1);
		addCmdLineArgs(classpathArgumentIndex, classpathOnlyJar.getAbsolutePath());
		addProcessTempFile(classpathOnlyJar);
	}

	protected void addProcessTempFile(File file) {
		processTempFiles.add(file);
	}

	protected boolean handleClasspathTooLongStatus() throws CoreException {
		IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_CLASSPATH_TOO_LONG, "", null); //$NON-NLS-1$
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
		if (handler == null) {
			return false;
		}
		Object result = handler.handleStatus(status, launch);
		if (!(result instanceof Boolean)) {
			return false;
		}
		return (boolean) result;
	}

	private File createClasspathOnlyJar(String classpath) throws CoreException {
		try {
			String timeStamp = getLaunchTimeStamp();
			File jarFile = new File(processTempFilesDir, String.format(LAUNCH_TEMP_FILE_PREFIX
					+ "%s-classpathOnly-%s.jar", getLaunchConfigurationName(), timeStamp)); //$NON-NLS-1$
			URI workingDirUri = processTempFilesDir.toURI();
			StringBuilder manifestClasspath = new StringBuilder();
			String[] classpathArray = getClasspathAsArray(classpath);
			for (int i = 0; i < classpathArray.length; i++) {
				if (i != 0) {
					manifestClasspath.append(' ');
				}
				File file = new File(classpathArray[i]);
				String relativePath = URIUtil.makeRelative(file.toURI(), workingDirUri).toString();
				manifestClasspath.append(relativePath);
			}
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0"); //$NON-NLS-1$
			manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, manifestClasspath.toString());
			try (JarOutputStream target = new JarOutputStream(new FileOutputStream(jarFile), manifest)) {
			}
			return jarFile;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR, "Cannot create classpath only jar", e)); // $NON-NLS-1$ //$NON-NLS-1$
		}
	}

	private String[] getClasspathAsArray(String classpath) {
		return classpath.split("" + getPathSeparatorChar()); //$NON-NLS-1$
	}

	protected char getPathSeparatorChar() {
		char separator = ':';
		if (os.equals(Platform.OS_WIN32)) {
			separator = ';';
		}
		return separator;
	}

	protected String getLaunchConfigurationName() {
		return launch.getLaunchConfiguration().getName();
	}

	private File createClassPathArgumentFile(String classpath) throws CoreException {
		try {
			String timeStamp = getLaunchTimeStamp();
			File classPathFile = new File(processTempFilesDir, String.format(LAUNCH_TEMP_FILE_PREFIX
					+ "%s-classpath-arg-%s.txt", getLaunchConfigurationName(), timeStamp)); //$NON-NLS-1$

			byte[] bytes = ("-classpath " + classpath).getBytes(StandardCharsets.UTF_8); //$NON-NLS-1$

			Files.write(classPathFile.toPath(), bytes);
			return classPathFile;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR, "Cannot create classpath argument file", e)); //$NON-NLS-1$
		}
	}

	private File createModulePathArgumentFile(String modulePath) throws CoreException {
		try {
			String timeStamp = getLaunchTimeStamp();
			File modulePathFile = new File(processTempFilesDir, String.format(LAUNCH_TEMP_FILE_PREFIX
					+ "%s-module-path-arg-%s.txt", getLaunchConfigurationName(), timeStamp)); //$NON-NLS-1$

			byte[] bytes = ("--module-path " + modulePath).getBytes(StandardCharsets.UTF_8); //$NON-NLS-1$

			Files.write(modulePathFile.toPath(), bytes);
			return modulePathFile;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR, "Cannot create module-path argument file", e)); //$NON-NLS-1$
		}
	}

	protected String getLaunchTimeStamp() {
		String timeStamp = launch.getAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP);
		if (timeStamp == null) {
			timeStamp = Long.toString(System.currentTimeMillis());
		}
		return timeStamp;
	}

	private String[] getEnvpFromNativeEnvironment() {
		Map<String, String> nativeEnvironment = getNativeEnvironment();
		String[] envp = new String[nativeEnvironment.size()];
		int idx = 0;
		for (Entry<String, String> entry : nativeEnvironment.entrySet()) {
			String value = entry.getValue();
			if (value == null) {
				value = ""; //$NON-NLS-1$
			}
			String key = entry.getKey();
			envp[idx] = key + '=' + value;
			idx++;
		}
		return envp;
	}

	protected Map<String, String> getNativeEnvironment() {
		return DebugPlugin.getDefault().getLaunchManager().getNativeEnvironment();
	}

	private void shortenClasspathUsingClasspathEnvVariable(int classpathArgumentIndex) {
		String classpath = cmdLine.get(classpathArgumentIndex);
		if (envp == null) {
			envp = getEnvpFromNativeEnvironment();
		}
		String classpathEnvVar = CLASSPATH_ENV_VAR_PREFIX + classpath;
		int index = getEnvClasspathIndex(envp);
		if (index < 0) {
			envp = Arrays.copyOf(envp, envp.length + 1);
			envp[envp.length - 1] = classpathEnvVar;
		} else {
			envp[index] = classpathEnvVar;
		}
		removeCmdLineArgs(classpathArgumentIndex - 1, 2);
	}

	private void removeCmdLineArgs(int index, int length) {
		for (int i = 0; i < length; i++) {
			cmdLine.remove(index);
			lastJavaArgumentIndex--;
		}
	}

	private void addCmdLineArgs(int index, String... newArgs) {
		cmdLine.addAll(index, Arrays.asList(newArgs));
		lastJavaArgumentIndex += newArgs.length;
	}

	/**
	 * Returns the index in the given array for the CLASSPATH variable
	 *
	 * @param env
	 *            the environment array or <code>null</code>
	 * @return -1 or the index of the CLASSPATH variable
	 */
	private int getEnvClasspathIndex(String[] env) {
		if (env != null) {
			for (int i = 0; i < env.length; i++) {
				if (env[i].regionMatches(true, 0, CLASSPATH_ENV_VAR_PREFIX, 0, 10)) {
					return i;
				}
			}
		}
		return -1;
	}

}
