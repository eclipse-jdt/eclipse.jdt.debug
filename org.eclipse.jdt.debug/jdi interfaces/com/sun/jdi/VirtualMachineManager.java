/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sun.jdi;


import java.io.IOException;
import java.util.List;

import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.spi.Connection;

public interface VirtualMachineManager {
	public List allConnectors();
	public List attachingConnectors();
	public List connectedVirtualMachines();
	public VirtualMachine createVirtualMachine(Connection connection) throws IOException;
	public VirtualMachine createVirtualMachine(Connection connection, Process process) throws IOException;
	public LaunchingConnector defaultConnector();
	public List launchingConnectors();
	public List listeningConnectors();
	public int majorInterfaceVersion();
	public int minorInterfaceVersion();
}
