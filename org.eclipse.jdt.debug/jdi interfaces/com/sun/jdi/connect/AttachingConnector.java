package com.sun.jdi.connect;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.util.Map;

import com.sun.jdi.VirtualMachine;

public interface AttachingConnector extends Connector {
	public VirtualMachine attach(Map arg1) throws IOException, IllegalConnectorArgumentsException;
}
