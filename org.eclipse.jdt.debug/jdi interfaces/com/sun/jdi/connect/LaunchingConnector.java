package com.sun.jdi.connect;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;

import com.sun.jdi.VirtualMachine;

public interface LaunchingConnector extends Connector {
	public VirtualMachine launch(java.util.Map arg1) throws IOException, IllegalConnectorArgumentsException, VMStartException;
}
