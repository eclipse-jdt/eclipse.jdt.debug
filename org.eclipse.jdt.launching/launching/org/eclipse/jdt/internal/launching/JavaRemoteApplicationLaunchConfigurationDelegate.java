package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.JavaRuntime;

import com.sun.jdi.VirtualMachine;

/**
 * Launch configuration delegate for a remote Java application.
 */
public class JavaRemoteApplicationLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate {

	/**
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, ILaunch, IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(LaunchingMessages.getString("JavaRemoteApplicationLaunchConfigurationDelegate.Connecting..._1"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}						
						
		// Allow termination of remote VM
		boolean allowTerminate = isAllowTerminate(configuration);
		
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
		
		Map argMap = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, (Map)null);

		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		VirtualMachine vm = null;
		try {
			// momentarily place the launch config handle into the connect map (bug 19379)
			argMap.put(IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION, configuration.getMemento());		
			vm= connector.connect(argMap, monitor);
		} finally {
			// remove the launch config handle from the arg map, so launch config does not have unsaved changes
			argMap.remove(IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION);
		}
		
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
				
		String vmLabel = constructVMLabel(vm);
						
		debugTarget= JDIDebugModel.newDebugTarget(launch, vm, vmLabel, null, allowTerminate, true);
		
		launch.addDebugTarget(debugTarget);
		
		// set the default source locator if required
		setDefaultSourceLocator(launch, configuration);
		
		monitor.done();
	}
	
	/**
	 * Helper method that constructs a human-readable label for a launch.
	 */
	protected String constructVMLabel(VirtualMachine vm) {
		StringBuffer buffer = new StringBuffer(vm.name());
		return buffer.toString();
	}
	
}
