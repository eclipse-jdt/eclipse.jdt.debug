/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdi.internal.VirtualMachineManagerImpl;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.spi.Connection;

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
		StringArgumentImpl strArg = new StringArgumentImpl("hostname", ConnectMessages.getString("SocketAttachingConnectorImpl.Machine_name_to_which_to_attach_for_VM_connections_1"), ConnectMessages.getString("SocketAttachingConnectorImpl.Host_2"), false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		strArg.setValue("localhost"); //$NON-NLS-1$
		arguments.put(strArg.name(), strArg);
		
		// Port
		IntegerArgumentImpl intArg = new IntegerArgumentImpl("port", ConnectMessages.getString("SocketAttachingConnectorImpl.Port_number_to_which_to_attach_for_VM_connections_3"), ConnectMessages.getString("SocketAttachingConnectorImpl.Port_4"), true, SocketTransportImpl.MIN_PORTNR, SocketTransportImpl.MAX_PORTNR); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		arguments.put(intArg.name(), intArg);
		
		return arguments;
	}
	
	/**
	 * @return Returns a short identifier for the connector.
	 */	
	public String name() {
		return "com.sun.jdi.SocketAttach"; //$NON-NLS-1$
	}
	
	/**
	 * @return Returns a human-readable description of this connector and its purpose.
	 */	
	public String description() {
		return ConnectMessages.getString("SocketAttachingConnectorImpl.Attaches_by_socket_to_other_VMs_5"); //$NON-NLS-1$
	}
	
 	/**
 	 * Retrieves connection arguments.
 	 */
	private void getConnectionArguments(Map connectionArgs) throws IllegalConnectorArgumentsException {
		String attribute = ""; //$NON-NLS-1$
		try {
			attribute = "hostname"; //$NON-NLS-1$
		 	fHostname = ((Connector.StringArgument)connectionArgs.get(attribute)).value();
		 	attribute = "port"; //$NON-NLS-1$
		 	fPort = ((Connector.IntegerArgument)connectionArgs.get(attribute)).intValue();
		 	// TODO: new timeout attribute ?
		} catch (ClassCastException e) {
			throw new IllegalConnectorArgumentsException(ConnectMessages.getString("SocketAttachingConnectorImpl.Connection_argument_is_not_of_the_right_type_6"), attribute); //$NON-NLS-1$
		} catch (NullPointerException e) {
			throw new IllegalConnectorArgumentsException(ConnectMessages.getString("SocketAttachingConnectorImpl.Necessary_connection_argument_is_null_7"), attribute); //$NON-NLS-1$
		} catch (NumberFormatException e) {
			throw new IllegalConnectorArgumentsException(ConnectMessages.getString("SocketAttachingConnectorImpl.Connection_argument_is_not_a_number_8"), attribute); //$NON-NLS-1$
		}
	}
	
	/**
	 * Establishes a connection to a virtual machine.
	 * @return Returns a connected Virtual Machine.
	 */
	public VirtualMachine attach(Map connectionArgs) throws IOException, IllegalConnectorArgumentsException {
		getConnectionArguments(connectionArgs);
		Connection connection = null;
		try {
			connection = ((SocketTransportImpl)fTransport).attach(fHostname, fPort);
		} catch (IllegalArgumentException e) {
			List args = new ArrayList();
			args.add("hostname"); //$NON-NLS-1$
			args.add("port"); //$NON-NLS-1$
			throw new IllegalConnectorArgumentsException(e.getMessage(), args);
		}
		return establishedConnection(connection);
	}
}
