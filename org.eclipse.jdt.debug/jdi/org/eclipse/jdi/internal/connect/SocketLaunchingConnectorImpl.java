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
package org.eclipse.jdi.internal.connect;


import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.VirtualMachineManagerImpl;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;


public class SocketLaunchingConnectorImpl extends ConnectorImpl implements LaunchingConnector {
	/** Time that a launched VM is given to connect to us. */
	private static final int ACCEPT_TIMEOUT = 10*1000;
	
	/** Home directory of the SDK or runtime environment used to launch the application. */
	private String fHome;
	/** Launched VM options. */
	private String fOptions;
	/** Main class and arguments, or if -jar is an option, the main jar file and arguments. */
	private String fMain;
	/** All threads will be suspended before execution of main. */
	private boolean fSuspend;
	/** Name of the Java VM launcher. */
	private String fLauncher;
	
	/**
	 * Creates new SocketAttachingConnectorImpl.
	 */
	public SocketLaunchingConnectorImpl(VirtualMachineManagerImpl virtualMachineManager) {
		super(virtualMachineManager);
		
		// Create communication protocol specific transport.
		SocketTransportImpl transport = new SocketTransportImpl();
		setTransport(transport);
	}
	
	/**
	 * @return Returns the default arguments.
	 */	
	public Map defaultArguments() {
		HashMap arguments = new HashMap(6);
		
		// Home
		StringArgumentImpl strArg = new StringArgumentImpl("home", ConnectMessages.getString("SocketLaunchingConnectorImpl.Home_directory_of_the_SDK_or_runtime_environment_used_to_launch_the_application_1"), ConnectMessages.getString("SocketLaunchingConnectorImpl.Home_2"), false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		strArg.setValue(System.getProperty("java.home")); //$NON-NLS-1$
		arguments.put(strArg.name(), strArg);
		
		// Options
		strArg = new StringArgumentImpl("options", ConnectMessages.getString("SocketLaunchingConnectorImpl.Launched_VM_options_3"), ConnectMessages.getString("SocketLaunchingConnectorImpl.Options_4"), false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		arguments.put(strArg.name(), strArg);
		
		// Main
		strArg = new StringArgumentImpl("main", ConnectMessages.getString("SocketLaunchingConnectorImpl.Main_class_and_arguments,_or_if_-jar_is_an_option,_the_main_jar_file_and_arguments_5"), ConnectMessages.getString("SocketLaunchingConnectorImpl.Main_6"), true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		arguments.put(strArg.name(), strArg);

		// Suspend
		BooleanArgumentImpl boolArg = new BooleanArgumentImpl("suspend", ConnectMessages.getString("SocketLaunchingConnectorImpl.All_threads_will_be_suspended_before_execution_of_main_7"), ConnectMessages.getString("SocketLaunchingConnectorImpl.Suspend_8"), false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		boolArg.setValue(true);
		arguments.put(boolArg.name(), boolArg);

		// Quote
		strArg = new StringArgumentImpl("quote", ConnectMessages.getString("SocketLaunchingConnectorImpl.Character_used_to_combine_space-delimited_text_into_a_single_command_line_argument_9"), ConnectMessages.getString("SocketLaunchingConnectorImpl.Quote_10"), true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		strArg.setValue("\""); //$NON-NLS-1$
		arguments.put(strArg.name(), strArg);

		// Launcher
		strArg = new StringArgumentImpl("vmexec", ConnectMessages.getString("SocketLaunchingConnectorImpl.Name_of_the_Java_VM_launcher_11"), ConnectMessages.getString("SocketLaunchingConnectorImpl.Launcher_12"), true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		strArg.setValue("java"); //$NON-NLS-1$
		arguments.put(strArg.name(), strArg);

		return arguments;
	}
	
	/**
	 * @return Returns a short identifier for the connector.
	 */	
	public String name() {
		return "com.sun.jdi.CommandLineLaunch"; //$NON-NLS-1$
	}
	
	/**
	 * @return Returns a human-readable description of this connector and its purpose.
	 */	
	public String description() {
		return ConnectMessages.getString("SocketLaunchingConnectorImpl.Launches_target_using_Sun_Java_VM_command_line_and_attaches_to_it_13"); //$NON-NLS-1$
	}
	
 	/**
 	 * Retrieves connection arguments.
 	 */
	private void getConnectionArguments(Map connectionArgs) throws IllegalConnectorArgumentsException {
		String attribute = ""; //$NON-NLS-1$
		try {
			attribute = "home"; //$NON-NLS-1$
		 	fHome = ((Connector.StringArgument)connectionArgs.get(attribute)).value();
		 	attribute = "options"; //$NON-NLS-1$
		 	fOptions = ((Connector.StringArgument)connectionArgs.get(attribute)).value();
		 	attribute = "main"; //$NON-NLS-1$
		 	fMain = ((Connector.StringArgument)connectionArgs.get(attribute)).value();
		 	attribute = "suspend"; //$NON-NLS-1$
		 	fSuspend = ((Connector.BooleanArgument)connectionArgs.get(attribute)).booleanValue();
		 	attribute = "quote"; //$NON-NLS-1$
		 	((Connector.StringArgument)connectionArgs.get(attribute)).value();
		 	attribute = "vmexec"; //$NON-NLS-1$
		 	fLauncher = ((Connector.StringArgument)connectionArgs.get(attribute)).value();
		} catch (ClassCastException e) {
			throw new IllegalConnectorArgumentsException(ConnectMessages.getString("SocketLaunchingConnectorImpl.Connection_argument_is_not_of_the_right_type_14"), attribute); //$NON-NLS-1$
		} catch (NullPointerException e) {
			throw new IllegalConnectorArgumentsException(ConnectMessages.getString("SocketLaunchingConnectorImpl.Necessary_connection_argument_is_null_15"), attribute); //$NON-NLS-1$
		} catch (NumberFormatException e) {
			throw new IllegalConnectorArgumentsException(ConnectMessages.getString("SocketLaunchingConnectorImpl.Connection_argument_is_not_a_number_16"), attribute); //$NON-NLS-1$
		}
	}

	/**
	 * Launches an application and connects to its VM. 
	 * @return Returns a connected Virtual Machine.
	 */
	public VirtualMachine launch(Map connectionArgs) throws IOException, IllegalConnectorArgumentsException, VMStartException {
		getConnectionArguments(connectionArgs);
		
		// A listening connector is used that waits for a connection of the VM that is started up.
		// Note that port number zero means that a free port is chosen.
		SocketListeningConnectorImpl listenConnector = new SocketListeningConnectorImpl(virtualMachineManager());
		Map args = listenConnector.defaultArguments();
		((Connector.IntegerArgument)args.get("port")).setValue(0); //$NON-NLS-1$
		((Connector.IntegerArgument)args.get("timeout")).setValue(ACCEPT_TIMEOUT); //$NON-NLS-1$
		listenConnector.startListening(args);
		
		// String for Executable.
		String slash = System.getProperty("file.separator"); //$NON-NLS-1$
		String execString = fHome + slash + "bin" + slash + fLauncher; //$NON-NLS-1$
		
		// Add Debug options.
		execString += " -Xdebug -Xnoagent -Djava.compiler=NONE"; //$NON-NLS-1$
		execString += " -Xrunjdwp:transport=dt_socket,address=localhost:" + listenConnector.listeningPort() + ",server=n,suspend=" + (fSuspend ? "y" : "n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		// Add User specified options.
		if (fOptions != null)
			execString += " " + fOptions; //$NON-NLS-1$
		
		// Add Main class.
		execString += " " + fMain; //$NON-NLS-1$
		
		// Start VM.
		Process proc = Runtime.getRuntime().exec(execString);

		// The accept times out if the VM does not connect.
		VirtualMachineImpl virtualMachine;
		try {
			virtualMachine = (VirtualMachineImpl)listenConnector.accept(args);
		} catch (InterruptedIOException e) {
			proc.destroy();
			String message= MessageFormat.format(ConnectMessages.getString("SocketLaunchingConnectorImpl.VM_did_not_connect_within_given_time__{0}_ms_1"), new String[]{((Connector.IntegerArgument)args.get("timeout")).value()}); //$NON-NLS-1$ //$NON-NLS-2$
			throw new VMStartException(message, proc);
		}
		
		virtualMachine.setLaunchedProcess(proc);
		return virtualMachine;
	}
}
