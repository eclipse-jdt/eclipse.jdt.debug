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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

/**
 * Launch configuration delegate for a remote Java application.
 */
public class JavaRemoteApplicationLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate {

	/**
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, IProgressMonitor)
	 */
	public ILaunch launch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
						
		// Host
		String hostName = getHostName(configuration);

		if (hostName.indexOf(' ') > -1) {
			abort("Invalid host name specified", null, IJavaLaunchConfigurationConstants.ERR_INVALID_HOSTNAME);
		}
		
		// Port
		int portNumber = verifyPortNumber(configuration);
						
		// Allow termination of remote VM
		boolean allowTerminate = isAllowTermiante(configuration);
		String portNumberString = Integer.toString(portNumber);
		
		IDebugTarget debugTarget = null;
		AttachingConnector connector= getAttachingConnector();
		if (connector != null) {
			Map map= connector.defaultArguments();
			Connector.Argument param= (Connector.Argument) map.get("hostname"); //$NON-NLS-1$
			param.setValue(hostName);
			param= (Connector.Argument) map.get("port"); //$NON-NLS-1$
			param.setValue(portNumberString);
			try {
				VirtualMachine vm= connector.attach(map);
				String vmLabel = constructVMLabel(vm, hostName, portNumber);
				debugTarget= JDIDebugModel.newDebugTarget(vm, vmLabel, null, allowTerminate, true);
			} catch (IOException ioe) {
				abort("Failed to connect to remote VM", ioe, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED);
			} catch (IllegalConnectorArgumentsException icae) {
				LaunchingPlugin.log(icae);
				return null;
			}
		} else {
			abort("Shared memory attaching connector not available", null, IJavaLaunchConfigurationConstants.ERR_SHARED_MEMORY_CONNECTOR_UNAVAILABLE);
		}
		
		// Create & return Launch:
		//  - set default source locator if none specified
		ISourceLocator sourceLocator = null;
		String id = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
		if (id == null) {
			IJavaProject javaProject = JavaLaunchConfigurationHelper.getJavaProject(configuration);
			sourceLocator = new JavaSourceLocator(javaProject);
		}
		Launch launch = new Launch(configuration, mode, sourceLocator, null, debugTarget);
		return launch;		
	}
	
	/**
	 * Helper method that constructs a human-readable label for a launch.
	 */
	protected String constructVMLabel(VirtualMachine vm, String host, int port) {
		StringBuffer buffer = new StringBuffer(vm.name());
		buffer.append('[');
		buffer.append(host);
		buffer.append(':');
		buffer.append(port);
		buffer.append(']');
		return buffer.toString();
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

}
