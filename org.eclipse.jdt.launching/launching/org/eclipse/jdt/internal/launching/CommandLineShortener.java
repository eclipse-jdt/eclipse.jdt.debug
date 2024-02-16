/*******************************************************************************
 * Copyright (c) 2020, 2023 Gunnar Wagenknecht and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gunnar Wagenknecht - copied from ClasspathShortener and simplified to shorten all arguments
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.io.File;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;

/**
 * Shortens the command line by writing all commands into an arguments file.
 */
public class CommandLineShortener implements IProcessTempFileCreator {
	public static String getJavaVersion(IVMInstall vmInstall) {
		if (vmInstall instanceof IVMInstall2) {
			IVMInstall2 install = (IVMInstall2) vmInstall;
			return install.getJavaVersion();
		}
		return null;
	}

	private final String javaVersion;
	private final ILaunch launch;
	private final String[] cmdLine;
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
	 * @param workingDir
	 *            the working dir to use for the launched VM or null
	 */
	public CommandLineShortener(IVMInstall vmInstall, ILaunch launch, String[] cmdLine, File workingDir) {
		this(getJavaVersion(vmInstall), launch, cmdLine, workingDir);
	}

	protected CommandLineShortener(String javaVersion, ILaunch launch, String[] cmdLine, File workingDir) {
		Assert.isNotNull(javaVersion);
		Assert.isNotNull(launch);
		Assert.isNotNull(cmdLine);
		this.javaVersion = javaVersion;
		this.launch = launch;
		this.cmdLine = cmdLine;
		this.processTempFilesDir = workingDir != null ? workingDir : Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
	}

	protected void addProcessTempFile(File file) {
		processTempFiles.add(file);
	}

	protected File createArgumentFile(String[] cmdLine) throws CoreException {
		Charset systemCharset = Platform.getSystemCharset();
		try {
			File argumentsFile = JavaLaunchingUtils.createFileForArgument(getLaunchTimeStamp(), processTempFilesDir, getLaunchConfigurationName(), "%s-args-%s.txt"); //$NON-NLS-1$
			cmdLine = quoteForArgfile(cmdLine);

			Files.write(argumentsFile.toPath(), Arrays.asList(cmdLine), systemCharset);
			return argumentsFile;
		} catch (CharacterCodingException e) {
			for (String s : cmdLine) {
				for (char c : s.toCharArray()) {
					if (!systemCharset.newEncoder().canEncode(c)) {
						throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR, "Cannot encode argument as file: Illegal character " //$NON-NLS-1$
								+ String.format("\\u%04x", (int) c) //$NON-NLS-1$
								+ " for system charset " + systemCharset.displayName() + ".", e)); //$NON-NLS-1$ //$NON-NLS-2$

					}
				}
			}
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR, "Cannot encode argument as file", e)); //$NON-NLS-1$
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR, "Cannot create argument file", e)); //$NON-NLS-1$
		}
	}

	String[] quoteForArgfile(String[] cmdLine) {
		String[] quotedCmdLine = new String[cmdLine.length];
		for (int i = 0; i < cmdLine.length; i++) {
			String arg = cmdLine[i];
			if (CommandLineQuoting.needsQuoting(arg)) {
				StringBuilder escapedArg = new StringBuilder();
				for (int j = 0; j < arg.length(); j++) {
					char c = arg.charAt(j);
					if (c == '\\') {
						escapedArg.append('\\');
					} else if (c == '\"') {
						escapedArg.append('\\');
					}
					escapedArg.append(c);
				}
				arg = "\"" + escapedArg.toString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			}
			quotedCmdLine[i] = arg;
		}
		return quotedCmdLine;
	}

	protected String getLaunchConfigurationName() {
		return launch.getLaunchConfiguration().getName();
	}

	protected String getLaunchTimeStamp() {
		return JavaLaunchingUtils.getLaunchTimeStamp(launch);
	}

	@Override
	public List<File> getProcessTempFiles() {
		return new ArrayList<>(processTempFiles);
	}

	public File getProcessTempFilesDir() {
		return processTempFilesDir;
	}

	/**
	 * @return the original, unshortened command line
	 */
	public String[] getOriginalCmdLine() {
		return cmdLine;
	}

	/**
	 * @return <code>true</code> if the JVM supports launching with argument files, <code>false</code> otherwise
	 */
	protected boolean isArgumentFileSupported() {
		return JavaCore.compareJavaVersions(javaVersion, JavaCore.VERSION_9) >= 0;
	}

	/**
	 * The directory to use to create temp files needed when shortening the classpath. By default, the working directory is used
	 *
	 * The java.io.tmpdir should not be used on MacOs (does not work for classpath-only jars)
	 */
	public void setProcessTempFilesDir(File processTempFilesDir) {
		this.processTempFilesDir = processTempFilesDir;
	}

	/**
	 * Writes the command line into an arguments file and returns the shortened command line.
	 *
	 * @return a shortened command line
	 */
	public String[] shortenCommandLine() throws CoreException {
		List<String> fullCommandLine = new ArrayList<>(Arrays.asList(cmdLine));
		List<String> shortCommandLine = new ArrayList<>();

		shortCommandLine.add(fullCommandLine.remove(0));

		File argumentFile = createArgumentFile(fullCommandLine.toArray(new String[fullCommandLine.size()]));
		addProcessTempFile(argumentFile);
		shortCommandLine.add("@" + argumentFile.getAbsolutePath());//$NON-NLS-1$

		return shortCommandLine.toArray(new String[shortCommandLine.size()]);
	}

	/**
	 * Indicates if the command line {@link #isArgumentFileSupported() can} and should be shortened.
	 * <p>
	 * The command line should only be shortened if at least Java 9 is used and the launch is configured to do so.
	 * </p>
	 *
	 * @return <code>true</code> if {@link #isArgumentFileSupported()} returns <code>true</code> and command line should be shortened,
	 *         <code>false</code> otherwise
	 */
	public boolean shouldShortenCommandLine() throws CoreException {
		if (!isArgumentFileSupported()) {
			return false;
		}

		if (cmdLine.length < 2) {
			// no need to shorten if it's just the program argument
			return false;
		}

		ILaunchConfiguration configuration = launch.getLaunchConfiguration();
		if (configuration != null) {
			return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_ARGFILE, false);
		}

		return false;
	}
}
