/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

public class SocketAttachingConnectorImpl extends ConnectorImpl implements
		AttachingConnector {
	/** Host name to which is attached. */
	private String fHostname;
	/** Port to which is attached. */
	private int fPort;
	private int fTimeout;

	/**
	 * Creates new SocketAttachingConnectorImpl.
	 */
	public SocketAttachingConnectorImpl(
			VirtualMachineManagerImpl virtualMachineManager) {
		super(virtualMachineManager);

		// Create communication protocol specific transport.
		SocketTransportImpl transport = new SocketTransportImpl();
		setTransport(transport);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.connect.Connector#defaultArguments()
	 */
	public Map<String, Connector.Argument> defaultArguments() {
		HashMap<String, Connector.Argument> arguments = new HashMap<String, Connector.Argument>(2);

		// Host name
		StringArgumentImpl strArg = new StringArgumentImpl(
				"hostname", ConnectMessages.SocketAttachingConnectorImpl_Machine_name_to_which_to_attach_for_VM_connections_1, ConnectMessages.SocketAttachingConnectorImpl_Host_2, false); //$NON-NLS-1$  
		strArg.setValue("localhost"); //$NON-NLS-1$
		arguments.put(strArg.name(), strArg);

		// Port
		IntegerArgumentImpl intArg = new IntegerArgumentImpl(
				"port", ConnectMessages.SocketAttachingConnectorImpl_Port_number_to_which_to_attach_for_VM_connections_3, ConnectMessages.SocketAttachingConnectorImpl_Port_4, true, SocketTransportImpl.MIN_PORTNR, SocketTransportImpl.MAX_PORTNR); //$NON-NLS-1$  
		arguments.put(intArg.name(), intArg);

		// Timeout
		IntegerArgumentImpl timeoutArg = new IntegerArgumentImpl(
				"timeout", ConnectMessages.SocketAttachingConnectorImpl_1, ConnectMessages.SocketAttachingConnectorImpl_2, false, 0, Integer.MAX_VALUE); //$NON-NLS-1$  
		timeoutArg.setValue(0); // by default wait forever
		arguments.put(timeoutArg.name(), timeoutArg);

		return arguments;
	}

	/**
	 * @return Returns a short identifier for the connector.
	 */
	@Override
	public String name() {
		return "com.sun.jdi.SocketAttach"; //$NON-NLS-1$
	}

	/**
	 * @return Returns a human-readable description of this connector and its
	 *         purpose.
	 */
	@Override
	public String description() {
		return ConnectMessages.SocketAttachingConnectorImpl_Attaches_by_socket_to_other_VMs_5;
	}

	/**
	 * Retrieves connection arguments.
	 */
	private void getConnectionArguments(Map<String,? extends Connector.Argument> connectionArgs)
			throws IllegalConnectorArgumentsException {
		String attribute = ""; //$NON-NLS-1$
		try {
			attribute = "hostname"; //$NON-NLS-1$
			fHostname = ((Connector.StringArgument) connectionArgs
					.get(attribute)).value();
			attribute = "port"; //$NON-NLS-1$
			fPort = ((Connector.IntegerArgument) connectionArgs.get(attribute))
					.intValue();
			attribute = "timeout"; //$NON-NLS-1$
			Object object = connectionArgs.get(attribute);
			if (object != null) {
				Connector.IntegerArgument timeoutArg = (IntegerArgument) object;
				if (timeoutArg.value() != null) {
					fTimeout = timeoutArg.intValue();
				}
			}
		} catch (ClassCastException e) {
			throw new IllegalConnectorArgumentsException(
					ConnectMessages.SocketAttachingConnectorImpl_Connection_argument_is_not_of_the_right_type_6,
					attribute);
		} catch (NullPointerException e) {
			throw new IllegalConnectorArgumentsException(
					ConnectMessages.SocketAttachingConnectorImpl_Necessary_connection_argument_is_null_7,
					attribute);
		} catch (NumberFormatException e) {
			throw new IllegalConnectorArgumentsException(
					ConnectMessages.SocketAttachingConnectorImpl_Connection_argument_is_not_a_number_8,
					attribute);
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.connect.AttachingConnector#attach(java.util.Map)
	 */
	public VirtualMachine attach(Map<String,? extends Connector.Argument> connectionArgs) throws IOException,
			IllegalConnectorArgumentsException {
		getConnectionArguments(connectionArgs);
		Connection connection = null;
		try {
			connection = ((SocketTransportImpl) fTransport).attach(fHostname,
					fPort, fTimeout, 0);
		} catch (IllegalArgumentException e) {
			List<String> args = new ArrayList<String>();
			args.add("hostname"); //$NON-NLS-1$
			args.add("port"); //$NON-NLS-1$
			throw new IllegalConnectorArgumentsException(e.getMessage(), args);
		}
		return establishedConnection(connection);
	}
}
