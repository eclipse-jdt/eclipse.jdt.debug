/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Alex Smirnoff   - Bug 289916
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.SocketUtil;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;

/**
 * A launcher for debugging Java main classes. Uses JDI to launch a VM in debug
 * mode.
 */
public class StandardVMDebugger extends StandardVMRunner {


	/**
	 * @since 3.3 OSX environment variable specifying JRE to use
	 */
	protected static final String JAVA_JVM_VERSION = "JAVA_JVM_VERSION"; //$NON-NLS-1$

	/**
	 * JRE path segment descriptor
	 *
	 * String equals the word: <code>jre</code>
	 *
	 * @since 3.3.1
	 */
	protected static final String JRE = "jre"; //$NON-NLS-1$

	/**
	 * Bin path segment descriptor
	 *
	 * String equals the word: <code>bin</code>
	 *
	 * @since 3.3.1
	 */
	protected static final String BIN = "bin"; //$NON-NLS-1$

	/**
	 * Used to attach to a VM in a separate thread, to allow for cancellation
	 * and detect that the associated System process died before the connect
	 * occurred.
	 */
	class ConnectRunnable implements Runnable {

		private VirtualMachine fVirtualMachine = null;
		private ListeningConnector fConnector = null;
		private Map<String, Connector.Argument> fConnectionMap = null;
		private Exception fException = null;

		/**
		 * Constructs a runnable to connect to a VM via the given connector
		 * with the given connection arguments.
		 *
		 * @param connector the connector to use
		 * @param map the argument map
		 */
		public ConnectRunnable(ListeningConnector connector, Map<String, Connector.Argument> map) {
			fConnector = connector;
			fConnectionMap = map;
		}

		@Override
		public void run() {
			try {
				fVirtualMachine = fConnector.accept(fConnectionMap);
			} catch (IOException | IllegalConnectorArgumentsException e) {
				fException = e;
			}
		}

		/**
		 * Returns the VM that was attached to, or <code>null</code> if none.
		 *
		 * @return the VM that was attached to, or <code>null</code> if none
		 */
		public VirtualMachine getVirtualMachine() {
			return fVirtualMachine;
		}

		/**
		 * Returns any exception that occurred while attaching, or <code>null</code>.
		 *
		 * @return IOException or IllegalConnectorArgumentsException
		 */
		public Exception getException() {
			return fException;
		}
	}

	/**
	 * Creates a new launcher
	 * @param vmInstance the backing {@link IVMInstall} to launch
	 */
	public StandardVMDebugger(IVMInstall vmInstance) {
		super(vmInstance);
	}

	@Override
	public String showCommandLine(VMRunnerConfiguration configuration, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor);

		CommandDetails cmd = getCommandLine(configuration, launch, subMonitor);
		if (subMonitor.isCanceled()) {
			return ""; //$NON-NLS-1$
		}
		String[] cmdLine = cmd.getCommandLine();
		cmdLine = quoteWindowsArgs(cmdLine);
		return getCmdLineAsString(cmdLine);
	}

	private CommandDetails getCommandLine(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IProgressMonitor subMonitor = SubMonitor.convert(monitor, 1);

		// check for cancellation
		if (subMonitor.isCanceled()) {
			return null;
		}

		subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Finding_free_socket____2);

		int port= SocketUtil.findFreePort();
		if (port == -1) {
			abort(LaunchingMessages.StandardVMDebugger_Could_not_find_a_free_socket_for_the_debugger_1, null, IJavaLaunchConfigurationConstants.ERR_NO_SOCKET_AVAILABLE);
		}

		subMonitor.worked(1);

		// check for cancellation
		if (subMonitor.isCanceled()) {
			return null;
		}

		subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Constructing_command_line____3);

		String program= constructProgramString(config);

		List<String> arguments= new ArrayList<>(12);

		arguments.add(program);

		if (fVMInstance instanceof StandardVM && ((StandardVM)fVMInstance).getDebugArgs() != null){
			String debugArgString = ((StandardVM)fVMInstance).getDebugArgs().replaceAll("\\Q" + StandardVM.VAR_PORT + "\\E", Integer.toString(port));  //$NON-NLS-1$ //$NON-NLS-2$
			arguments.addAll(Arrays.asList(DebugPlugin.parseArguments(debugArgString)));
		} else {
			// VM arguments are the first thing after the java program so that users can specify
			// options like '-client' & '-server' which are required to be the first options
			double version = getJavaVersion(fVMInstance);
			if (version < 1.5) {
				arguments.add("-Xdebug"); //$NON-NLS-1$
				arguments.add("-Xnoagent"); //$NON-NLS-1$
			}

			//check if java 1.4 or greater
			if (version < 1.4) {
				arguments.add("-Djava.compiler=NONE"); //$NON-NLS-1$
			}
			// check if java 14 or greater
			if (version >= 14) {
				if (launch.getLaunchConfiguration().getAttribute(IJavaLaunchConfigurationConstants.ATTR_SHOW_CODEDETAILS_IN_EXCEPTION_MESSAGES, true)) {
					arguments.add("-XX:+ShowCodeDetailsInExceptionMessages"); //$NON-NLS-1$
				}
			}
			if (version < 1.5) {
				arguments.add("-Xrunjdwp:transport=dt_socket,suspend=y,address=localhost:" + port); //$NON-NLS-1$
			} else {
				arguments.add("-agentlib:jdwp=transport=dt_socket,suspend=y,address=localhost:" + port); //$NON-NLS-1$
			}

		}

		String[] allVMArgs = combineVmArgs(config, fVMInstance);
		addArguments(ensureEncoding(launch, allVMArgs), arguments);
		addBootClassPathArguments(arguments, config);

		String[] mp = config.getModulepath();
		if (mp != null && mp.length > 0) { // There can be scenarios like junit where launched class is in classpath
											// with modular path entries
			arguments.add("-p"); //$NON-NLS-1$
			arguments.add(convertClassPath(mp));
		}

		String[] cp= config.getClassPath();
		if (cp.length > 0) {
			arguments.add("-classpath"); //$NON-NLS-1$
			arguments.add(convertClassPath(cp));
		}

		// https://openjdk.java.net/jeps/12
		if (config.isPreviewEnabled()) {
			arguments.add("--enable-preview"); //$NON-NLS-1$
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
		/*
		 * String[] cp= config.getClassPath(); int cpidx = -1; if (cp.length > 0) { cpidx = arguments.size(); arguments.add("-classpath");
		 * //$NON-NLS-1$ arguments.add(convertClassPath(cp)); }
		 *
		 * arguments.add(config.getClassToLaunch());
		 */
		addArguments(config.getProgramArguments(), arguments);

		//With the newer VMs and no backwards compatibility we have to always prepend the current env path (only the runtime one)
		//with a 'corrected' path that points to the location to load the debug dlls from, this location is of the standard JDK installation
		//format: <jdk path>/jre/bin
		String[] envp = prependJREPath(config.getEnvironment(), new Path(program));

		String[] cmdLine= new String[arguments.size()];
		arguments.toArray(cmdLine);

		// check for cancellation
		if (subMonitor.isCanceled()) {
			return null;
		}
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
		String[] newCmdLine = validateCommandLine(launch.getLaunchConfiguration(), cmdLine);
		if (newCmdLine != null) {
			cmdLine = newCmdLine;
		}
		subMonitor.worked(1);

		cmd.setCommandLine(cmdLine);
		cmd.setEnvp(envp);
		cmd.setWorkingDir(workingDir);
		cmd.setPort(port);
		return cmd;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.IVMRunner#run(org.eclipse.jdt.launching.VMRunnerConfiguration, org.eclipse.debug.core.ILaunch,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IProgressMonitor subMonitor = SubMonitor.convert(monitor, 1);
		CommandDetails cmdDetails = getCommandLine(config, launch, subMonitor);

		// check for cancellation
		if (subMonitor.isCanceled() || cmdDetails == null) {
			return;
		}
		String[] cmdLine = cmdDetails.getCommandLine();

		subMonitor.beginTask(LaunchingMessages.StandardVMDebugger_Launching_VM____1, 4);
		subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Starting_virtual_machine____4);
		ListeningConnector connector= getConnector();
		if (connector == null) {
			abort(LaunchingMessages.StandardVMDebugger_Couldn__t_find_an_appropriate_debug_connector_2, null, IJavaLaunchConfigurationConstants.ERR_CONNECTOR_NOT_AVAILABLE);
		}
		Map<String, Connector.Argument> map= connector.defaultArguments();

		specifyArguments(map, cmdDetails.getPort());
		Process p= null;
		try {
			try {
				// check for cancellation
				if (subMonitor.isCanceled()) {
					return;
				}

				connector.startListening(map);

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
					Arrays.sort(cmdDetails.getEnvp());
					StringBuilder buff = new StringBuilder();
					for (int i = 0; i < cmdDetails.getEnvp().length; i++) {
						buff.append(cmdDetails.getEnvp()[i]);
						if (i < cmdDetails.getEnvp().length - 1) {
							buff.append('\n');
						}
					}
					process.setAttribute(DebugPlugin.ATTR_ENVIRONMENT, buff.toString());
				}
				List<File> processTempFiles = cmdDetails.getCommandLineShortener().getProcessTempFiles();
				if (!processTempFiles.isEmpty()) {
					String tempFiles = processTempFiles.stream().map(File::getAbsolutePath).collect(Collectors.joining(File.pathSeparator));
					process.setAttribute(LaunchingPlugin.ATTR_LAUNCH_TEMP_FILES, tempFiles);
				}
				subMonitor.worked(1);
				subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Establishing_debug_connection____5);
				int retryCount = 0;
				boolean retry= false;
				do  {
					try {

						ConnectRunnable runnable = new ConnectRunnable(connector, map);
						Thread connectThread = new Thread(runnable, "Listening Connector"); //$NON-NLS-1$
                        connectThread.setDaemon(true);
						connectThread.start();
						while (connectThread.isAlive()) {
							if (subMonitor.isCanceled()) {
                                try {
                                    connector.stopListening(map);
                                } catch (IOException ioe) {
                                    //expected
                                }
								p.destroy();
								return;
							}
							try {
								p.exitValue();
								// process has terminated - stop waiting for a connection
								try {
									connector.stopListening(map);
								} catch (IOException e) {
									// expected
								}
								checkErrorMessage(process);
							} catch (IllegalThreadStateException e) {
								// expected while process is alive
							}
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
							}
						}

						Exception ex = runnable.getException();
						if (ex instanceof IllegalConnectorArgumentsException) {
							throw (IllegalConnectorArgumentsException)ex;
						}
						if (ex instanceof InterruptedIOException) {
							throw (InterruptedIOException)ex;
						}
						if (ex instanceof IOException) {
							throw (IOException)ex;
						}

						VirtualMachine vm= runnable.getVirtualMachine();
						if (vm != null) {
							createDebugTarget(config, launch, cmdDetails.getPort(), process, vm);
							subMonitor.worked(1);
							subMonitor.done();
						}
						return;
					} catch (InterruptedIOException e) {
						checkErrorMessage(process);

						// timeout, consult status handler if there is one
						IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_VM_CONNECT_TIMEOUT, "", e); //$NON-NLS-1$
						IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);

						retry= false;
						if (handler == null) {
							// if there is no handler, throw the exception
							throw new CoreException(status);
						}
						Object result = handler.handleStatus(status, this);
						if (result instanceof Boolean) {
							retry = ((Boolean)result);
						}
						if (!retry && retryCount < 5) {
							retry = true;
							retryCount++;
							LaunchingPlugin.log("Retrying count: " + retryCount); //$NON-NLS-1$

						}
					}
				} while (retry);
			} finally {
				connector.stopListening(map);
			}
		} catch (IOException e) {
			abort(LaunchingMessages.StandardVMDebugger_Couldn__t_connect_to_VM_4, e, IJavaLaunchConfigurationConstants.ERR_CONNECTION_FAILED);
		} catch (IllegalConnectorArgumentsException e) {
			abort(LaunchingMessages.StandardVMDebugger_Couldn__t_connect_to_VM_5, e, IJavaLaunchConfigurationConstants.ERR_CONNECTION_FAILED);
		}
		if (p != null) {
			p.destroy();
		}
	}

	/**
	 * This method performs platform specific operations to modify the runtime path for JREs prior to launching.
	 * Nothing is written back to the original system path.
	 *
	 * <p>
	 * For Windows:
	 * Prepends the location of the JRE bin directory for the given JDK path to the PATH variable in Windows.
	 * This method assumes that the JRE is located within the JDK install directory
	 * in: <code><JDK install dir>/jre/bin/</code> where the JRE itself would be located
	 * in: <code><JDK install dir>/bin/</code>  where the JDK itself is located
	 * </p>
	 * <p>
	 * For Mac OS:
	 * Searches for and sets the correct state of the JAVA_VM_VERSION environment variable to ensure it matches
	 * the currently chosen VM of the launch config
	 * </p>
	 *
	 * @param env the current array of environment variables to run with
	 * @param jdkpath the path to the executable (javaw).
	 * @return the altered JRE path
	 * @since 3.3
	 */
	protected String[] prependJREPath(String[] env, IPath jdkpath) {
		if(Platform.OS_WIN32.equals(Platform.getOS())) {
			IPath jrepath = jdkpath.removeLastSegments(1);
			if(jrepath.lastSegment().equals(BIN)) {
				int count = jrepath.segmentCount();
				if(count > 1 && !jrepath.segment(count-2).equalsIgnoreCase(JRE)) {
					jrepath = jrepath.removeLastSegments(1).append(JRE).append(BIN);
				}
			}
			else {
				jrepath = jrepath.append(JRE).append(BIN);
			}
			if(jrepath.toFile().exists()) {
				String jrestr = jrepath.toOSString();
				if(env == null){
					Map<String, String> map = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironment();
					env = new String[map.size()];
					int index = 0;
					for (String var : map.keySet()) {
						String value = map.get(var);
						if (value == null) {
							value = ""; //$NON-NLS-1$
						}
						if (var.equalsIgnoreCase("path")) { //$NON-NLS-1$
							if (!value.contains(jrestr)) {
								value = jrestr+';'+value;
							}
						}
						env[index] = var+"="+value; //$NON-NLS-1$
						index++;
					}
				} else {
					String var = null;
					int esign = -1;
					for(int i = 0; i < env.length; i++) {
						esign = env[i].indexOf('=');
						if(esign > -1) {
							var = env[i].substring(0, esign);
							if(var != null && var.equalsIgnoreCase("path")) { //$NON-NLS-1$
								if(env[i].indexOf(jrestr) == -1) {
									env[i] = var + "="+jrestr+';'+(esign == env[i].length() ? "" : env[i].substring(esign+1)); //$NON-NLS-1$ //$NON-NLS-2$
									break;
								}
							}
						}
					}
				}
			}
		}
		return super.prependJREPath(env);
	}

	/**
	 * Creates a new debug target for the given virtual machine and system process
	 * that is connected on the specified port for the given launch.
	 *
	 * @param config run configuration used to launch the VM
	 * @param launch launch to add the target to
	 * @param port port the VM is connected to
	 * @param process associated system process
	 * @param vm JDI virtual machine
	 * @return the {@link IDebugTarget}
	 */
	protected IDebugTarget createDebugTarget(VMRunnerConfiguration config, ILaunch launch, int port, IProcess process, VirtualMachine vm) {
		return JDIDebugModel.newDebugTarget(launch, vm, renderDebugTarget(config.getClassToLaunch(), port), process, true, false, config.isResumeOnStartup());
	}

	/**
	 * Checks and forwards an error from the specified process
	 * @param process the process to get the error message from
	 * @throws CoreException if a problem occurs
	 */
	protected void checkErrorMessage(IProcess process) throws CoreException {
		IStreamsProxy streamsProxy = process.getStreamsProxy();
		if (streamsProxy != null) {
			String errorMessage= streamsProxy.getErrorStreamMonitor().getContents();
			if (errorMessage.length() == 0) {
				errorMessage= streamsProxy.getOutputStreamMonitor().getContents();
			}
			if (errorMessage.length() != 0) {
				abort(errorMessage, null, IJavaLaunchConfigurationConstants.ERR_VM_LAUNCH_ERROR);
			}
		}
	}

	/**
	 * Allows arguments to be specified
	 * @param map argument map
	 * @param portNumber the port number
	 */
	protected void specifyArguments(Map<String, Connector.Argument> map, int portNumber) {
		// XXX: Revisit - allows us to put a quote (") around the classpath
		Connector.IntegerArgument port= (Connector.IntegerArgument) map.get("port"); //$NON-NLS-1$
		port.setValue(portNumber);

		Connector.IntegerArgument timeoutArg= (Connector.IntegerArgument) map.get("timeout"); //$NON-NLS-1$
		if (timeoutArg != null) {
			int timeout = Platform.getPreferencesService().getInt(
					LaunchingPlugin.ID_PLUGIN,
					JavaRuntime.PREF_CONNECT_TIMEOUT,
					JavaRuntime.DEF_CONNECT_TIMEOUT,
					null);
			timeoutArg.setValue(timeout);
		}
	}

	/**
	 * Returns the default 'com.sun.jdi.SocketListen' connector
	 * @return the {@link ListeningConnector}
	 */
	@SuppressWarnings("nls")
	protected ListeningConnector getConnector() {
		for (ListeningConnector c : Bootstrap.virtualMachineManager().listeningConnectors()) {
			if ("com.sun.jdi.SocketListen".equals(c.name())) {
				return c;
			}
		}
		return null;
	}

}
