package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

/**
 * A standard socket attaching connector
 */
public class SocketAttachConnector implements IVMConnector {

	/**
	 * @see IVMConnector#connect(String, int, IProgressMonitor)
	 */
	public VirtualMachine connect(String host, int port, IProgressMonitor monitor) throws CoreException {
								
		AttachingConnector connector= getAttachingConnector();
		String portNumberString = Integer.toString(port);
		if (connector != null) {
			Map map= connector.defaultArguments();
			Connector.Argument param= (Connector.Argument) map.get("hostname"); //$NON-NLS-1$
			param.setValue(host);
			param= (Connector.Argument) map.get("port"); //$NON-NLS-1$
			param.setValue(portNumberString);
			try {
				return connector.attach(map);
			} catch (IOException e) {
				abort(LaunchingMessages.getString("SocketAttachConnector.Failed_to_connect_to_remote_VM_1"), e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED); //$NON-NLS-1$
			} catch (IllegalConnectorArgumentsException e) {
				abort(LaunchingMessages.getString("SocketAttachConnector.Failed_to_connect_to_remote_VM_2"), e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED); //$NON-NLS-1$
			}
		} else {
			abort(LaunchingMessages.getString("SocketAttachConnector.Socket_attaching_connector_not_available_3"), null, IJavaLaunchConfigurationConstants.ERR_SHARED_MEMORY_CONNECTOR_UNAVAILABLE); //$NON-NLS-1$
		}
		// execution path will not reach here
		return null;
	}
		
	/**
	 * Return the socket transport attaching connector
	 */
	protected static AttachingConnector getAttachingConnector() {
		AttachingConnector connector= null;
		Iterator iter= Bootstrap.virtualMachineManager().attachingConnectors().iterator();
		while (iter.hasNext()) {
			AttachingConnector lc= (AttachingConnector) iter.next();
			if (lc.name().equals("com.sun.jdi.SocketAttach")) { //$NON-NLS-1$
				connector= lc;
				break;
			}
		}
		return connector;
	}

	/**
	 * @see IVMConnector#getIdentifier()
	 */
	public String getIdentifier() {
		return IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR;
	}

	/**
	 * @see IVMConnector#getName()
	 */
	public String getName() {
		return LaunchingMessages.getString("SocketAttachConnector.Standard_(Socket_Attach)_4"); //$NON-NLS-1$
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
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.PLUGIN_ID, code, message, exception));
	}		

}
