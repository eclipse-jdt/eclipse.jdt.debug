package org.eclipse.jdi.internal.connect;/*
 * JDI class Implementation
 *
 * (BB)
 * (C) Copyright IBM Corp. 2000
 */



import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.io.*;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
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
