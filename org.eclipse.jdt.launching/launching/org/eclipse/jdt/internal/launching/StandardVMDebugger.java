package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IDebugTarget;
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
	 * Creates a new launcher
	 */
	public StandardVMDebugger(IVMInstall vmInstance) {
		super(vmInstance);
	}

	/**
	 * @see IVMRunner#run(VMRunnerConfiguration, ILaunch, IProgressMonitor)
	 */
	public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		int port= SocketUtil.findUnusedLocalPort("", 5000, 15000); //$NON-NLS-1$
		if (port == -1) {
			abort(LaunchingMessages.getString("StandardVMDebugger.Could_not_find_a_free_socket_for_the_debugger_1"), null, IJavaLaunchConfigurationConstants.ERR_NO_SOCKET_AVAILABLE); //$NON-NLS-1$
		}
		
		String program= constructProgramString(config);

		List arguments= new ArrayList(12);

		arguments.add(program);

		// VM args are the first thing after the java program so that users can specify
		// options like '-client' & '-server' which are required to be the first options
		addArguments(config.getVMArguments(), arguments);

		String[] bootCP= config.getBootClassPath();
		if (bootCP.length > 0) {
			arguments.add("-Xbootclasspath:" + convertClassPath(bootCP)); //$NON-NLS-1$
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

		ListeningConnector connector= getConnector();
		if (connector == null) {
			abort(LaunchingMessages.getString("StandardVMDebugger.Couldn__t_find_an_appropriate_debug_connector_2"), null, IJavaLaunchConfigurationConstants.ERR_CONNECTOR_NOT_AVAILABLE); //$NON-NLS-1$
		}
		Map map= connector.defaultArguments();
		
		specifyArguments(map, port);
		Process p= null;
		try {
			try {
				connector.startListening(map);
				
				File workingDir = getWorkingDir(config);
				p = exec(cmdLine, workingDir);				
				if (p == null) {
					return;
				}
				
				IProcess process= DebugPlugin.getDefault().newProcess(launch, p, renderProcessLabel(cmdLine));
				process.setAttribute(JavaRuntime.ATTR_CMDLINE, renderCommandLine(cmdLine));
				
				boolean retry= false;
				do  {
					try {
						VirtualMachine vm= connector.accept(map);
						JDIDebugModel.newDebugTarget(launch, vm, renderDebugTarget(config.getClassToLaunch(), port), process, true, false);
						return;
					} catch (InterruptedIOException e) {
						String errorMessage= process.getStreamsProxy().getErrorStreamMonitor().getContents();
						if (errorMessage.length() == 0) {
							errorMessage= process.getStreamsProxy().getOutputStreamMonitor().getContents();
						}
						if (errorMessage.length() != 0) {
							abort(errorMessage, e, IJavaLaunchConfigurationConstants.ERR_VM_LAUNCH_ERROR);
						} else {
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
