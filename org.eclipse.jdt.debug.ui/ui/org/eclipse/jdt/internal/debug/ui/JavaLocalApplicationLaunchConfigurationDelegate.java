package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.ui.JavaDebugUI;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaUISourceLocator;
import org.eclipse.jdt.internal.debug.ui.launcher.MainMethodFinder;
import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jdt.launching.VMRunnerResult;
import org.eclipse.ui.IEditorInput;

/**
 * Launch configuration delegate for a local Java application.
 */
public class JavaLocalApplicationLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	// QualifiedName constants used in writing and retrieving persisted default attribute values
	private static final QualifiedName fgQualNameContainer = new QualifiedName(IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.ATTR_CONTAINER);
	private static final QualifiedName fgQualNameRunPerspId = new QualifiedName(IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE);
	private static final QualifiedName fgQualNameDebugPerspId = new QualifiedName(IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE);
	private static final QualifiedName fgQualNameWorkingDir = new QualifiedName(JavaDebugUI.PLUGIN_ID, JavaDebugUI.WORKING_DIRECTORY_ATTR);
	private static final QualifiedName fgQualNamePgmArgs = new QualifiedName(JavaDebugUI.PLUGIN_ID, JavaDebugUI.PROGRAM_ARGUMENTS_ATTR);
	private static final QualifiedName fgQualNameVMArgs = new QualifiedName(JavaDebugUI.PLUGIN_ID, JavaDebugUI.VM_ARGUMENTS_ATTR);
	private static final QualifiedName fgQualNameVMTypeId = new QualifiedName(JavaDebugUI.PLUGIN_ID, JavaDebugUI.VM_INSTALL_TYPE_ATTR);
	private static final QualifiedName fgQualNameVMId = new QualifiedName(JavaDebugUI.PLUGIN_ID, JavaDebugUI.VM_INSTALL_ATTR);

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
		if ((object instanceof ICompilationUnit) || (object instanceof IClassFile)) {
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
	 * the IResource, and the working copy is initialized entirely from context and
	 * hard-coded defaults.
	 */
	protected void initializeDefaults(ILaunchConfigurationWorkingCopy workingCopy, IJavaElement javaElement) {
		
		// The java project & main type are ALWAYS initialized from context (it wouldn't make
		// sense to persist these)
		initializeFromContextJavaProject(workingCopy, javaElement);
		initializeFromContextMainTypeAndName(workingCopy, javaElement);
		
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
			initializeFromPersisted = initializeFromPersistedVM(workingCopy, resource);
		}
		
		// If we have so far successfully initialized the working copy from persisted information,
		// initialize the rest of the working copy attributes from persisted info, otherwise
		// initialize the working copy from contextual and 'hard-coded' defaults
		if (initializeFromPersisted) {
			initializeFromPersistedContainer(workingCopy, resource);
			initializeFromPersistedPerspectives(workingCopy, resource);
			initializeFromPersistedPgmArgs(workingCopy, resource);
			initializeFromPersistedVMArgs(workingCopy, resource);
			initializeFromPersistedWorkingDir(workingCopy, resource);
		} else {
			initializeFromDefaultVM(workingCopy);
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
	 * Set the main type & name attributes on the working copy based on the IJavaElement
	 */
	protected void initializeFromContextMainTypeAndName(ILaunchConfigurationWorkingCopy workingCopy, IJavaElement javaElement) {
		String name = "";
		try {
			IType[] types = MainMethodFinder.findTargets(new BusyIndicatorRunnableContext(), new Object[] {javaElement});
			if ((types == null) || (types.length < 1)) {
				return;
			}
			name = types[0].getFullyQualifiedName();
			workingCopy.setAttribute(JavaDebugUI.MAIN_TYPE_ATTR, name);
		} catch (InterruptedException ie) {
		} catch (InvocationTargetException ite) {
		}			
		workingCopy.rename(generateUniqueNameFrom(name));	
	}
	
	/**
	 * Set the VM attributes on the working copy based on the workbench default VM.
	 */
	protected void initializeFromDefaultVM(ILaunchConfigurationWorkingCopy workingCopy) {
		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
		IVMInstallType vmInstallType = vmInstall.getVMInstallType();
		String vmInstallTypeID = vmInstallType.getId();
		workingCopy.setAttribute(JavaDebugUI.VM_INSTALL_TYPE_ATTR, vmInstallTypeID);

		String vmInstallID = vmInstall.getId();
		workingCopy.setAttribute(JavaDebugUI.VM_INSTALL_ATTR, vmInstallID);		
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
	
	/**
	 * Attempt to retrieve the VM-related attributes that were persisted for the specified resource.
	 * If successful, set these on the working copy and return true, otherwise return false.
	 */
	protected boolean initializeFromPersistedVM(ILaunchConfigurationWorkingCopy workingCopy, IResource resource) {
		try {
			String installTypeId = resource.getPersistentProperty(fgQualNameVMTypeId);
			if (installTypeId == null) {
				return false;
			}
			workingCopy.setAttribute(JavaDebugUI.VM_INSTALL_TYPE_ATTR, installTypeId);
			
			String installId = resource.getPersistentProperty(fgQualNameVMId);
			if (installId == null) {
				return false;
			}
			workingCopy.setAttribute(JavaDebugUI.VM_INSTALL_ATTR, installId);
		} catch (CoreException ce) {
			return false;
		}	
			
		return true;
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
	
	protected void initializeFromPersistedPgmArgs(ILaunchConfigurationWorkingCopy workingCopy, IResource resource) {
		try {
			String pgmArgs = resource.getPersistentProperty(fgQualNamePgmArgs);
			workingCopy.setAttribute(JavaDebugUI.PROGRAM_ARGUMENTS_ATTR, pgmArgs);
		} catch (CoreException ce) {			
		}
	}
	
	protected void initializeFromPersistedVMArgs(ILaunchConfigurationWorkingCopy workingCopy, IResource resource) {
		try {
			String vmArgs = resource.getPersistentProperty(fgQualNameVMArgs);
			workingCopy.setAttribute(JavaDebugUI.VM_ARGUMENTS_ATTR, vmArgs);
		} catch (CoreException ce) {			
		}		
	}
	
	protected void initializeFromPersistedWorkingDir(ILaunchConfigurationWorkingCopy workingCopy, IResource resource) {
		try {
			String workingDir = resource.getPersistentProperty(fgQualNameWorkingDir);
			workingCopy.setAttribute(JavaDebugUI.WORKING_DIRECTORY_ATTR, workingDir);
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
		
		// Main type
		String mainTypeName = configuration.getAttribute(JavaDebugUI.MAIN_TYPE_ATTR, null);
		if ((mainTypeName == null) || (mainTypeName.trim().length() < 1)) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.Main_type_not_specified._1"), null, JavaDebugUI.UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
		IType mainType = null;
		try {
			mainType = findType(javaProject, mainTypeName);
		} catch (JavaModelException jme) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.Main_type_does_not_exist"), null, JavaDebugUI.UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
		if (mainType == null) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.Main_type_does_not_exist"), null, JavaDebugUI.UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
				
		// VM install type
		String vmInstallTypeId = configuration.getAttribute(JavaDebugUI.VM_INSTALL_TYPE_ATTR, null);
		if (vmInstallTypeId == null) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.JRE_Type_not_specified._2"), null, JavaDebugUI.UNSPECIFIED_VM_INSTALL_TYPE); //$NON-NLS-1$
		}		
		IVMInstallType type = JavaRuntime.getVMInstallType(vmInstallTypeId);
		if (type == null) {
			abort(MessageFormat.format(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.VM_Install_type_does_not_exist"), new String[] {vmInstallTypeId}), null, JavaDebugUI.VM_INSTALL_TYPE_DOES_NOT_EXIST); //$NON-NLS-1$
		}
		
		// VM
		String vmInstallId = configuration.getAttribute(JavaDebugUI.VM_INSTALL_ATTR, null);
		if (vmInstallId == null) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.JRE_not_specified._3"), null, JavaDebugUI.UNSPECIFIED_VM_INSTALL); //$NON-NLS-1$
		}
		IVMInstall install = type.findVMInstall(vmInstallId);
		if (install == null) {
			abort(MessageFormat.format(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.JRE_{0}_does_not_exist._4"), new String[]{vmInstallId}), null, JavaDebugUI.VM_INSTALL_DOES_NOT_EXIST); //$NON-NLS-1$
		}		
		IVMRunner runner = install.getVMRunner(mode);
		if (runner == null) {
			abort(MessageFormat.format(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.Internal_error__JRE_{0}_does_not_specify_a_VM_Runner._5"), new String[]{vmInstallId}), null, JavaDebugUI.VM_RUNNER_DOES_NOT_EXIST); //$NON-NLS-1$
		}
		
		// Working directory
		String workingDir = configuration.getAttribute(JavaDebugUI.WORKING_DIRECTORY_ATTR, null);
		if ((workingDir != null) && (workingDir.trim().length() > 0)) {
			File dir = new File(workingDir);
			if (!dir.isDirectory()) {
				abort(MessageFormat.format(DebugUIMessages.getString("JavaApplicationLaunchConfiguration.Working_directory_does_not_exist"), new String[] {workingDir}), null, JavaDebugUI.WORKING_DIRECTORY_DOES_NOT_EXIST); //$NON-NLS-1$
			}
		}
		
		if (!doLaunch) {
			// just verify
			return null;
		}
		
		String pgmArgs = configuration.getAttribute(JavaDebugUI.VM_ARGUMENTS_ATTR, "");	//$NON-NLS-1$
		String vmArgs = configuration.getAttribute(JavaDebugUI.PROGRAM_ARGUMENTS_ATTR, ""); //$NON-NLS-1$
		ExecutionArguments args = new ExecutionArguments(vmArgs, pgmArgs);			
		String[] classpath = JavaRuntime.computeDefaultRuntimeClassPath(javaProject);
		String bootpath = configuration.getAttribute(JavaDebugUI.BOOTPATH_ATTR, null);

		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainType.getFullyQualifiedName(), classpath);
		runConfig.setProgramArguments(args.getProgramArgumentsArray());
		runConfig.setVMArguments(args.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDir);
		if (bootpath != null) {
			runConfig.setBootClassPath(new String[]{bootpath});
		}
		
		// Get the configuration's container as a String
		IPath location = configuration.getLocation();
		IPath containerPath = location.removeLastSegments(1);
		
		// Get the configuration's perspective id's
		String runPerspID = configuration.getAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, null);
		String debugPerspID = configuration.getAttribute(IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE, null);
				
		VMRunnerResult result = runner.run(runConfig);
		
		// Persist config info as default values on the launched resource
		IResource resource = null;
		try {
			resource = mainType.getUnderlyingResource();
		} catch (CoreException ce) {			
		}		
		if (resource != null) {
			persistAttribute(fgQualNameContainer, resource, containerPath.toString());
			persistAttribute(fgQualNameRunPerspId, resource, runPerspID);
			persistAttribute(fgQualNameDebugPerspId, resource, debugPerspID);			
			persistAttribute(fgQualNameWorkingDir, resource, workingDir);
			persistAttribute(fgQualNamePgmArgs, resource, pgmArgs);
			persistAttribute(fgQualNameVMArgs, resource, vmArgs);
			persistAttribute(fgQualNameVMTypeId, resource, vmInstallTypeId);
			persistAttribute(fgQualNameVMId, resource, vmInstallId);
		}

		ISourceLocator sourceLocator = new JavaUISourceLocator(javaProject);
		Launch launch = new Launch(configuration, mode, sourceLocator, result.getProcesses(), result.getDebugTarget());
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
	 * Find the specified (fully-qualified) type name in the specified java project.
	 */
	private IType findType(IJavaProject javaProject, String mainTypeName) throws JavaModelException {
		String pathStr= mainTypeName.replace('.', '/') + ".java"; //$NON-NLS-1$
		IJavaElement javaElement= javaProject.findElement(new Path(pathStr));
		if (javaElement == null) {
			// try to find it as inner type
			String qualifier= Signature.getQualifier(mainTypeName);
			if (qualifier.length() > 0) {
				IType type= findType(javaProject, qualifier); // recursive!
				if (type != null) {
					IType res= type.getType(Signature.getSimpleName(mainTypeName));
					if (res.exists()) {
						return res;
					}
				}
			}
		} else if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
			String simpleName= Signature.getSimpleName(mainTypeName);
			return ((ICompilationUnit) javaElement).getType(simpleName);
		} else if (javaElement.getElementType() == IJavaElement.CLASS_FILE) {
			return ((IClassFile) javaElement).getType();
		}
		return null;		
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
	 * Convenience method to return the launch manager.
	 * 
	 * @return the launch manager
	 */
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Convenience method to get the java model.
	 */
	private IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
	}

	/**
	 * Convenience method to get the workspace root.
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
}

