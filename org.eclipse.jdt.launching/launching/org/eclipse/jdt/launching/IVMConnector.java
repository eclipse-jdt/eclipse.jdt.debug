/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching;

 
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;

import com.sun.jdi.connect.Connector;

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
 * @since 2.0
 */

public interface IVMConnector {
	
	/**
	 * Establishes a JDI connection with a debuggable VM using the arguments
	 * specified in the given map, contributing results (debug targets and processes),
	 * to the given launch.
	 * 
	 * @param arguments Argument map to use in establishing a connection. The keys of
	 * 	the map are strings corresponding to the names of arguments returned by this
	 * 	connector's <code>getDefaultAgruments()</code> method. The values of the map
	 * 	are strings corresponding to the (String) values of the associated
	 *  <code>com.sun.jdi.connect.Connector.Argument</code>s to use.
	 * @param monitor progress monitor
	 * @param launch launch to contribute debug target(s) and/or process(es) to
	 * @exception CoreException if unable to establish a connection with the target VM
	 */
	public void connect(Map<String, String> arguments, IProgressMonitor monitor, ILaunch launch) throws CoreException;
		
	/**
	 * Returns the name of this connector.
	 * 
	 * @return the name of this connector
	 */
	public String getName();
	
	/**
	 * Returns a unique identifier for this kind of connector.
	 * 
	 * @return a unique identifier for this kind of connector
	 */
	public String getIdentifier();
	
	/**
	 * Returns a map of default arguments used by this connector. 
	 * The keys of the map are names of arguments used by this
	 * connector, and the values are of type
	 * <code>com.sun.jdi.connect.Connector.Argument</code>.
	 * 
	 * @return argument map with default values
	 * @exception CoreException if unable to retrieve a default argument map
	 */
	public Map<String, Connector.Argument> getDefaultArguments() throws CoreException;
	
	/**
	 * Returns a list of argument names found in this connector's
	 * default argument map, defining the order in which arguments
	 * should be presented to the user. Since a map is not ordered,
	 * this provides control on how arguments will be presented to 
	 * the user.
	 * 
	 * @return list of argument names
	 */
	public List<String> getArgumentOrder();	
}
