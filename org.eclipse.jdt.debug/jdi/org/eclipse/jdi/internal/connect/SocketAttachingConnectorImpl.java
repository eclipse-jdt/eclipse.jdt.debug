package org.eclipse.jdi.internal.connect;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.eclipse.jdi.internal.*;

public class SocketAttachingConnectorImpl extends ConnectorImpl implements AttachingConnector {
	/** Hostname to which is attached. */
	private String fHostname;
	/** Port to which is attached. */
	private int fPort;
	
	/**
	 * Creates new SocketAttachingConnectorImpl.
	 */
	public SocketAttachingConnectorImpl(VirtualMachineManagerImpl virtualMachineManager) {
		super(virtualMachineManager);
		
		// Create communication protocol specific transport.
		SocketTransportImpl transport = new SocketTransportImpl();
		setTransport(transport);
	}
	
	/**
	 * @return Returns the default arguments.
	 */	
	public Map defaultArguments() {
		HashMap arguments = new HashMap(2);
		
		// Hostname
		StringArgumentImpl strArg = new StringArgumentImpl("hostname", "Machine name to which to attach for VM connections.", "Host", false);
		strArg.setValue("localhost");
		arguments.put(strArg.name(), strArg);
		
		// Port
		IntegerArgumentImpl intArg = new IntegerArgumentImpl("port", "Port number to which to attach for VM connections.", "Port", true, SocketTransportImpl.MIN_PORTNR, SocketTransportImpl.MAX_PORTNR);
		arguments.put(intArg.name(), intArg);
		
		return arguments;
	}
	
	/**
	 * @return Returns a short identifier for the connector.
	 */	
	public String name() {
		return "com.sun.jdi.SocketAttach";
	}
	
	/**
	 * @return Returns a human-readable description of this connector and its purpose.
	 */	
	public String description() {
		return "Attaches by socket to other VMs.";
	}
	
 	/**
 	 * Retrieves connection arguments.
 	 */
	private void getConnectionArguments(Map connectionArgs) throws IllegalConnectorArgumentsException {
		String attribute = "";
		try {
			attribute = "hostname";
		 	fHostname = ((Connector.StringArgument)connectionArgs.get(attribute)).value();
		 	attribute = "port";
		 	fPort = ((Connector.IntegerArgument)connectionArgs.get(attribute)).intValue();
		} catch (ClassCastException e) {
			throw new IllegalConnectorArgumentsException("Connection argument is not of the right type.", attribute);
		} catch (NullPointerException e) {
			throw new IllegalConnectorArgumentsException("Necessary connection argument is null.", attribute);
		} catch (NumberFormatException e) {
			throw new IllegalConnectorArgumentsException("Connection argument is not a number", attribute);
		}
	}
	
	/**
	 * Establishes a connection to a virtual machine.
	 * @return Returns a connected Virtual Machine.
	 */
	public VirtualMachine attach(Map connectionArgs) throws IOException, IllegalConnectorArgumentsException {
		getConnectionArguments(connectionArgs);
		try {
			((SocketTransportImpl)fTransport).attach(fHostname, fPort);
		} catch (IllegalArgumentException e) {
			Vector args = new Vector();
			args.add("hostname");
			args.add("port");
			throw new IllegalConnectorArgumentsException(e.getMessage(), args);
		}
		return establishedConnection();
	}
}