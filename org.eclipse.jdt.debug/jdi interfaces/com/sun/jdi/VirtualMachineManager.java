/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.spi.Connection;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/VirtualMachineManager.html
 */
public interface VirtualMachineManager {
	public List<Connector> allConnectors();
	public List<AttachingConnector> attachingConnectors();
	public List<VirtualMachine> connectedVirtualMachines();
	public VirtualMachine createVirtualMachine(Connection connection) throws IOException;
	public VirtualMachine createVirtualMachine(Connection connection, Process process) throws IOException;
	public LaunchingConnector defaultConnector();
	public List<LaunchingConnector> launchingConnectors();
	public List<ListeningConnector> listeningConnectors();
	public int majorInterfaceVersion();
	public int minorInterfaceVersion();
}
