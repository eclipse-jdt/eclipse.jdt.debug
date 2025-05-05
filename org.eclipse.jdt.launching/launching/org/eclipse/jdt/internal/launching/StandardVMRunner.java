/*******************************************************************************
 *  Copyright (c) 2000, 2023 IBM Corporation and others.
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
package org.eclipse.jdt.internal.launching;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.AbstractVMRunner;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.osgi.util.NLS;

/**
 * A launcher for running Java main classes.
 */
public class StandardVMRunner extends AbstractVMRunner {

	/**
	 * Constant representing the <code>-XstartOnFirstThread</code> VM argument
	 *
	 * @since 3.2.200
	 */
	public static final String XSTART_ON_FIRST_THREAD = "-XstartOnFirstThread"; //$NON-NLS-1$

	/**
	 * The VM install instance
	 */
	protected IVMInstall fVMInstance;

	/**
	 * Constructor
	 * @param vmInstance the VM
	 */
	public StandardVMRunner(IVMInstall vmInstance) {
		fVMInstance= vmInstance;
	}

	/**
	 * Returns the 'rendered' name for the current target
	 * @param classToRun the class
	 * @param host the host name
	 * @return the name for the current target
	 */
	protected String renderDebugTarget(String classToRun, int host) {
		String format= LaunchingMessages.StandardVMRunner__0__at_localhost__1__1;
		return NLS.bind(format, classToRun, String.valueOf(host));
	}

	/**
	 * Returns the 'rendered' name for the specified command line
	 *
	 * @param commandLine
	 *            the command line
	 * @param timestamp
	 *            the run-at time for the process
	 * @return the name for the process
	 */
	public static String renderProcessLabel(Process p, String[] commandLine, String timestamp) {
		String processId = getProcessId(p);
		if (processId == null) {
			String format = LaunchingMessages.StandardVMRunner__0____1___2;
			return NLS.bind(format, commandLine[0], timestamp);
		}
		String format = LaunchingMessages.StandardVMRunner__0____1___2_3;
		return NLS.bind(format, new String[] { commandLine[0], timestamp, processId });
	}

	private static String getProcessId(Process p) {
		try {
			return p != null ? Long.toString(p.pid()) : null;
		} catch (UnsupportedOperationException e) {
			// ignore, pid() is not implemented in this JVM
		}
		return null;
	}

	/**
	 * Prepares the command line from the specified array of strings
	 * @param commandLine the command line
	 * @return the command line label
	 */
	protected String renderCommandLine(String[] commandLine) {
		return DebugPlugin.renderArguments(commandLine, null);
	}

	/**
	 * Adds the array of {@link String}s to the given {@link List}
	 * @param args the strings
	 * @param v the list
	 */
	protected void addArguments(String[] args, List<String> v) {
		if (args == null) {
			return;
		}
		v.addAll(Arrays.asList(args));
	}

	/**
	 * This method allows consumers to have a last look at the command line that will be used
	 * to start the runner just prior to launching. This method returns the new array of commands
	 * to use to start the runner with or <code>null</code> if the existing command line should be used.
	 * <br><br>
	 * By default this method returns <code>null</code> indicating that the existing command line should be used to launch
	 *
	 * @param configuration the backing {@link ILaunchConfiguration}
	 * @param cmdLine the existing command line
	 * @return the new command line to launch with or <code>null</code> if the existing one should be used
	 * @since 3.7.0
	 */
	protected String[] validateCommandLine(ILaunchConfiguration configuration, String[] cmdLine) {
		try {
			return wrap(configuration, cmdLine);
		}
		catch(CoreException ce) {
			LaunchingPlugin.log(ce);
		}
		return null;
	}

	/**
	 * Adds in special command line arguments if SWT or the <code>-ws</code> directive
	 * are used
	 *
	 * @param config the backing {@link ILaunchConfiguration}
	 * @param cmdLine the original VM arguments
	 * @return the (possibly) modified command line to launch with
	 */
	private String[] wrap(ILaunchConfiguration config, String[] cmdLine) throws CoreException {
		if (config != null && Platform.OS.isMac()) {
			for (String element : cmdLine) {
				if ("-ws".equals(element) || element.contains("swt.jar") || element.contains("org.eclipse.swt")) { //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
					return createSWTlauncher(cmdLine,
							cmdLine[0],
							config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_START_ON_FIRST_THREAD, true));
				}
			}
		}
		return cmdLine;
	}

	/**
	 * Returns path to executable.
	 * @param cmdLine the old command line
	 * @param vmVersion the version of the VM
	 * @return the new command line
	 */
	private String[] createSWTlauncher(String[] cmdLine, String vmVersion, boolean startonfirstthread) {
		// the following property is defined if Eclipse is started via java_swt
		String java_swt= System.getProperty("org.eclipse.swtlauncher");	//$NON-NLS-1$
		if (java_swt == null) {
			// not started via java_swt -> now we require that the VM supports the "-XstartOnFirstThread" option
			boolean found = false;
			ArrayList<String> args = new ArrayList<>();
			for (String element : cmdLine) {
				if(XSTART_ON_FIRST_THREAD.equals(element)) {
					found = true;
				}
				args.add(element);
			}
			//newer VMs and non-MacOSX VMs don't like "-XstartOnFirstThread"
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=211625
			if(!found && startonfirstthread) {
				//add it as the first VM argument
				args.add(1, XSTART_ON_FIRST_THREAD);
			}
			return args.toArray(new String[args.size()]);
		}
		try {
			// copy java_swt to /tmp in order to get the app name right
			Process process= Runtime.getRuntime().exec(new String[] { "/bin/cp", java_swt, "/tmp" }); //$NON-NLS-1$ //$NON-NLS-2$
			process.waitFor();
			java_swt= "/tmp/java_swt"; //$NON-NLS-1$
		} catch (IOException | InterruptedException e) {
			// ignore and run java_swt in place
		}
		String[] newCmdLine= new String[cmdLine.length+1];
		int argCount= 0;
		newCmdLine[argCount++]= java_swt;
		newCmdLine[argCount++]= "-XXvm=" + vmVersion; //$NON-NLS-1$
		for (int i= 1; i < cmdLine.length; i++) {
			newCmdLine[argCount++]= cmdLine[i];
		}
		return newCmdLine;
	}

	/**
	 * Returns the working directory to use for the launched VM,
	 * or <code>null</code> if the working directory is to be inherited
	 * from the current process.
	 *
	 * @param config the VM configuration
	 * @return the working directory to use
	 * @exception CoreException if the working directory specified by
	 *  the configuration does not exist or is not a directory
	 */
	protected File getWorkingDir(VMRunnerConfiguration config) throws CoreException {
		String path = config.getWorkingDirectory();
		if (path == null) {
			return null;
		}
		File dir = new File(path);
		if (!dir.isDirectory()) {
			abort(NLS.bind(LaunchingMessages.StandardVMRunner_Specified_working_directory_does_not_exist_or_is_not_a_directory___0__3, path), null, IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
		}
		return dir;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractVMRunner#getPluginIdentifier()
	 */
	@Override
	protected String getPluginIdentifier() {
		return LaunchingPlugin.getUniqueIdentifier();
	}

	/**
	 * Construct and return a String containing the full path of a java executable
	 * command such as 'java' or 'javaw.exe'.  If the configuration specifies an
	 * explicit executable, that is used.
	 *
	 * @param config the runner configuration
	 * @return full path to java executable
	 * @exception CoreException if unable to locate an executable
	 */
	protected String constructProgramString(VMRunnerConfiguration config) throws CoreException {

		// Look for the user-specified java executable command
		String command= null;
		Map<String, Object> map= config.getVMSpecificAttributesMap();
		if (map != null) {
			command = (String) map.get(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND);
		}

		// If no java command was specified, use default executable
		if (command == null) {
			File exe = null;
			if (fVMInstance instanceof StandardVM) {
				exe = ((StandardVM)fVMInstance).getJavaExecutable();
			} else {
				exe = StandardVMType.findJavaExecutable(fVMInstance.getInstallLocation());
			}
			if (exe == null) {
				abort(NLS.bind(LaunchingMessages.StandardVMRunner_Unable_to_locate_executable_for__0__1, fVMInstance.getName()), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			} else {
				return exe.getAbsolutePath();
			}
		}

		// Build the path to the java executable.  First try 'bin', and if that
		// doesn't exist, try 'jre/bin'
		String installLocation = fVMInstance.getInstallLocation().getAbsolutePath() + File.separatorChar;
		File exe = new File(installLocation + "bin" + File.separatorChar + command); //$NON-NLS-1$
		if (fileExists(exe)){
			return exe.getAbsolutePath();
		}
		exe = new File(exe.getAbsolutePath() + ".exe"); //$NON-NLS-1$
		if (fileExists(exe)){
			return exe.getAbsolutePath();
		}
		exe = new File(installLocation + "jre" + File.separatorChar + "bin" + File.separatorChar + command); //$NON-NLS-1$ //$NON-NLS-2$
		if (fileExists(exe)) {
			return exe.getAbsolutePath();
		}
		exe = new File(exe.getAbsolutePath() + ".exe"); //$NON-NLS-1$
		if (fileExists(exe)) {
			return exe.getAbsolutePath();
		}

		// not found
		abort(NLS.bind(LaunchingMessages.StandardVMRunner_Specified_executable__0__does_not_exist_for__1__4, command, fVMInstance.getName()), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		// NOTE: an exception will be thrown - null cannot be returned
		return null;
	}

	/**
	 * Convenience method to determine if the specified file exists or not
	 * @param file the file to check
	 * @return true if the file indeed exists, false otherwise
	 */
	protected boolean fileExists(File file) {
		return file.exists() && file.isFile();
	}

	protected String convertClassPath(String[] cp) {
		int pathCount= 0;
		StringBuilder buf= new StringBuilder();
		if (cp.length == 0) {
			return "";    //$NON-NLS-1$
		}
		for (String element : cp) {
			if (pathCount > 0) {
				buf.append(File.pathSeparator);
			}
			buf.append(element);
			pathCount++;
		}
		return buf.toString();
	}

	/**
	 * This method is used to ensure that the JVM file encoding matches that of the console preference for file encoding. If the user explicitly
	 * declares a file encoding in the launch configuration, then that file encoding is used. This is needed for Java >=19.
	 *
	 * @param launch
	 *            the {@link Launch}
	 * @param vmargs
	 *            the original listing of JVM arguments
	 * @return the listing of JVM arguments including file encoding if one was not specified
	 *
	 * @since 3.4
	 * @see System#out
	 * @see System#err
	 */
	protected String[] ensureEncoding(ILaunch launch, String[] vmargs) {
		String encoding = launch.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING);
		// XXX JDKs Javadoc specifies:
		// "values other than UTF-8 (or COMPAT for file.encoding) leads to unspecified behavior"
		//
		// However it works fine on systems that support the specified encoding.
		vmargs = ensureArgument("-Dfile.encoding=", encoding, vmargs); //$NON-NLS-1$
		vmargs = ensureArgument("-Dstdout.encoding=", encoding, vmargs); //$NON-NLS-1$
		vmargs = ensureArgument("-Dstderr.encoding=", encoding, vmargs); //$NON-NLS-1$
		return vmargs;
	}

	protected String[] ensureArgument(String argument, String value, String[] vmargs) {
		boolean foundArgument = false;
		for (String vmarg : vmargs) {
			if (vmarg.startsWith(argument)) {
				foundArgument = true;
			}
		}
		if (!foundArgument) {
			if (value == null) {
				return vmargs;
			}
			String[] newargs = new String[vmargs.length+1];
			System.arraycopy(vmargs, 0, newargs, 0, vmargs.length);
			newargs[newargs.length - 1] = argument + value;
			return newargs;
		}
		return vmargs;
	}

	protected static class CommandDetails {
		private String[] commandLine;
		private String[] envp;
		private File workingDir;
		private IProcessTempFileCreator commandLineShortener;
		private int port;

		public String[] getEnvp() {
			return envp;
		}

		public void setEnvp(String[] envp) {
			this.envp = envp;
		}

		public String[] getCommandLine() {
			return commandLine;
		}

		public void setCommandLine(String[] commandLine) {
			this.commandLine = commandLine;
		}

		public File getWorkingDir() {
			return workingDir;
		}

		public void setWorkingDir(File workingDir) {
			this.workingDir = workingDir;
		}

		public IProcessTempFileCreator getCommandLineShortener() {
			return commandLineShortener;
		}

		public void setCommandLineShortener(IProcessTempFileCreator commandLineShortener) {
			this.commandLineShortener = commandLineShortener;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

	}

	@Override
	public String showCommandLine(VMRunnerConfiguration configuration, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IProgressMonitor subMonitor = SubMonitor.convert(monitor, 1);

		CommandDetails cmd = getCommandLine(configuration, launch, subMonitor);
		if (subMonitor.isCanceled() || cmd == null) {
			return ""; //$NON-NLS-1$
		}
		String[] cmdLine = cmd.getCommandLine();
		cmdLine = quoteWindowsArgs(cmdLine);
		return getCmdLineAsString(cmdLine);
	}

	private CommandDetails getCommandLine(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IProgressMonitor subMonitor = SubMonitor.convert(monitor, 1);
		subMonitor.subTask(LaunchingMessages.StandardVMRunner_Constructing_command_line____2);
		String program = constructProgramString(config);

		List<String> arguments= new ArrayList<>();
		arguments.add(program);

		// VM args are the first thing after the java program so that users can specify
		// options like '-client' & '-server' which are required to be the first option
		String[] allVMArgs = combineVmArgs(config, fVMInstance);
		addArguments(ensureEncoding(launch, allVMArgs), arguments);

		addBootClassPathArguments(arguments, config);

		String[] mp = config.getModulepath();
		if (mp != null && mp.length > 0) { // There can be scenarios like junit where launched class is in classpath
											// with modular path entries
			arguments.add("-p"); //$NON-NLS-1$
			arguments.add(convertClassPath(mp));
		}
		String[] cp = config.getClassPath();
		if (cp.length > 0) {
			arguments.add("-classpath"); //$NON-NLS-1$
			arguments.add(convertClassPath(cp));
		}

		// https://openjdk.java.net/jeps/12
		if (config.isPreviewEnabled()) {
			arguments.add("--enable-preview"); //$NON-NLS-1$
		}

		ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
		// check if java 14 or greater
		if (getJavaVersion(fVMInstance) >= 14) {
			if (launchConfiguration != null
					&& launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_SHOW_CODEDETAILS_IN_EXCEPTION_MESSAGES, true)) {
				arguments.add("-XX:+ShowCodeDetailsInExceptionMessages"); //$NON-NLS-1$
			}
		}

		String dependencies = config.getOverrideDependencies();
		if (dependencies != null && dependencies.length() > 0) {
			arguments.addAll(Arrays.asList(DebugPlugin.parseArguments(dependencies)));
		}
		if (isModular(config, fVMInstance)) {
			arguments.add("-m"); //$NON-NLS-1$
			arguments.add(config.getModuleDescription() + "/" + config.getClassToLaunch()); //$NON-NLS-1$
		} else {
			arguments.add(config.getClassToLaunch());
		}
		int lastVMArgumentIndex = arguments.size() - 1;
		String[] programArgs= config.getProgramArguments();
		addArguments(programArgs, arguments);

		String[] envp = prependJREPath(config.getEnvironment());

		String[] cmdLine= new String[arguments.size()];
		arguments.toArray(cmdLine);
		File workingDir = getWorkingDir(config);
		CommandDetails cmd = new CommandDetails();
		CommandLineShortener commandLineShortener = new CommandLineShortener(fVMInstance, launch, cmdLine, workingDir);
		if (commandLineShortener.shouldShortenCommandLine()) {
			cmdLine = commandLineShortener.shortenCommandLine();
			cmd.setCommandLineShortener(commandLineShortener);
		} else {
			ClasspathShortener classpathShortener = new ClasspathShortener(fVMInstance, launch, cmdLine, lastVMArgumentIndex, workingDir, envp);
			if (classpathShortener.shortenCommandLineIfNecessary()) {
				cmdLine = classpathShortener.getCmdLine();
				envp = classpathShortener.getEnvp();
			}
			cmd.setCommandLineShortener(classpathShortener);
		}
		String[] newCmdLine = validateCommandLine(launchConfiguration, cmdLine);
		if (newCmdLine != null) {
			cmdLine = newCmdLine;
		}

		cmd.setCommandLine(cmdLine);
		cmd.setEnvp(envp);
		cmd.setWorkingDir(workingDir);
		subMonitor.worked(1);
		return cmd;
	}

	@Override
	public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IProgressMonitor subMonitor = SubMonitor.convert(monitor, 1);

		CommandDetails cmdDetails = getCommandLine(config, launch, subMonitor);
		// check for cancellation
		if (subMonitor.isCanceled() || cmdDetails == null) {
			return;
		}
		String[] cmdLine = cmdDetails.getCommandLine();

		subMonitor.beginTask(LaunchingMessages.StandardVMRunner_Launching_VM____1, 2);
		subMonitor.subTask(LaunchingMessages.StandardVMRunner_Starting_virtual_machine____3);
		Process p = null;
		p = exec(cmdLine, cmdDetails.getWorkingDir(), cmdDetails.getEnvp(), config.isMergeOutput());
		if (p == null) {
			return;
		}

		// check for cancellation
		if (subMonitor.isCanceled()) {
			p.destroy();
			return;
		}
		String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
		IProcess process = newProcess(launch, p, renderProcessLabel(p, cmdLine, timestamp), getDefaultProcessMap());
		process.setAttribute(DebugPlugin.ATTR_PATH, cmdLine[0]);
		process.setAttribute(IProcess.ATTR_CMDLINE, renderCommandLine(cmdLine));
		String ltime = launch.getAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP);
		process.setAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP, ltime != null ? ltime : timestamp);
		if (cmdDetails.getWorkingDir() != null) {
			process.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, cmdDetails.getWorkingDir().getAbsolutePath());
		}
		if (cmdDetails.getEnvp() != null) {
			String[] envp = cmdDetails.getEnvp();
			Arrays.sort(envp);
			process.setAttribute(DebugPlugin.ATTR_ENVIRONMENT, String.join(String.valueOf('\n'), envp));
		}
		List<File> processTempFiles = cmdDetails.getCommandLineShortener().getProcessTempFiles();
		if (!processTempFiles.isEmpty()) {
			String tempFiles = processTempFiles.stream().map(File::getAbsolutePath).collect(Collectors.joining(File.pathSeparator));
			process.setAttribute(LaunchingPlugin.ATTR_LAUNCH_TEMP_FILES, tempFiles);
		}
		subMonitor.worked(1);
		subMonitor.done();
	}

	/**
	 * Returns the index in the given array for the CLASSPATH variable
	 * @param env the environment array or <code>null</code>
	 * @return -1 or the index of the CLASSPATH variable
	 * @since 3.6.200
	 */
	int getCPIndex(String[] env) {
		if(env != null) {
			for (int i = 0; i < env.length; i++) {
				if(env[i].regionMatches(true, 0, "CLASSPATH=", 0, 10)) { //$NON-NLS-1$
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Prepends the correct java version variable state to the environment path for Mac VMs
	 *
	 * @param env the current array of environment variables to run with
	 * @return the new path segments
	 * @since 3.3
	 */
	protected String[] prependJREPath(String[] env) {
		if (Platform.OS.isMac()) {
			if (fVMInstance instanceof IVMInstall2 vm) {
				String javaVersion = vm.getJavaVersion();
				if (javaVersion != null) {
					if (env == null) {
						Map<String, String> map = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironmentCasePreserved();
						if (map.containsKey(StandardVMDebugger.JAVA_JVM_VERSION)) {
							String[] env2 = new String[map.size()];
							Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
							int i = 0;
							while (iterator.hasNext()) {
								Entry<String, String> entry = iterator.next();
								String key = entry.getKey();
								if (StandardVMDebugger.JAVA_JVM_VERSION.equals(key)) {
									env2[i] = key + "=" + javaVersion; //$NON-NLS-1$
								} else {
									env2[i] = key + "=" + entry.getValue(); //$NON-NLS-1$
								}
								i++;
							}
							env = env2;
						}
					} else {
						for (int i = 0; i < env.length; i++) {
							String string = env[i];
							if (string.startsWith(StandardVMDebugger.JAVA_JVM_VERSION)) {
								env[i]=StandardVMDebugger.JAVA_JVM_VERSION+"="+javaVersion; //$NON-NLS-1$
								break;
							}
						}
					}
				}
			}
		}
		return env;
	}

	/**
	 * Adds arguments to the bootpath
	 * @param arguments the arguments
	 * @param config the VM config
	 */
	protected void addBootClassPathArguments(List<String> arguments, VMRunnerConfiguration config) {
		String[] prependBootCP= null;
		String[] bootCP= null;
		String[] appendBootCP= null;
		Map<String, Object> map = config.getVMSpecificAttributesMap();
		if (map != null) {
			prependBootCP = (String[]) map.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_PREPEND);
			bootCP = (String[]) map.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH);
			if (JavaRuntime.isModularJava(fVMInstance)) {
				if (prependBootCP != null && prependBootCP.length > 0) {
					prependBootCP = null;
					LaunchingPlugin.log(LaunchingMessages.RunnerBootpathPError);
				}
				if (bootCP != null && bootCP.length > 0) {
					bootCP = null;
					LaunchingPlugin.log(LaunchingMessages.RunnerBootpathError);
				}
			}
			appendBootCP= (String[]) map.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_APPEND);
		}
		if (!JavaRuntime.isModularJava(fVMInstance)) {
			if (prependBootCP == null && bootCP == null && appendBootCP == null) {
				// use old single attribute instead of new attributes if not specified
				bootCP = config.getBootClassPath();
			}
		}
		 if (prependBootCP != null) {
			 arguments.add("-Xbootclasspath/p:" + convertClassPath(prependBootCP)); //$NON-NLS-1$
		 }

		if (bootCP != null) {
			if (bootCP.length > 0) {
				arguments.add("-Xbootclasspath:" + convertClassPath(bootCP)); //$NON-NLS-1$
			}
		}
		if (appendBootCP != null) {
			arguments.add("-Xbootclasspath/a:" + convertClassPath(appendBootCP)); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the version of the current VM in use
	 * @return the VM version
	 */
	protected double getJavaVersion(IVMInstall fVMInstance) {
		String version = null;
		if (fVMInstance instanceof IVMInstall2) {
			version = ((IVMInstall2)fVMInstance).getJavaVersion();
		} else {
			LibraryInfo libInfo = LaunchingPlugin.getLibraryInfo(fVMInstance.getInstallLocation().getAbsolutePath());
			if (libInfo == null) {
			    return 0D;
			}
			version = libInfo.getVersion();
		}
		if (version == null) {
			// unknown version
			return 0D;
		}
		int index = version.indexOf("."); //$NON-NLS-1$
		int nextIndex = version.indexOf(".", index+1); //$NON-NLS-1$
		try {
			if (index > 0 && nextIndex>index) {
				return Double.parseDouble(version.substring(0,nextIndex));
			}
			return Double.parseDouble(version);
		} catch (NumberFormatException e) {
			return 0D;
		}

	}

}
