package com.sun.jdi.connect;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.Map;

import com.sun.jdi.VirtualMachine;

public interface ListeningConnector extends Connector {
	public VirtualMachine accept(Map arg1) throws IOException, IllegalConnectorArgumentsException;
	public String startListening(Map arg1) throws IOException, IllegalConnectorArgumentsException;
	public void stopListening(Map arg1) throws IOException, IllegalConnectorArgumentsException;
	public boolean supportsMultipleConnections() throws IOException, IllegalConnectorArgumentsException;
}
