package com.sun.jdi.connect;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface LaunchingConnector extends com.sun.jdi.connect.Connector {
	public com.sun.jdi.VirtualMachine launch(java.util.Map arg1) throws java.io.IOException, IllegalConnectorArgumentsException, VMStartException;
}
