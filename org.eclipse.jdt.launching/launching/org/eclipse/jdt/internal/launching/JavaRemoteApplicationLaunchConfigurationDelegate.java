package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

/**
 * Launch configuration delegate for a remote Java application.
 */
public class JavaRemoteApplicationLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	/**
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String)
	 */
	public ILaunch launch(ILaunchConfiguration configuration, String mode) throws CoreException {
		
		// Java project
		String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
		if ((projectName == null) || (projectName.trim().length() < 1)) {
			abort("No project specified", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_PROJECT);
		}			
		IJavaProject javaProject = getJavaModel().getJavaProject(projectName);
		if ((javaProject == null) || !javaProject.exists()) {
			abort("Invalid project specified", null, IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);
		}
				
		// Host
		String hostName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_HOSTNAME, "").trim();
		if (hostName.length() < 1) {
			abort("No host name specified", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_HOSTNAME);
		}
		if (hostName.indexOf(' ') > -1) {
			abort("Invalid host name specified", null, IJavaLaunchConfigurationConstants.ERR_INVALID_HOSTNAME);
		}
		
		// Port
		int portNumber = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PORT_NUMBER, Integer.MIN_VALUE);
		if (portNumber == Integer.MIN_VALUE) {
			abort("No port number specified", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_PORT);
		}
		if (portNumber < 1) {
			abort("Invalid port number specified", null, IJavaLaunchConfigurationConstants.ERR_INVALID_PORT);
		}
						
		// Allow termination of remote VM
		boolean allowTerminate = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
		String portNumberString = String.valueOf(portNumber);
		
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

		// Persist config info as default values on the launched resource
		IResource projectResource = null;
		try {
			projectResource = javaProject.getUnderlyingResource();
		} catch (CoreException ce) {			
		}		
			
		ISourceLocator sourceLocator = new JavaSourceLocator(javaProject);
		Launch launch = new Launch(configuration, mode, sourceLocator, null, debugTarget);
		return launch;		
	}

	/**
	 * Convenience method to get the workspace root.
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	/**
	 * Convenience method to get the java model.
	 */
	private IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
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

	/**
	 * @see JavaLocalApplicationLaunchConfigurationHelper#abort(String, Throwable, int)
	 */
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		JavaLaunchConfigurationHelper.abort(message, exception, code);
	}
	
	/**
	 * Convenience method to return the launch manager.
	 * 
	 * @return the launch manager
	 */
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	
}
