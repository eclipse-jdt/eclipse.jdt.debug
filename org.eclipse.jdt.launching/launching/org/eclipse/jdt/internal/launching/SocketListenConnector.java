/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;

/**
 * A standard socket listening connector.
 * Starts a launch that waits for a VM to connect at a specific port.
 * @since 3.4
 * @see SocketListenConnectorProcess
 */
public class SocketListenConnector implements IVMConnector {
		
	/**
	 * Return the socket transport listening connector
	 * 
	 * @exception CoreException if unable to locate the connector
	 */
	protected static ListeningConnector getListeningConnector() throws CoreException {
		ListeningConnector connector= null;
		Iterator iter= Bootstrap.virtualMachineManager().listeningConnectors().iterator();
		while (iter.hasNext()) {
			ListeningConnector lc= (ListeningConnector) iter.next();
			if (lc.name().equals("com.sun.jdi.SocketListen")) { //$NON-NLS-1$
				connector= lc;
				break;
			}
		}
		if (connector == null) {
			abort(LaunchingMessages.SocketListenConnector_0, null, IJavaLaunchConfigurationConstants.ERR_SHARED_MEMORY_CONNECTOR_UNAVAILABLE); 
		}
		return connector;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMConnector#getIdentifier()
	 */
	public String getIdentifier() {
		return IJavaLaunchConfigurationConstants.ID_SOCKET_LISTEN_VM_CONNECTOR;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMConnector#getName()
	 */
	public String getName() {
		return LaunchingMessages.SocketListenConnector_1; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMConnector#connect(java.util.Map, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.debug.core.ILaunch)
	 */
	public void connect(Map arguments, IProgressMonitor monitor, ILaunch launch) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		monitor.subTask(LaunchingMessages.SocketListenConnector_2);
		
		ListeningConnector connector= getListeningConnector();
		
		String portNumberString = (String)arguments.get("port"); //$NON-NLS-1$
		if (portNumberString == null) {
			abort(LaunchingMessages.SocketAttachConnector_Port_unspecified_for_remote_connection__2, null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_PORT); 
		}
	
		Map acceptArguments = connector.defaultArguments();
		
        Connector.Argument param= (Connector.Argument) acceptArguments.get("port"); //$NON-NLS-1$
		param.setValue(portNumberString);
        
		try {
			monitor.subTask(MessageFormat.format(LaunchingMessages.SocketListenConnector_3, new String[]{portNumberString}));
			connector.startListening(acceptArguments);
			SocketListenConnectorProcess process = new SocketListenConnectorProcess(launch,portNumberString);
			launch.addProcess(process);
			process.waitForConnection(connector, acceptArguments);
		} catch (IOException e) {
			abort(LaunchingMessages.SocketListenConnector_4, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED); 
		} catch (IllegalConnectorArgumentsException e) {
			abort(LaunchingMessages.SocketListenConnector_4, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED); 
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMConnector#getDefaultArguments()
	 */
	public Map getDefaultArguments() throws CoreException {
		Map def = getListeningConnector().defaultArguments();
		Connector.IntegerArgument arg = (Connector.IntegerArgument)def.get("port"); //$NON-NLS-1$
		arg.setValue(8000);
		return def;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMConnector#getArgumentOrder()
	 */
	public List getArgumentOrder() {
		List list = new ArrayList(1);
		list.add("port"); //$NON-NLS-1$
		return list;
	}

	/**
	 * Throws a core exception with an error status object built from
	 * the given message, lower level exception, and error code.
	 * 
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @param code error code
	 */
	protected static void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), code, message, exception));
	}
}
