package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;

import com.sun.jdi.connect.LaunchingConnector;

public interface VirtualMachineManager {
	public List allConnectors();
	public List attachingConnectors();
	public List connectedVirtualMachines();
	public LaunchingConnector defaultConnector();
	public List launchingConnectors();
	public List listeningConnectors();
	public int majorInterfaceVersion();
	public int minorInterfaceVersion();
}
