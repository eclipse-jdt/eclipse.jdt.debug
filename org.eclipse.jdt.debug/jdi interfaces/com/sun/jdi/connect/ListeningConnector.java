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
package com.sun.jdi.connect;

import java.io.IOException;
import java.util.Map;

import com.sun.jdi.VirtualMachine;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/connect/ListeningConnector.html
 */
public interface ListeningConnector extends Connector {
	public VirtualMachine accept(Map<String,? extends Connector.Argument> arg1) throws IOException, IllegalConnectorArgumentsException;
	public String startListening(Map<String,? extends Connector.Argument> arg1) throws IOException, IllegalConnectorArgumentsException;
	public void stopListening(Map<String,? extends Connector.Argument> arg1) throws IOException, IllegalConnectorArgumentsException;
	public boolean supportsMultipleConnections() throws IOException, IllegalConnectorArgumentsException;
}
