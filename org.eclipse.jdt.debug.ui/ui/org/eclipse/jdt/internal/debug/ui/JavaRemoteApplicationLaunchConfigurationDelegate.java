package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.ui.JavaDebugUI;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaUISourceLocator;
import org.eclipse.ui.IEditorInput;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

/**
 * Launch configuration delegate for a remote Java application.
 */
public class JavaRemoteApplicationLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	// QualifiedName constants used in writing and retrieving persisted default attribute values
	private static final QualifiedName fgQualNameContainer = new QualifiedName(IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.ATTR_CONTAINER);
	private static final QualifiedName fgQualNameRunPerspId = new QualifiedName(IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE);
	private static final QualifiedName fgQualNameDebugPerspId = new QualifiedName(IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE);
	private static final QualifiedName fgQualNameHostName = new QualifiedName(JavaDebugUI.PLUGIN_ID, JavaDebugUI.HOSTNAME_ATTR);
	private static final QualifiedName fgQualNamePortNumber = new QualifiedName(JavaDebugUI.PLUGIN_ID, JavaDebugUI.PORT_ATTR);
	private static final QualifiedName fgQualNameAllowTerminate = new QualifiedName(JavaDebugUI.PLUGIN_ID, JavaDebugUI.ALLOW_TERMINATE_ATTR);

	/**
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String)
	 */
	public ILaunch launch(ILaunchConfiguration configuration, String mode) throws CoreException {
		return verifyAndLaunch(configuration, mode, true);
	}

	/**
	 * @see ILaunchConfigurationDelegate#verify(ILaunchConfiguration, String)
	 */
	public void verify(ILaunchConfiguration configuration, String mode) throws CoreException {
		verifyAndLaunch(configuration, mode, false);
	}

	/**
	 * This delegate can initialize defaults for context objects that are IJavaElements
	 * (ICompilationUnits or IClassFiles), IFiles, and IEditorInputs.  The job of this method
	 * is to get an IJavaElement for the context then call the method that does the real work.
	 * 
	 * @see ILaunchConfigurationDelegate#initializeDefaults(ILaunchConfigurationWorkingCopy, Object)
	 */
	public void initializeDefaults(ILaunchConfigurationWorkingCopy configuration, Object object) {
		if (object instanceof IJavaElement) {
			initializeDefaults(configuration, (IJavaElement)object);
		} else if (object instanceof IFile) {
			IJavaElement javaElement = JavaCore.create((IFile)object);
			initializeDefaults(configuration, javaElement);			
		} else if (object instanceof IEditorInput) {
			IJavaElement javaElement = (IJavaElement) ((IEditorInput)object).getAdapter(IJavaElement.class);
			initializeDefaults(configuration, javaElement);
		}		
	}

	/**
	 * Attempt to initialize default attribute values on the specified working copy by
	 * retrieving the values from persistent storage on the resource associated with the
	 * specified IJavaElement.  If any of the required attributes cannot be found in
	 * persistent storage, this is taken to mean that there are no persisted defaults for 
	 * the IResource, and the working copy is initialized entirely from the IJavaElement.
	 */
	protected void initializeDefaults(ILaunchConfigurationWorkingCopy workingCopy, IJavaElement javaElement) {
		
		// The java project & main type are ALWAYS initialized from context (it wouldn't make
		// sense to persist these)
		initializeFromContextJavaProject(workingCopy, javaElement);
		initializeFromContextName(workingCopy, javaElement);
		
		// Retrieve the IResource for the java element
		boolean initializeFromPersisted = true;
		IResource resource = null;
		try {
			resource = javaElement.getUnderlyingResource();
		} catch (CoreException ce) {
		}
		if (resource == null) {
			initializeFromPersisted = false;
		}
		
		// The VM attributes are 'required' in the sense that if any launch config attributes 
		// were persisted for this resource, these must be also.  
		if (initializeFromPersisted) {
			initializeFromPersisted = initializeFromPersistedHostName(workingCopy, resource);
			initializeFromPersisted &= initializeFromPersistedPortNumber(workingCopy, resource);			
		}
		
		// If we have so far successfully initialized the working copy from persisted information,
		// initialize the rest of the working copy attributes from persisted info, otherwise
		// initialize the working copy from contextual and 'hard-coded' defaults
		if (initializeFromPersisted) {
			initializeFromPersistedContainer(workingCopy, resource);
			initializeFromPersistedPerspectives(workingCopy, resource);
			initializeFromPersistedAllowTerminate(workingCopy, resource);
		} else {
			initializeFromDefaultHostName(workingCopy);
			initializeFromDefaultPortNumber(workingCopy);
			initializeFromDefaultAllowTerminate(workingCopy);
			initializeFromDefaultContainer(workingCopy);
			initializeFromDefaultPerspectives(workingCopy);			
		}
	}
	
	/**
	 * Set the java project attribute on the working copy based on the IJavaElement.
	 */
	protected void initializeFromContextJavaProject(ILaunchConfigurationWorkingCopy workingCopy, IJavaElement javaElement) {
		IJavaProject javaProject = javaElement.getJavaProject();
		if ((javaProject == null) || !javaProject.exists()) {
			return;
		}
		workingCopy.setAttribute(JavaDebugUI.PROJECT_ATTR, javaProject.getElementName());		
	}
	
	/**
	 * Find the first instance of a type, compilation unit, class file or project in the
	 * specified element's parental hierarchy, and use this as the default name.
	 */
	protected void initializeFromContextName(ILaunchConfigurationWorkingCopy workingCopy, IJavaElement javaElement) {
		String name = "";
		try {
			IResource resource = javaElement.getUnderlyingResource();
			name = resource.getName();
			int index = name.lastIndexOf('.');
			if (index > 0) {
				name = name.substring(0, index);
			}
			workingCopy.rename(generateUniqueNameFrom(name));				
		} catch (JavaModelException jme) {
		}
	}

	protected void initializeFromDefaultHostName(ILaunchConfigurationWorkingCopy workingCopy) {
		workingCopy.setAttribute(JavaDebugUI.HOSTNAME_ATTR, "localhost"); //$NON-NLS-1$
	}
	
	protected void initializeFromDefaultPortNumber(ILaunchConfigurationWorkingCopy workingCopy) {
		workingCopy.setAttribute(JavaDebugUI.PORT_ATTR, 8000);	
	}
	
	protected void initializeFromDefaultAllowTerminate(ILaunchConfigurationWorkingCopy workingCopy) {
		workingCopy.setAttribute(JavaDebugUI.ALLOW_TERMINATE_ATTR, false);			
	}
	
	/**
	 * Set the default storage location of the working copy to local.
	 */
	protected void initializeFromDefaultContainer(ILaunchConfigurationWorkingCopy workingCopy) {
		workingCopy.setContainer(null);		
	}
	
	/**
	 * Set the default perspectives for Run & Debug to the DebugPerspective.
	 */
	protected void initializeFromDefaultPerspectives(ILaunchConfigurationWorkingCopy workingCopy) {
		String debugPerspID = IDebugUIConstants.ID_DEBUG_PERSPECTIVE;
		workingCopy.setAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, debugPerspID);
		workingCopy.setAttribute(IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE, debugPerspID);				
	}
	
	protected boolean initializeFromPersistedHostName(ILaunchConfigurationWorkingCopy workingCopy, IResource resource) {
		try {
			String hostName = resource.getPersistentProperty(fgQualNameHostName);
			if (hostName == null) {
				return false;
			}	
			workingCopy.setAttribute(JavaDebugUI.HOSTNAME_ATTR, hostName);
		} catch (CoreException ce) {
			return false;			
		}	
		return true;
	}
	
	protected boolean initializeFromPersistedPortNumber(ILaunchConfigurationWorkingCopy workingCopy, IResource resource) {
		try {
			String portNumberString = resource.getPersistentProperty(fgQualNamePortNumber);
			int portNumber = 0;
			try {
				portNumber = Integer.parseInt(portNumberString);
			} catch (NumberFormatException nfe) {
				return false;
			}
			workingCopy.setAttribute(JavaDebugUI.PORT_ATTR, portNumber);
		} catch (CoreException ce) {			
			return false;
		}	
		return true;	
	}
	
	protected void initializeFromPersistedAllowTerminate(ILaunchConfigurationWorkingCopy workingCopy, IResource resource) {
		try {
			String allowTerminateString = resource.getPersistentProperty(fgQualNameAllowTerminate);
			if (allowTerminateString != null) {
				boolean allowTerminate = Boolean.getBoolean(allowTerminateString);
				workingCopy.setAttribute(JavaDebugUI.ALLOW_TERMINATE_ATTR, allowTerminate);
			}
		} catch (CoreException ce) {			
		}
	}
	
	protected void initializeFromPersistedContainer(ILaunchConfigurationWorkingCopy workingCopy, IResource resource) {
		try {
			String containerName = resource.getPersistentProperty(fgQualNameContainer);
			if (containerName == null) {
				workingCopy.setContainer(null);
			} else {
				Path containerPath = new Path(containerName);
				IContainer container = getWorkspaceRoot().getContainerForLocation(containerPath);
				workingCopy.setContainer(container);
			}
		} catch (CoreException ce) {			
		}		
	}
	
	protected void initializeFromPersistedPerspectives(ILaunchConfigurationWorkingCopy workingCopy, IResource resource) {
		try {
			String runPersp = resource.getPersistentProperty(fgQualNameRunPerspId);
			workingCopy.setAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, runPersp);
			String debugPersp = resource.getPersistentProperty(fgQualNameDebugPerspId);
			workingCopy.setAttribute(IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE, debugPersp);
		} catch (CoreException ce) {			
		}
		
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
		String projectName = configuration.getAttribute(JavaDebugUI.PROJECT_ATTR, null);
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
				
		if (!doLaunch) {
			// just verify
			return null;
		}
		
		boolean allowTerminate = configuration.getAttribute(JavaDebugUI.ALLOW_TERMINATE_ATTR, false);
		String portNumberString = String.valueOf(portNumber);
		
		// Persist config info as default values on the launched resource
		IResource projectResource = null;
		try {
			projectResource = javaProject.getUnderlyingResource();
		} catch (CoreException ce) {			
		}		
		if (projectResource != null) {
			persistAttribute(fgQualNameHostName, projectResource, hostName);
			persistAttribute(fgQualNamePortNumber, projectResource, portNumberString);
			persistAttribute(fgQualNameAllowTerminate, projectResource, String.valueOf(allowTerminate));
		}
			
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
