package org.eclipse.jdi.internal.connect;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.VirtualMachineManagerImpl;
import org.eclipse.jdi.internal.connect.ConnectorImpl.BooleanArgumentImpl;
import org.eclipse.jdi.internal.connect.ConnectorImpl.StringArgumentImpl;

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
	/** Character used to combine space-delimited text into a single command line argument. */
	private String fQuote;
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
		StringArgumentImpl strArg = new StringArgumentImpl("home", "Home directory of the SDK or runtime environment used to launch the application.", "Home", false); //$NON-NLS-1$
		strArg.setValue(System.getProperty("java.home")); //$NON-NLS-1$
		arguments.put(strArg.name(), strArg);
		
		// Options
		strArg = new StringArgumentImpl("options", "Launched VM options.", "Options", false); //$NON-NLS-1$
		arguments.put(strArg.name(), strArg);
		
		// Main
		strArg = new StringArgumentImpl("main", "Main class and arguments, or if -jar is an option, the main jar file and arguments.", "Main", true); //$NON-NLS-1$
		arguments.put(strArg.name(), strArg);

		// Suspend
		BooleanArgumentImpl boolArg = new BooleanArgumentImpl("suspend", "All threads will be suspended before execution of main.", "Suspend", false); //$NON-NLS-1$
		boolArg.setValue(true);
		arguments.put(boolArg.name(), boolArg);

		// Quote
		strArg = new StringArgumentImpl("quote", "Character used to combine space-delimited text into a single command line argument.", "Quote", true); //$NON-NLS-1$
		strArg.setValue("\""); //$NON-NLS-1$
		arguments.put(strArg.name(), strArg);

		// Launcher
		strArg = new StringArgumentImpl("vmexec", "Name of the Java VM launcher.", "Launcher", true); //$NON-NLS-1$
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
		return "Launches target using Sun Java VM command line and attaches to it.";
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
		 	fQuote = ((Connector.StringArgument)connectionArgs.get(attribute)).value();
		 	attribute = "vmexec"; //$NON-NLS-1$
		 	fLauncher = ((Connector.StringArgument)connectionArgs.get(attribute)).value();
		} catch (ClassCastException e) {
			throw new IllegalConnectorArgumentsException("Connection argument is not of the right type.", attribute);
		} catch (NullPointerException e) {
			throw new IllegalConnectorArgumentsException("Necessary connection argument is null.", attribute);
		} catch (NumberFormatException e) {
			throw new IllegalConnectorArgumentsException("Connection argument is not a number", attribute);
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

		// The accept times out it the VM does not connect.
		VirtualMachineImpl virtualMachine;
		try {
			virtualMachine = (VirtualMachineImpl)listenConnector.accept(args);
		} catch (InterruptedIOException e) {
			proc.destroy();
			throw new VMStartException("VM did not connect within given time: " + ((Connector.IntegerArgument)args.get("timeout")).value() + " ms.", proc); //$NON-NLS-2$
		}
		
		virtualMachine.setLauncedProcess(proc);
		return virtualMachine;
	}
}