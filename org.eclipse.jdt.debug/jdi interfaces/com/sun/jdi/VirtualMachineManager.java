package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface VirtualMachineManager {
	public java.util.List allConnectors();
	public java.util.List attachingConnectors();
	public java.util.List connectedVirtualMachines();
	public com.sun.jdi.connect.LaunchingConnector defaultConnector();
	public java.util.List launchingConnectors();
	public java.util.List listeningConnectors();
	public int majorInterfaceVersion();
	public int minorInterfaceVersion();
}
