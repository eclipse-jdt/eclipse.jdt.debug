package org.eclipse.jdt.launching;

/*
 * (c) Copyright IBM Corp. 2002
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A VM connector establishes a JDI connection with a debuggable
 * virtual machine. This extension point provides a mechanism for
 * abstracting the connection to a remote virtual machine.
 * <p>
 * A VM connector extension is defined in <code>plugin.xml</code>.
 * Following is an example definition of a VM connector extension.
 * <pre>
 * &lt;extension point="org.eclipse.jdt.launching.vmConnectors"&gt;
 *   &lt;vmConnector 
 *      id="com.example.VMConnector"
 *      class="com.example.VMConnectorClass"
 *   &lt;/vmConnector&gt;
 * &lt;/extension&gt;
 * </pre>
 * The attributes are specified as follows:
 * <ul>
 * <li><code>id</code> specifies a unique identifier for this VM connector.</li>
 * <li><code>class</code> specifies the fully qualified name of the Java class
 *   that implements <code>IVMConnector</code>.</li>
 * </ul>
 * </p>
 * <p>
 * Note: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * 
 * @since 2.0
 */

public interface IVMConnector {
	
	/**
	 * Establishes a JDI connection with a debuggable VM at the specified
	 * address. The resulting port and host address that the debugger should
	 * connect to are returned via <code>getHost()</code> and <code>getPort()</code>.
	 * 
	 * @param host the name of the host on which the target VM is running
	 * @param port the port number at which to connect to the target VM
	 * @param monitor progress monitor
	 * @exception CoreException if unable to establish a connection with the target VM
	 */
	public void connect(String host, int port, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Returns the port that the debugger should connect to, or 
	 * <code>-1</code> if a has not been established.
	 * 
	 * @return the port that the debugger should connect to, or 
	 *  <code>-1</code> if a has not been established
	 */
	public int getPort();
	
	/**
	 * Returns the name of the host the debugger should connect to, or
	 * <code>null</code> if a connection has not been established.
	 * 
	 * @return the name of the host the debugger should connect to, or
	 *  <code>null</code> if a connection has not been established
	 */
	public String getHost();
	
	/**
	 * Returns the name of this connector.
	 * 
	 * @return the name of this connector
	 */
	public String getName();
	
	/**
	 * Returns a unique indentifier for this kind of connector.
	 * 
	 * @return a unique indentifier for this kind of connector
	 */
	public String getIdentifier();
}
