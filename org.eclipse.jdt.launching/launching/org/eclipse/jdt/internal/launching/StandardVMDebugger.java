/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IProcess;
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
 * A launcher for running Java main classes. Uses JDI to launch a vm in debug 
 * mode.
 */
public class StandardVMDebugger extends StandardVMRunner {
	
	
	/**
	 * Used to attach to a VM in a seperate thread, to allow for cancellation
	 * and detect that the associated System process died before the connect
	 * occurred.
	 */
	class ConnectRunnable implements Runnable {
		
		private VirtualMachine fVirtualMachine = null;
		private ListeningConnector fConnector = null;
		private Map fConnectionMap = null;
		private Exception fException = null;
		
		/**
		 * Constructs a runnable to connect to a VM via the given connector
		 * with the given connection arguments.
		 * 
		 * @param connector
		 * @param map
		 */
		public ConnectRunnable(ListeningConnector connector, Map map) {
			fConnector = connector;
			fConnectionMap = map;
		}
		
		public void run() {
			try {
				fVirtualMachine = fConnector.accept(fConnectionMap);
			} catch (IOException e) {
				fException = e;
			} catch (IllegalConnectorArgumentsException e) {
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
	 */
	public StandardVMDebugger(IVMInstall vmInstance) {
		super(vmInstance);
	}

	/**
	 * @see IVMRunner#run(VMRunnerConfiguration, ILaunch, IProgressMonitor)
	 */
	public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
		subMonitor.beginTask(LaunchingMessages.getString("StandardVMDebugger.Launching_VM..._1"), 4); //$NON-NLS-1$
		subMonitor.subTask(LaunchingMessages.getString("StandardVMDebugger.Finding_free_socket..._2")); //$NON-NLS-1$

		int port= SocketUtil.findFreePort();
		if (port == -1) {
			abort(LaunchingMessages.getString("StandardVMDebugger.Could_not_find_a_free_socket_for_the_debugger_1"), null, IJavaLaunchConfigurationConstants.ERR_NO_SOCKET_AVAILABLE); //$NON-NLS-1$
		}
		
		subMonitor.worked(1);
		
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}		
		
		subMonitor.subTask(LaunchingMessages.getString("StandardVMDebugger.Constructing_command_line..._3")); //$NON-NLS-1$
				
		String program= constructProgramString(config);

		List arguments= new ArrayList(12);

		arguments.add(program);

		// VM args are the first thing after the java program so that users can specify
		// options like '-client' & '-server' which are required to be the first options
		addArguments(config.getVMArguments(), arguments);

		String[] bootCP= config.getBootClassPath();
		if (bootCP != null) {
			if (bootCP.length > 0) {
				arguments.add("-Xbootclasspath:" + convertClassPath(bootCP)); //$NON-NLS-1$
			} else {
				// empty
				arguments.add("-Xbootclasspath:"); //$NON-NLS-1$
			}
		}
		
		String[] cp= config.getClassPath();
		if (cp.length > 0) {
			arguments.add("-classpath"); //$NON-NLS-1$
			arguments.add(convertClassPath(cp));
		}
		arguments.add("-Xdebug"); //$NON-NLS-1$
		arguments.add("-Xnoagent"); //$NON-NLS-1$
		arguments.add("-Djava.compiler=NONE"); //$NON-NLS-1$
		arguments.add("-Xrunjdwp:transport=dt_socket,suspend=y,address=localhost:" + port); //$NON-NLS-1$

		arguments.add(config.getClassToLaunch());
		addArguments(config.getProgramArguments(), arguments);
		String[] cmdLine= new String[arguments.size()];
		arguments.toArray(cmdLine);
		
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}		
		
		subMonitor.worked(1);
		subMonitor.subTask(LaunchingMessages.getString("StandardVMDebugger.Starting_virtual_machine..._4")); //$NON-NLS-1$

		ListeningConnector connector= getConnector();
		if (connector == null) {
			abort(LaunchingMessages.getString("StandardVMDebugger.Couldn__t_find_an_appropriate_debug_connector_2"), null, IJavaLaunchConfigurationConstants.ERR_CONNECTOR_NOT_AVAILABLE); //$NON-NLS-1$
		}
		Map map= connector.defaultArguments();
		
		specifyArguments(map, port);
		Process p= null;
		try {
			try {
				// check for cancellation
				if (monitor.isCanceled()) {
					return;
				}				
				
				connector.startListening(map);
				
				File workingDir = getWorkingDir(config);
				p = exec(cmdLine, workingDir);				
				if (p == null) {
					return;
				}
				
				// check for cancellation
				if (monitor.isCanceled()) {
					p.destroy();
					return;
				}				
				
				IProcess process= newProcess(launch, p, renderProcessLabel(cmdLine), getDefaultProcessMap());
				process.setAttribute(IProcess.ATTR_CMDLINE, renderCommandLine(cmdLine));
				subMonitor.worked(1);
				subMonitor.subTask(LaunchingMessages.getString("StandardVMDebugger.Establishing_debug_connection..._5")); //$NON-NLS-1$
				boolean retry= false;
				do  {
					try {
						
						ConnectRunnable runnable = new ConnectRunnable(connector, map);
						Thread connectThread = new Thread(runnable, "Listening Connector"); //$NON-NLS-1$
						connectThread.start();
						while (connectThread.isAlive()) {
							if (monitor.isCanceled()) {
								connector.stopListening(map);
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
						if (ex instanceof IllegalConnectorArgumentsException)						 {
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
							JDIDebugModel.newDebugTarget(launch, vm, renderDebugTarget(config.getClassToLaunch(), port), process, true, false);
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
						} else {
							Object result = handler.handleStatus(status, this);
							if (result instanceof Boolean) {
								retry = ((Boolean)result).booleanValue();
							}
						} 
					}
				} while (retry);
			} finally {
				connector.stopListening(map);
			}
		} catch (IOException e) {
			abort(LaunchingMessages.getString("StandardVMDebugger.Couldn__t_connect_to_VM_4"), e, IJavaLaunchConfigurationConstants.ERR_CONNECTION_FAILED);  //$NON-NLS-1$
		} catch (IllegalConnectorArgumentsException e) {
			abort(LaunchingMessages.getString("StandardVMDebugger.Couldn__t_connect_to_VM_5"), e, IJavaLaunchConfigurationConstants.ERR_CONNECTION_FAILED);  //$NON-NLS-1$
		}
		if (p != null) {
			p.destroy();
		}
	}
	
	protected void checkErrorMessage(IProcess process) throws CoreException {
		String errorMessage= process.getStreamsProxy().getErrorStreamMonitor().getContents();
		if (errorMessage.length() == 0) {
			errorMessage= process.getStreamsProxy().getOutputStreamMonitor().getContents();
		}
		if (errorMessage.length() != 0) {
			abort(errorMessage, null, IJavaLaunchConfigurationConstants.ERR_VM_LAUNCH_ERROR);
		}										
	}
		
	protected void specifyArguments(Map map, int portNumber) {
		// XXX: Revisit - allows us to put a quote (") around the classpath
		Connector.IntegerArgument port= (Connector.IntegerArgument) map.get("port"); //$NON-NLS-1$
		port.setValue(portNumber);
		
		Connector.IntegerArgument timeoutArg= (Connector.IntegerArgument) map.get("timeout"); //$NON-NLS-1$
		if (timeoutArg != null) {
			int timeout = JavaRuntime.getPreferences().getInt(JavaRuntime.PREF_CONNECT_TIMEOUT);
			timeoutArg.setValue(timeout);
		}
	}

	protected ListeningConnector getConnector() {
		List connectors= Bootstrap.virtualMachineManager().listeningConnectors();
		for (int i= 0; i < connectors.size(); i++) {
			ListeningConnector c= (ListeningConnector) connectors.get(i);
			if ("com.sun.jdi.SocketListen".equals(c.name())) //$NON-NLS-1$
				return c;
		}
		return null;
	}
	
}
