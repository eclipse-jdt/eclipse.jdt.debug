package org.eclipse.jdi.internal.connect;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdi.internal.VirtualMachineManagerImpl;
import org.eclipse.jdi.internal.connect.ConnectorImpl.IntegerArgumentImpl;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;


public class SocketListeningConnectorImpl extends ConnectorImpl implements ListeningConnector {
	/** Port to which is attached. */
	private int fPort;
	/** Timeout before accept returns. */
	private int fTimeout;
	
	
	/**
	 * Creates new SocketAttachingConnectorImpl.
	 */
	public SocketListeningConnectorImpl(VirtualMachineManagerImpl virtualMachineManager) {
		super(virtualMachineManager);
		
		// Create communication protocol specific transport.
		SocketTransportImpl transport = new SocketTransportImpl();
		setTransport(transport);
	}
	
	/**
	 * @return Returns the default arguments.
	 */	
	public Map defaultArguments() {
		HashMap arguments = new HashMap(1);
		
		// Port
		IntegerArgumentImpl intArg = new IntegerArgumentImpl("port", "Port number at which to listen for VM connections", "Port", true, SocketTransportImpl.MIN_PORTNR, SocketTransportImpl.MAX_PORTNR);
		arguments.put(intArg.name(), intArg);
		
		// Timeout
		intArg = new IntegerArgumentImpl("timeout", "Timeout before accept returns", "Timeout", false, 0, Integer.MAX_VALUE);
		arguments.put(intArg.name(), intArg);
		
		return arguments;
	}
	
	/**
	 * @return Returns a short identifier for the connector.
	 */	
	public String name() {
		return "com.sun.jdi.SocketListen";
	}
	
	/**
	 * @return Returns a human-readable description of this connector and its purpose.
	 */	
	public String description() {
		return "Accepts socket connections initiated by other VMs.";
	}
	
 	/**
 	 * Retrieves connection arguments.
 	 */
	private void getConnectionArguments(Map connectionArgs) throws IllegalConnectorArgumentsException {
		String attribute = "";
		try {
		 	attribute = "port";
		 	fPort = ((Connector.IntegerArgument)connectionArgs.get(attribute)).intValue();
		 	// Note that timeout is not used in SUN's ListeningConnector, but is used by our
		 	// LaunchingConnector.
		 	attribute = "timeout";
		 	fTimeout = ((Connector.IntegerArgument)connectionArgs.get(attribute)).intValue();
		} catch (ClassCastException e) {
			throw new IllegalConnectorArgumentsException("Connection argument is not of the right type.", attribute);
		} catch (NullPointerException e) {
			throw new IllegalConnectorArgumentsException("Necessary connection argument is null.", attribute);
		} catch (NumberFormatException e) {
			throw new IllegalConnectorArgumentsException("Connection argument is not a number", attribute);
		}
	}
	
	/**
	 * Listens for one or more connections initiated by target VMs. 
	 * @return Returns the address at which the connector is listening for a connection.
	 */
	public String startListening(Map connectionArgs) throws IOException, IllegalConnectorArgumentsException {
		getConnectionArguments(connectionArgs);
		String result = "ListeningConnector Socket Port=" + fPort;
		try {
			((SocketTransportImpl)fTransport).listen(fPort);
		} catch (IllegalArgumentException e) {
			throw new IllegalConnectorArgumentsException(e.getMessage(), "port");
		}
		return result;
	}
	
	/**
	 * Cancels listening for connections. 
	 */
	public void stopListening(Map connectionArgs) throws IOException, IllegalConnectorArgumentsException {
		((SocketTransportImpl)fTransport).closeListen();
	}
		
	/**
	 * Waits for a target VM to attach to this connector.
	 * @return Returns a connected Virtual Machine.
	 */
	public VirtualMachine accept(Map connectionArgs) throws IOException, IllegalConnectorArgumentsException {
		((SocketTransportImpl)fTransport).setAcceptTimeout(fTimeout);
		((SocketTransportImpl)fTransport).accept();
		return establishedConnection();
	}
	
	/**
	 * @return Returns whether this listening connector supports multiple connections for a single argument map. 
	 */
	public boolean supportsMultipleConnections() {
		return true;
	}
	
	/**
	 * @return Returns port number that is listened to. 
	 */
	public int listeningPort() {
		return ((SocketTransportImpl)fTransport).listeningPort();
	}
}