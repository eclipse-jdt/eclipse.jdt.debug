package com.sun.jdi.connect;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface ListeningConnector extends com.sun.jdi.connect.Connector {
	public com.sun.jdi.VirtualMachine accept(java.util.Map arg1) throws java.io.IOException, IllegalConnectorArgumentsException;
	public String startListening(java.util.Map arg1) throws java.io.IOException, IllegalConnectorArgumentsException;
	public void stopListening(java.util.Map arg1) throws java.io.IOException, IllegalConnectorArgumentsException;
	public boolean supportsMultipleConnections() throws java.io.IOException, IllegalConnectorArgumentsException;
}
