package org.eclipse.jdt.internal.debug.ui;

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
import org.eclipse.core.runtime.QualifiedName;
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
import org.eclipse.jdt.debug.ui.JavaDebugUI;
import org.eclipse.jdt.debug.ui.JavaUISourceLocator;

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
		return verifyAndLaunch(configuration, mode, true);
	}
		
	/**
	 * Verifies the given configuration can be launched, and attempts the
	 * launch as specified by the <code>launch</code> parameter.
	 * 
	 * @param configuration the configuration to validate and launch
	 * @param mode the mode in which to launch
	 * @param doLaunch whether to launch the configuration after validation
	 *  is complete
	 * @return the result launch or <code>null</code> if the launch
	 *  is not performed.
	 * @exception CoreException if the configuration is invalid or
	 *  if launching fails.
	 */
	protected ILaunch verifyAndLaunch(ILaunchConfiguration configuration, String mode, boolean doLaunch) throws CoreException {
		
		// Java project
		String projectName = configuration.getAttribute(JavaDebugUI.PROJECT_ATTR, (String)null);
		if ((projectName == null) || (projectName.trim().length() < 1)) {
			abort("No project specified", null, JavaDebugUI.UNSPECIFIED_PROJECT);
		}			
		IJavaProject javaProject = getJavaModel().getJavaProject(projectName);
		if ((javaProject == null) || !javaProject.exists()) {
			abort("Invalid project specified", null, JavaDebugUI.NOT_A_JAVA_PROJECT);
		}
				
		// Host
		String hostName = configuration.getAttribute(JavaDebugUI.HOSTNAME_ATTR, "").trim();
		if (hostName.length() < 1) {
			abort("No host name specified", null, JavaDebugUI.UNSPECIFIED_HOSTNAME);
		}
		if (hostName.indexOf(' ') > -1) {
			abort("Invalid host name specified", null, JavaDebugUI.INVALID_HOSTNAME);
		}
		
		// Port
		int portNumber = configuration.getAttribute(JavaDebugUI.PORT_ATTR, Integer.MIN_VALUE);
		if (portNumber == Integer.MIN_VALUE) {
			abort("No port number specified", null, JavaDebugUI.UNSPECIFIED_PORT);
		}
		if (portNumber < 1) {
			abort("Invalid port number specified", null, JavaDebugUI.INVALID_PORT);
		}
				
		// If we were just verifying, we're done
		if (!doLaunch) {
			return null;
		}
		
		// Allow termination of remote VM
		boolean allowTerminate = configuration.getAttribute(JavaDebugUI.ALLOW_TERMINATE_ATTR, false);
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
				abort("Failed to connect to remote VM", ioe, JavaDebugUI.REMOTE_VM_CONNECTION_FAILED);
			} catch (IllegalConnectorArgumentsException icae) {
				JDIDebugUIPlugin.logError(icae);
				return null;
			}
		} else {
			abort("Shared memory attaching connector not available", null, JavaDebugUI.SHARED_MEMORY_CONNECTOR_UNAVAILABLE);
		}

		// Persist config info as default values on the launched resource
		IResource projectResource = null;
		try {
			projectResource = javaProject.getUnderlyingResource();
		} catch (CoreException ce) {			
		}		
			
		ISourceLocator sourceLocator = new JavaUISourceLocator(javaProject);
		Launch launch = new Launch(configuration, mode, sourceLocator, null, debugTarget);
		return launch;		
	}

	/**
	 * Convenience method to set a persistent property on the specified IResource
	 */
	protected void persistAttribute(QualifiedName qualName, IResource resource, String value) {
		try {
			resource.setPersistentProperty(qualName, value);
		} catch (CoreException ce) {	
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
	 * Throws a core exception with the given message and optional
	 * exception. The exception's status code will indicate an error.
	 * 
	 * @param message error message
	 * @param exception cause of the error, or <code>null</code>
	 * @exception CoreException with the given message and underlying
	 *  exception
	 */
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
		  code, message, exception));
	}
	
	/**
	 * Construct a new config name using the name of the given config as a starting point.
	 * The new name is guaranteed not to collide with any existing config name.
	 */
	protected String generateUniqueNameFrom(String startingName) {
		String newName = startingName;
		int index = 1;
		while (getLaunchManager().isExistingLaunchConfigurationName(newName)) {
			StringBuffer buffer = new StringBuffer(startingName);
			buffer.append(" (#");
			buffer.append(String.valueOf(index));
			buffer.append(')');	
			index++;
			newName = buffer.toString();		
		}		
		return newName;
	}
	
	/**
	 * Convenience method to get the java model.
	 */
	private IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
	}

	/**
	 * Convenience method to return the launch manager.
	 * 
	 * @return the launch manager
	 */
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Convenience method to get the workspace root.
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
}
