/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.osgi.util.NLS;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

/**
 * A standard socket attaching connector
 */
public class SocketAttachConnector implements IVMConnector {

	/**
	 * Return the socket transport attaching connector
	 *
	 * @return the {@link AttachingConnector}
	 * @exception CoreException if unable to locate the connector
	 */
	protected static AttachingConnector getAttachingConnector() throws CoreException {
		AttachingConnector connector= null;
		Iterator<AttachingConnector> iter= Bootstrap.virtualMachineManager().attachingConnectors().iterator();
		while (iter.hasNext()) {
			AttachingConnector lc= iter.next();
			if (lc.name().equals("com.sun.jdi.SocketAttach")) { //$NON-NLS-1$
				connector= lc;
				break;
			}
		}
		if (connector == null) {
			abort(LaunchingMessages.SocketAttachConnector_Socket_attaching_connector_not_available_3, null, IJavaLaunchConfigurationConstants.ERR_SHARED_MEMORY_CONNECTOR_UNAVAILABLE);
		}
		return connector;
	}

	/**
	 * @see IVMConnector#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR;
	}

	/**
	 * @see IVMConnector#getName()
	 */
	@Override
	public String getName() {
		return LaunchingMessages.SocketAttachConnector_Standard__Socket_Attach__4;
	}

	/**
	 * Throws a core exception with an error status object built from
	 * the given message, lower level exception, and error code.
	 *
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @param code error code
	 * @throws CoreException if an error occurs
	 */
	protected static void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), code, message, exception));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMConnector#connect(java.util.Map, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.debug.core.ILaunch)
	 */
	@Override
	public void connect(Map<String, String> arguments, IProgressMonitor monitor, ILaunch launch) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, LaunchingMessages.SocketAttachConnector_Connecting____1, 2);
		subMonitor.subTask(LaunchingMessages.SocketAttachConnector_Configuring_connection____1);

		AttachingConnector connector= getAttachingConnector();
		String portNumberString = arguments.get("port"); //$NON-NLS-1$
		if (portNumberString == null) {
			abort(LaunchingMessages.SocketAttachConnector_Port_unspecified_for_remote_connection__2, null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_PORT);
		}
		String host = arguments.get("hostname"); //$NON-NLS-1$
		if (host == null) {
			abort(LaunchingMessages.SocketAttachConnector_Hostname_unspecified_for_remote_connection__4, null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_HOSTNAME);
		}
		Map<String, Connector.Argument> map= connector.defaultArguments();

        Connector.Argument param= map.get("hostname"); //$NON-NLS-1$
		param.setValue(host);
		param= map.get("port"); //$NON-NLS-1$
		param.setValue(portNumberString);

        String timeoutString = arguments.get("timeout"); //$NON-NLS-1$
        if (timeoutString != null) {
            param= map.get("timeout"); //$NON-NLS-1$
            param.setValue(timeoutString);
        }

		ILaunchConfiguration configuration = launch.getLaunchConfiguration();
		boolean allowTerminate = false;
		if (configuration != null) {
			allowTerminate = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
		}
		subMonitor.worked(1);
		subMonitor.subTask(LaunchingMessages.SocketAttachConnector_Establishing_connection____2);
		try {
			VirtualMachine vm = connector.attach(map);
			String vmLabel = constructVMLabel(vm, host, portNumberString, configuration);
			IDebugTarget debugTarget= JDIDebugModel.newDebugTarget(launch, vm, vmLabel, null, allowTerminate, true);
			launch.addDebugTarget(debugTarget);
			subMonitor.worked(1);
			subMonitor.done();
        } catch (TimeoutException e) {
            abort(LaunchingMessages.SocketAttachConnector_0, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED);
		} catch (UnknownHostException e) {
			abort(NLS.bind(LaunchingMessages.SocketAttachConnector_Failed_to_connect_to_remote_VM_because_of_unknown_host____0___1, host), e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED);
		} catch (ConnectException e) {
			abort(LaunchingMessages.SocketAttachConnector_Failed_to_connect_to_remote_VM_as_connection_was_refused_2, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED);
		} catch (IOException e) {
			abort(LaunchingMessages.SocketAttachConnector_Failed_to_connect_to_remote_VM_1, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED);
		} catch (IllegalConnectorArgumentsException e) {
			abort(LaunchingMessages.SocketAttachConnector_Failed_to_connect_to_remote_VM_1, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED);
		}
	}

	/**
	 * Helper method that constructs a human-readable label for a remote VM.
	 * @param vm the VM
	 * @param host the host name
	 * @param port the port number
	 * @param configuration the backing configuration
	 * @return the new label for the VM
	 */
	protected String constructVMLabel(VirtualMachine vm, String host, String port, ILaunchConfiguration configuration) {
		String name = null;
		try {
			name = vm.name();
		} catch (TimeoutException e) {
			// do nothing
		} catch (VMDisconnectedException e) {
			// do nothing
		}
		if (name == null) {
			if (configuration == null) {
				name = ""; //$NON-NLS-1$
			} else {
				name = configuration.getName();
			}
		}
		StringBuilder buffer = new StringBuilder(name);
		if(!"".equals(name)) { //$NON-NLS-1$
			buffer.append(' ');
		}
		buffer.append('[');
		buffer.append(host);
		buffer.append(':');
		buffer.append(port);
		buffer.append(']');
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMConnector#getDefaultArguments()
	 */
	@Override
	public Map<String, Connector.Argument> getDefaultArguments() throws CoreException {
		Map<String, Connector.Argument> def = getAttachingConnector().defaultArguments();
		Connector.IntegerArgument arg = (Connector.IntegerArgument)def.get("port"); //$NON-NLS-1$
		arg.setValue(8000);
		return def;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMConnector#getArgumentOrder()
	 */
	@Override
	public List<String> getArgumentOrder() {
		List<String> list = new ArrayList<>(2);
		list.add("hostname"); //$NON-NLS-1$
		list.add("port"); //$NON-NLS-1$
		return list;
	}
}
