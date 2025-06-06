/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *     Ivan Popov - Bug 184211: JDI connectors throw NullPointerException if used separately
 *     			from Eclipse
 *     Microsoft Corporation - supports virtual threads
 *******************************************************************************/
package org.eclipse.jdi.internal.connect;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.VirtualMachineManagerImpl;
import org.eclipse.osgi.util.NLS;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;

public class SocketLaunchingConnectorImpl extends ConnectorImpl implements
		LaunchingConnector {
	/** Time that a launched VM is given to connect to us. */
	private static final int ACCEPT_TIMEOUT = 10 * 1000;

	/**
	 * Home directory of the SDK or runtime environment used to launch the
	 * application.
	 */
	private String fHome;
	/** Launched VM options. */
	private String fOptions;
	/**
	 * Main class and arguments, or if -jar is an option, the main jar file and
	 * arguments.
	 */
	private String fMain;
	/** All threads will be suspended before execution of main. */
	private boolean fSuspend;
	/** Name of the Java VM launcher. */
	private String fLauncher;
	/**
	 * List of all threads includes virtual threads as well as platform threads.
	 * Virtual threads are a preview feature of the Java platform.
	 * @since 3.20
	 */
	private boolean fIncludeVirtualThreads;

	/**
	 * Creates new SocketAttachingConnectorImpl.
	 */
	public SocketLaunchingConnectorImpl(
			VirtualMachineManagerImpl virtualMachineManager) {
		super(virtualMachineManager);

		// Create communication protocol specific transport.
		SocketTransportImpl transport = new SocketTransportImpl();
		setTransport(transport);
	}

	/**
	 * @return Returns the default arguments.
	 */
	@Override
	public Map<String, Connector.Argument> defaultArguments() {
		HashMap<String, Connector.Argument> arguments = new HashMap<>(6);

		// Home
		StringArgumentImpl strArg = new StringArgumentImpl(
				"home", ConnectMessages.SocketLaunchingConnectorImpl_Home_directory_of_the_SDK_or_runtime_environment_used_to_launch_the_application_1, ConnectMessages.SocketLaunchingConnectorImpl_Home_2, false); //$NON-NLS-1$
		strArg.setValue(System.getProperty("java.home")); //$NON-NLS-1$
		arguments.put(strArg.name(), strArg);

		// Options
		strArg = new StringArgumentImpl(
				"options", ConnectMessages.SocketLaunchingConnectorImpl_Launched_VM_options_3, ConnectMessages.SocketLaunchingConnectorImpl_Options_4, false); //$NON-NLS-1$
		arguments.put(strArg.name(), strArg);

		// Main
		strArg = new StringArgumentImpl(
				"main", ConnectMessages.SocketLaunchingConnectorImpl_Main_class_and_arguments__or_if__jar_is_an_option__the_main_jar_file_and_arguments_5, ConnectMessages.SocketLaunchingConnectorImpl_Main_6, true); //$NON-NLS-1$
		arguments.put(strArg.name(), strArg);

		// Suspend
		BooleanArgumentImpl boolArg = new BooleanArgumentImpl(
				"suspend", ConnectMessages.SocketLaunchingConnectorImpl_All_threads_will_be_suspended_before_execution_of_main_7, ConnectMessages.SocketLaunchingConnectorImpl_Suspend_8, false); //$NON-NLS-1$
		boolArg.setValue(true);
		arguments.put(boolArg.name(), boolArg);

		// Quote
		strArg = new StringArgumentImpl(
				"quote", ConnectMessages.SocketLaunchingConnectorImpl_Character_used_to_combine_space_delimited_text_into_a_single_command_line_argument_9, ConnectMessages.SocketLaunchingConnectorImpl_Quote_10, true); //$NON-NLS-1$
		strArg.setValue("\""); //$NON-NLS-1$
		arguments.put(strArg.name(), strArg);

		// Launcher
		strArg = new StringArgumentImpl(
				"vmexec", ConnectMessages.SocketLaunchingConnectorImpl_Name_of_the_Java_VM_launcher_11, ConnectMessages.SocketLaunchingConnectorImpl_Launcher_12, true); //$NON-NLS-1$
		strArg.setValue("java"); //$NON-NLS-1$
		arguments.put(strArg.name(), strArg);

		// Include Virtual Threads
		BooleanArgumentImpl vthreadsArg = new BooleanArgumentImpl(
				"includevirtualthreads", ConnectMessages.SocketLaunchingConnectorImpl_Include_virtual_threads_17, ConnectMessages.SocketLaunchingConnectorImpl_IncludeVirtualThreads_18, false); //$NON-NLS-1$
		vthreadsArg.setValue(false);
		arguments.put(vthreadsArg.name(), vthreadsArg);

		return arguments;
	}

	/**
	 * @return Returns a short identifier for the connector.
	 */
	@Override
	public String name() {
		return "com.sun.jdi.CommandLineLaunch"; //$NON-NLS-1$
	}

	/**
	 * @return Returns a human-readable description of this connector and its
	 *         purpose.
	 */
	@Override
	public String description() {
		return ConnectMessages.SocketLaunchingConnectorImpl_Launches_target_using_Sun_Java_VM_command_line_and_attaches_to_it_13;
	}

	/**
	 * Retrieves connection arguments.
	 */
	private void getConnectionArguments(Map<String,? extends Connector.Argument> connectionArgs)
			throws IllegalConnectorArgumentsException {
		String attribute = ""; //$NON-NLS-1$
		try {
			attribute = "home"; //$NON-NLS-1$
			fHome = connectionArgs.get(attribute).value();
			attribute = "options"; //$NON-NLS-1$
			fOptions = connectionArgs.get(attribute).value();
			attribute = "main"; //$NON-NLS-1$
			fMain = connectionArgs.get(attribute)
					.value();
			attribute = "suspend"; //$NON-NLS-1$
			fSuspend = ((Connector.BooleanArgument) connectionArgs
					.get(attribute)).booleanValue();
			attribute = "quote"; //$NON-NLS-1$
			connectionArgs.get(attribute).value();
			attribute = "vmexec"; //$NON-NLS-1$
			fLauncher = connectionArgs.get(attribute).value();
			attribute = "includevirtualthreads"; //$NON-NLS-1$
			fIncludeVirtualThreads = ((Connector.BooleanArgument) connectionArgs
					.get(attribute)).booleanValue();
		} catch (ClassCastException e) {
			throw new IllegalConnectorArgumentsException(
					ConnectMessages.SocketLaunchingConnectorImpl_Connection_argument_is_not_of_the_right_type_14,
					attribute);
		} catch (NullPointerException e) {
			throw new IllegalConnectorArgumentsException(
					ConnectMessages.SocketLaunchingConnectorImpl_Necessary_connection_argument_is_null_15,
					attribute);
		} catch (NumberFormatException e) {
			throw new IllegalConnectorArgumentsException(
					ConnectMessages.SocketLaunchingConnectorImpl_Connection_argument_is_not_a_number_16,
					attribute);
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.connect.LaunchingConnector#launch(java.util.Map)
	 */
	@Override
	public VirtualMachine launch(Map<String,? extends Connector.Argument> connectionArgs) throws IOException,
			IllegalConnectorArgumentsException, VMStartException {
		getConnectionArguments(connectionArgs);

		// A listening connector is used that waits for a connection of the VM
		// that is started up.
		// Note that port number zero means that a free port is chosen.
		SocketListeningConnectorImpl listenConnector = new SocketListeningConnectorImpl(
				virtualMachineManager());
		Map<String, Connector.Argument> args = listenConnector.defaultArguments();
		((Connector.IntegerArgument) args.get("timeout")).setValue(ACCEPT_TIMEOUT); //$NON-NLS-1$
		String address = listenConnector.startListening(args);

		// String for Executable.
		String execString = fHome + File.separatorChar + "bin" + File.separatorChar + fLauncher; //$NON-NLS-1$

		// Add Debug options.
		execString += " -Xdebug -Xnoagent -Djava.compiler=NONE"; //$NON-NLS-1$
		execString += " -Xrunjdwp:transport=dt_socket,address=" + address + ",server=n,suspend=" + (fSuspend ? "y" : "n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (fIncludeVirtualThreads) { // The default value is 'n', add it only when explicitly enabled.
			execString += ",includevirtualthreads=y"; //$NON-NLS-1$
		}

		// Add User specified options.
		if (fOptions != null) {
			execString += " " + fOptions; //$NON-NLS-1$
		}

		// Add Main class.
		execString += " " + fMain; //$NON-NLS-1$

		// Start VM.
		String[] cmdLine = DebugPlugin.parseArguments(execString);
		Process proc = Runtime.getRuntime().exec(cmdLine);

		// The accept times out if the VM does not connect.
		VirtualMachineImpl virtualMachine;
		try {
			virtualMachine = (VirtualMachineImpl) listenConnector.accept(args);
		} catch (InterruptedIOException e) {
			proc.destroy();
			String message = NLS.bind(ConnectMessages.SocketLaunchingConnectorImpl_VM_did_not_connect_within_given_time___0__ms_1,
					args.get("timeout").value()); //$NON-NLS-1$
			throw new VMStartException(message, proc);
		}

		virtualMachine.setLaunchedProcess(proc);
		return virtualMachine;
	}

	/**
	 * Returns a free port number on localhost, or -1 if unable to find a free
	 * port.
	 *
	 * @return a free port number on localhost, or -1 if unable to find a free
	 *         port
	 * @since 3.2
	 */
	public static int findFreePort() {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		} catch (IOException e) {
		}
		return -1;
	}
}
