package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.ui.JavaDebugUI;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaUISourceLocator;
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
public class JavaApplicationLaunchConfigurationDelegate
	implements ILaunchConfigurationDelegate {

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
	 * @see ILaunchConfigurationDelegate#initializeDefaults(ILaunchConfigurationWorkingCopy, Object)
	 */
	public void initializeDefaults(
		ILaunchConfigurationWorkingCopy configuration,
		Object object) {
	}
	
	/**
	 * Verifies the given configuration can be launched, and attempts the
	 * launch as specified by the <code>launch</code> parameter.
	 * 
	 * @param configuration the configuration to validate and launch
	 * @param mode the mode in which to launch
	 * @param lanuch whether to launch the configuration after validation
	 *  is complete
	 * @return the result launch or <code>null</code> if the launch
	 *  is not performed.
	 * @exception CoreException if the configuration is invalid or
	 *  if launching fails.
	 * 
	 * [Issue: it is assumed that a launch configuration resides in
	 *  the project to be launched. It is not possible to store configurations
	 *  to launch project A in project B.]
	 */
	protected ILaunch verifyAndLaunch(ILaunchConfiguration configuration, String mode, boolean doLaunch) throws CoreException {
		String mainType = configuration.getAttribute(JavaDebugUI.MAIN_TYPE_ATTR, null);
		if (mainType == null) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.Main_type_not_specified._1"), null, JavaDebugUI.UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
		String installTypeId = configuration.getAttribute(JavaDebugUI.VM_INSTALL_TYPE_ATTR, null);
		if (installTypeId == null) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.JRE_Type_not_specified._2"), null, JavaDebugUI.UNSPECIFIED_VM_INSTALL_TYPE); //$NON-NLS-1$
		}
		IVMInstallType type = JavaRuntime.getVMInstallType(installTypeId);
				
		String installId = configuration.getAttribute(JavaDebugUI.VM_INSTALL_ATTR, null);
		if (installId == null) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.JRE_not_specified._3"), null, JavaDebugUI.UNSPECIFIED_VM_INSTALL); //$NON-NLS-1$
		}
		IVMInstall install = type.findVMInstall(installId);
		if (install == null) {
			abort(MessageFormat.format(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.JRE_{0}_does_not_exist._4"), new String[]{installId}), null, JavaDebugUI.VM_INSTALL_DOES_NOT_EXIST); //$NON-NLS-1$
		}		
		IVMRunner runner = install.getVMRunner(mode);
		if (runner == null) {
			abort(MessageFormat.format(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.Internal_error__JRE_{0}_does_not_specify_a_VM_Runner._5"), new String[]{installId}), null, JavaDebugUI.VM_RUNNER_DOES_NOT_EXIST); //$NON-NLS-1$
		}
		
		IProject project = configuration.getProject();
		IJavaProject javaProject = JavaCore.create(project);
		if (project == null) {
			abort(MessageFormat.format(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.Project_{0}_is_not_a_Java_project._6"), new String[]{project.getName()}), null, JavaDebugUI.NOT_A_JAVA_PROJECT); //$NON-NLS-1$
		}
		
		if (!doLaunch) {
			// just verify
			return null;
		}
		
		ExecutionArguments args = new ExecutionArguments(
			configuration.getAttribute(JavaDebugUI.VM_ARGUMENTS_ATTR, ""), //$NON-NLS-1$
			configuration.getAttribute(JavaDebugUI.PROGRAM_ARGUMENTS_ATTR, "")); //$NON-NLS-1$
			
		String[] classpath = JavaRuntime.computeDefaultRuntimeClassPath(javaProject);
		String bootpath = configuration.getAttribute(JavaDebugUI.BOOTPATH_ATTR, null);

		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainType, classpath);
		runConfig.setProgramArguments(args.getProgramArgumentsArray());
		runConfig.setVMArguments(args.getVMArgumentsArray());
		if (bootpath != null) {
			runConfig.setBootClassPath(new String[]{bootpath});
		}
		
		VMRunnerResult result = runner.run(runConfig);
		
		ISourceLocator sourceLocator = new JavaUISourceLocator(javaProject);
		Launch launch = new Launch(configuration, mode, sourceLocator, result.getProcesses(), result.getDebugTarget());
		return launch;
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
}

