package org.eclipse.jdt.launching;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Common function for VM connectors.
 * <p>
 * This class is intended to be subclassed.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @since 2.0
 */
public abstract class AbstractVMConnector implements IVMConnector {

	/**
	 * The port at which the debugger should connect
	 */
	private int fPort = -1;
	
	/**
	 * The host name the debugger should connect to
	 */
	private String fHost = null;
	
	/**
	 * @see IVMConnector#getPort()
	 */
	public int getPort() {
		return fPort;
	}

	/**
	 * @see IVMConnector#getHost()
	 */
	public String getHost() {
		return fHost;
	}

	/**
	 * Sets the port the debugger should connect to.
	 * 
	 * @param port the port the debugger should connect to
	 */
	protected void setPort(int port) {
		fPort = port;
	}
	
	/**
	 * Sets the host name the debugger should connect to.
	 * 
	 * @param host the host name the debugger should connect to
	 */
	protected void setHost(String host) {
		fHost = host;
	}	
}
