package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.ui.JavaDebugUI;
import org.eclipse.jdt.debug.ui.JavaUISourceLocator;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jdt.launching.VMRunnerResult;

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

