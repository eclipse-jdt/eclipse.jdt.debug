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
package org.eclipse.jdi.internal.connect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sun.jdi.connect.Transport;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public abstract class TransportImpl implements Transport {
	/** Name of Transport. */
	private String fName;

	/**
	 * Constructs new SocketTransportImpl.
	 */
	public TransportImpl(String name) {
		fName = name;
	}

	/**
	 * @return Returns a short identifier for the transport.
	 */
	public String name() {
		return fName;
	}

	/**
	 * @return Returns true if we have an open connection.
	 */
	public abstract boolean isOpen();

	/**
	 * Closes connection.
	 */
	public abstract void close();

	/**
	 * @return Returns InputStream from Virtual Machine.
	 */
	public abstract InputStream getInputStream() throws IOException;

	/**
	 * @return Returns OutputStream to Virtual Machine.
	 */
	public abstract OutputStream getOutputStream() throws IOException;
}
