package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.ui.JavaDebugUI;
import org.eclipse.jdt.debug.ui.JavaUISourceLocator;
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

	/**
	 * Create the helper class that handles deleting configs whose underlying main type gets deleted
	 */
	static {
		new JavaLocalApplicationLaunchConfigurationHelper();
	}

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
		} else {
			initializeHardCodedDefaults(configuration);
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
		
		// First look for a default config for this config type and the specified resource
		if (javaElement != null) {
			try {
				IResource resource = javaElement.getUnderlyingResource();
				if (resource != null) {
					String configTypeID = workingCopy.getType().getIdentifier();
					boolean foundDefault = getLaunchManager().initializeFromDefaultLaunchConfiguration(resource, workingCopy, configTypeID);
					if (foundDefault) {
						initializeFromContextJavaProject(workingCopy, javaElement);
						initializeFromContextMainTypeAndName(workingCopy, javaElement);
						return;
					}
				}
			} catch (JavaModelException jme) {			
			} catch (CoreException ce) {			
			}
		}
				
		// If no default config was found, initialize all attributes we can from the specified 
		// context object and from 'hard-coded' defaults known to this delegate
		if (javaElement != null) {
			initializeFromContextJavaProject(workingCopy, javaElement);
			initializeFromContextMainTypeAndName(workingCopy, javaElement);
		}
		initializeHardCodedDefaults(workingCopy);
	}
	
	/**
	 * Initialize those attributes whose default values are independent of any context.
	 */
	protected void initializeHardCodedDefaults(ILaunchConfigurationWorkingCopy workingCopy) {
		initializeFromDefaultVM(workingCopy);
		initializeFromDefaultContainer(workingCopy);
		initializeFromDefaultPerspectives(workingCopy);	
		initializeFromDefaultBuild(workingCopy);						
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
		try {
			IType[] types = MainMethodFinder.findTargets(new BusyIndicatorRunnableContext(), new Object[] {javaElement});
			if ((types == null) || (types.length < 1)) {
				return;
			}
			// Simply grab the first main type found in the searched element
			String fullyQualifiedName = types[0].getFullyQualifiedName();
			workingCopy.setAttribute(JavaDebugUI.MAIN_TYPE_ATTR, fullyQualifiedName);
			String name = types[0].getElementName();
			workingCopy.rename(generateUniqueNameFrom(name));	
		} catch (InterruptedException ie) {
		} catch (InvocationTargetException ite) {
		}			
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
	 * Set the default 'build before launch' value.
	 */
	protected void initializeFromDefaultBuild(ILaunchConfigurationWorkingCopy workingCopy) {
		workingCopy.setAttribute(JavaDebugUI.BUILD_BEFORE_LAUNCH_ATTR, false);				
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
		
		/*
		// Java project
		String projectName = configuration.getAttribute(JavaDebugUI.PROJECT_ATTR, (String)null);
		if ((projectName == null) || (projectName.trim().length() < 1)) {
			abort("No project specified", null, JavaDebugUI.UNSPECIFIED_PROJECT);
		}			
		IJavaProject javaProject = getJavaModel().getJavaProject(projectName);
		if ((javaProject == null) || !javaProject.exists()) {
			abort("Invalid project specified", null, JavaDebugUI.NOT_A_JAVA_PROJECT);
		}
		
		// Main type
		String mainTypeName = configuration.getAttribute(JavaDebugUI.MAIN_TYPE_ATTR, (String)null);
		if ((mainTypeName == null) || (mainTypeName.trim().length() < 1)) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.Main_type_not_specified._1"), null, JavaDebugUI.UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
		IType mainType = null;
		try {
			mainType = JavaLocalApplicationLaunchConfigurationHelper.findType(javaProject, mainTypeName);
		} catch (JavaModelException jme) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.Main_type_does_not_exist"), null, JavaDebugUI.UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
		if (mainType == null) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.Main_type_does_not_exist"), null, JavaDebugUI.UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
		*/
		
		// Java project
		IJavaProject javaProject = JavaLocalApplicationLaunchConfigurationHelper.getJavaProject(configuration);
		
		// Main type
		IType mainType = JavaLocalApplicationLaunchConfigurationHelper.getMainType(configuration, javaProject);
				
		// VM install type
		String vmInstallTypeId = configuration.getAttribute(JavaDebugUI.VM_INSTALL_TYPE_ATTR, (String)null);
		if (vmInstallTypeId == null) {
			JavaLocalApplicationLaunchConfigurationHelper.abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.JRE_Type_not_specified._2"), null, JavaDebugUI.UNSPECIFIED_VM_INSTALL_TYPE); //$NON-NLS-1$
		}		
		IVMInstallType type = JavaRuntime.getVMInstallType(vmInstallTypeId);
		if (type == null) {
			JavaLocalApplicationLaunchConfigurationHelper.abort(MessageFormat.format(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.VM_Install_type_does_not_exist"), new String[] {vmInstallTypeId}), null, JavaDebugUI.VM_INSTALL_TYPE_DOES_NOT_EXIST); //$NON-NLS-1$
		}
		
		// VM
		String vmInstallId = configuration.getAttribute(JavaDebugUI.VM_INSTALL_ATTR, (String)null);
		if (vmInstallId == null) {
			JavaLocalApplicationLaunchConfigurationHelper.abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.JRE_not_specified._3"), null, JavaDebugUI.UNSPECIFIED_VM_INSTALL); //$NON-NLS-1$
		}
		IVMInstall install = type.findVMInstall(vmInstallId);
		if (install == null) {
			JavaLocalApplicationLaunchConfigurationHelper.abort(MessageFormat.format(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.JRE_{0}_does_not_exist._4"), new String[]{vmInstallId}), null, JavaDebugUI.VM_INSTALL_DOES_NOT_EXIST); //$NON-NLS-1$
		}		
		IVMRunner runner = install.getVMRunner(mode);
		if (runner == null) {
			JavaLocalApplicationLaunchConfigurationHelper.abort(MessageFormat.format(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.Internal_error__JRE_{0}_does_not_specify_a_VM_Runner._5"), new String[]{vmInstallId}), null, JavaDebugUI.VM_RUNNER_DOES_NOT_EXIST); //$NON-NLS-1$
		}
		
		// Working directory
		String workingDir = configuration.getAttribute(JavaDebugUI.WORKING_DIRECTORY_ATTR, (String)null);
		if ((workingDir != null) && (workingDir.trim().length() > 0)) {
			File dir = new File(workingDir);
			if (!dir.isDirectory()) {
				JavaLocalApplicationLaunchConfigurationHelper.abort(MessageFormat.format(DebugUIMessages.getString("JavaApplicationLaunchConfiguration.Working_directory_does_not_exist"), new String[] {workingDir}), null, JavaDebugUI.WORKING_DIRECTORY_DOES_NOT_EXIST); //$NON-NLS-1$
			}
		}
		
		// If we were just verifying, we're done
		if (!doLaunch) {
			return null;
		}
		
		// Build before launch
		boolean build = configuration.getAttribute(JavaDebugUI.BUILD_BEFORE_LAUNCH_ATTR, false);
		if (build) {
			if (!DebugUIPlugin.saveAndBuild()) {
				return null;
			}			
		}
		
		// Program & VM args
		String pgmArgs = configuration.getAttribute(JavaDebugUI.PROGRAM_ARGUMENTS_ATTR, "");	//$NON-NLS-1$
		String vmArgs = configuration.getAttribute(JavaDebugUI.VM_ARGUMENTS_ATTR, ""); //$NON-NLS-1$
		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
		
		// Classpath
		List classpathList = configuration.getAttribute(JavaDebugUI.CLASSPATH_ATTR, (List)null);
		String[] classpath;
		if (classpathList == null) {
			classpath = JavaRuntime.computeDefaultRuntimeClassPath(javaProject);
		} else {
			classpath = new String[classpathList.size()];
			classpathList.toArray(classpath);
		}
		
		// Create VM config
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainType.getFullyQualifiedName(), classpath);
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDir);

		// Bootpath
		List bootpathList = configuration.getAttribute(JavaDebugUI.BOOTPATH_ATTR, (List)null);
		if (bootpathList != null) {
			String[] bootpath = new String[bootpathList.size()];
			bootpathList.toArray(bootpath);
			runConfig.setBootClassPath(bootpath);
		}
		
		// Launch the configuration
		VMRunnerResult result = runner.run(runConfig);
		
		
		// Persist config info as default values on the launched resource
		IResource resource = null;
		try {
			resource = mainType.getUnderlyingResource();
		} catch (CoreException ce) {			
		}		
		if (resource != null) {
			getLaunchManager().setDefaultLaunchConfiguration(resource, configuration);
		}

		if (result == null) {
			return null;
		}
		// Create & return Launch
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
	 * Construct a new config name using the name of the given config as a starting point.
	 * The new name is guaranteed not to collide with any existing config name.
	 */
	protected String generateUniqueNameFrom(String startingName) {
		int index = 1;
		String baseName = startingName;
		int underscoreIndex = baseName.lastIndexOf('_');
		if (underscoreIndex > -1) {
			String trailer = baseName.substring(underscoreIndex + 1);
			try {
				index = Integer.parseInt(trailer);
				baseName = startingName.substring(0, underscoreIndex);
			} catch (NumberFormatException nfe) {
			}
		} 
		String newName = baseName;
		while (getLaunchManager().isExistingLaunchConfigurationName(newName)) {
			StringBuffer buffer = new StringBuffer(baseName);
			buffer.append('_');
			buffer.append(String.valueOf(index));
			index++;
			newName = buffer.toString();		
		}		
		return newName;
		
	}
	
	/**
	 * Convenience method to get the launch manager.
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

