package org.eclipse.jdt.launching;

/*
 * (c) Copyright IBM Corp. 2002
 * All Rights Reserved.
 */
 
import java.util.Map;

import com.sun.jdi.VirtualMachine;

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
	 * Establishes a JDI connection with a debuggable VM using the arguments
	 * specified in the given map, and returns the resulting virtual machine. 
	 * The keys of the map are names of arguments used by this
	 * connector (as returned by <code>#getDefaultArguments()</code>, and the
	 * values are Strings representing the vales to use.
	 * 
	 * @param map argument map to use in establishing a connection
	 * @param monitor progress monitor
	 * @return virtual machine
	 * @exception CoreException if unable to establish a connection with the target VM
	 */
	public VirtualMachine connect(Map arguments, IProgressMonitor monitor) throws CoreException;
		
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
	
	/**
	 * Returns a map of default arguments used by this connector. 
	 * The keys of the map are names of arguments used by this
	 * connector, and the values are of type
	 * <code>com.sun.jdt.Connect.Connector.Argument</code>.
	 * 
	 * @return argument map with default values
	 * @exception CoreException if unable to retrieve a default argument map
	 */
	public Map getDefaultArguments() throws CoreException;
}
