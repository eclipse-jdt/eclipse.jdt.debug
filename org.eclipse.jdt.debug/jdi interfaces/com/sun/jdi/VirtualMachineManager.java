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
package com.sun.jdi;


import java.io.IOException;
import java.util.List;

import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.spi.Connection;

public interface VirtualMachineManager {
	public List allConnectors();
	public List attachingConnectors();
	public VirtualMachine createVirtualMachine(Connection arg1) throws IOException;
	public VirtualMachine createVirtualMachine(Connection arg1, Process arg2) throws IOException;
	public List connectedVirtualMachines();
	public LaunchingConnector defaultConnector();
	public List launchingConnectors();
	public List listeningConnectors();
	public int majorInterfaceVersion();
	public int minorInterfaceVersion();
}
