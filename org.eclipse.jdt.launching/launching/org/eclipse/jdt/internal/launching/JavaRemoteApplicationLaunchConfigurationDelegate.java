package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;

import com.sun.jdi.VirtualMachine;

/**
 * Launch configuration delegate for a remote Java application.
 */
public class JavaRemoteApplicationLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate {

	/**
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, ILaunch, IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
						
		// Host
		String hostName = getHostName(configuration);

		if (hostName.indexOf(' ') > -1) {
			abort(LaunchingMessages.getString("JavaRemoteApplicationLaunchConfigurationDelegate.Invalid_host_name_specified_1"), null, IJavaLaunchConfigurationConstants.ERR_INVALID_HOSTNAME); //$NON-NLS-1$
		}
		
		// Port
		int portNumber = verifyPortNumber(configuration);
						
		// Allow termination of remote VM
		boolean allowTerminate = isAllowTermiante(configuration);
		
		IDebugTarget debugTarget = null;
		String connectorId = getVMConnectorId(configuration);
		IVMConnector connector = null;
		if (connectorId == null) {
			connector = JavaRuntime.getDefaultVMConnector();
		} else {
			connector = JavaRuntime.getVMConnector(connectorId);
		}
		if (connector == null) {
			abort(LaunchingMessages.getString("JavaRemoteApplicationLaunchConfigurationDelegate.Connector_not_specified_2"), null, IJavaLaunchConfigurationConstants.ERR_CONNECTOR_NOT_AVAILABLE); //$NON-NLS-1$
		}

		VirtualMachine vm= connector.connect(hostName, portNumber, monitor);
		String vmLabel = constructVMLabel(vm, hostName, portNumber);
		debugTarget= JDIDebugModel.newDebugTarget(launch, vm, vmLabel, null, allowTerminate, true);
		
		launch.addDebugTarget(debugTarget);
		
		if (launch.getSourceLocator() == null) {
			//  set default source locator if none specified
			String id = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
			if (id == null) {
				IJavaProject javaProject = JavaLaunchConfigurationUtils.getJavaProject(configuration);
				if (javaProject != null) {
					ISourceLocator sourceLocator = new JavaSourceLocator(javaProject);
					launch.setSourceLocator(sourceLocator);
				}
			}
		}
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
	
}
