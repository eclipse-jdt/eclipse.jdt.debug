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
import org.eclipse.jdi.internal.connect.ConnectorImpl.StringArgumentImpl;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;


public class SocketRawLaunchingConnectorImpl extends ConnectorImpl implements LaunchingConnector {
	/** Time that a launched VM is given to connect to us. */
	private static final int ACCEPT_TIMEOUT = 10*1000;

	/** Raw command to start the debugged application VM. */
	private String fCommand;
	/** Address from which to listen for a connection after the raw command is run. */
	private String fAddress;
	/** Character used to combine space-delimited text into a single command line argument. */
	private String fQuote;
	
	/**
	 * Creates new SocketAttachingConnectorImpl.
	 */
	public SocketRawLaunchingConnectorImpl(VirtualMachineManagerImpl virtualMachineManager) {
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
		
		// Command
		StringArgumentImpl strArg = new StringArgumentImpl("command", "Raw command to start the debugged application VM.", "Command", true);
		arguments.put(strArg.name(), strArg);
		
		// Address
		strArg = new StringArgumentImpl("address", "Address from which to listen for a connection after the raw command is run.", "Address", true);
		arguments.put(strArg.name(), strArg);
		
		// Quote
		strArg = new StringArgumentImpl("quote", "Character used to combine space-delimited text into a single command line argument.", "Quote", true);
		strArg.setValue("\"");
		arguments.put(strArg.name(), strArg);

		return arguments;
	}
	
	/**
	 * @return Returns a short identifier for the connector.
	 */	
	public String name() {
		return "com.sun.jdi.RawCommandLineLaunch";
	}
	
	/**
	 * @return Returns a human-readable description of this connector and its purpose.
	 */	
	public String description() {
		return "Launches target using user-specified command line and attaches to it.";
	}
	
 	/**
 	 * Retrieves connection arguments.
 	 */
	private void getConnectionArguments(Map connectionArgs) throws IllegalConnectorArgumentsException {
		String attribute = "";
		try {
			attribute = "command";
		 	fCommand = ((Connector.StringArgument)connectionArgs.get(attribute)).value();
		 	attribute = "address";
		 	fAddress = ((Connector.StringArgument)connectionArgs.get(attribute)).value();
		 	attribute = "quote";
		 	fQuote = ((Connector.StringArgument)connectionArgs.get(attribute)).value();
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
		((Connector.IntegerArgument)args.get("port")).setValue(fAddress);
		((Connector.IntegerArgument)args.get("timeout")).setValue(ACCEPT_TIMEOUT);
		listenConnector.startListening(args);
		
		// Start VM.
		Process proc = Runtime.getRuntime().exec(fCommand);

		// The accept times out it the VM does not connect.
		VirtualMachineImpl virtualMachine;
		try {
			virtualMachine = (VirtualMachineImpl)listenConnector.accept(args);
		} catch (InterruptedIOException e) {
			proc.destroy();
			throw new VMStartException("VM did not connect within given time: " + ((Connector.IntegerArgument)args.get("timeout")).value() + " ms.", proc);
		}
		
		virtualMachine.setLauncedProcess(proc);
		return virtualMachine;
	}
}