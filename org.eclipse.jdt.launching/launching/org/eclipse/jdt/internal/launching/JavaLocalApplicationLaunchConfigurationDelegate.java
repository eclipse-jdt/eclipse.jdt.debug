package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jdt.launching.VMRunnerResult;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;

/**
 * Launch configuration delegate for a local Java application.
 */
public class JavaLocalApplicationLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	/**
	 * Create the helper class that handles deleting configs whose underlying main type gets deleted
	 */
	static {
		new JavaLaunchConfigurationHelper();
	}

	/**
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String)
	 */
	public ILaunch launch(ILaunchConfiguration configuration, String mode) throws CoreException {
		
		// Main type
		String mainTypeName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
		if ((mainTypeName == null) || (mainTypeName.trim().length() < 1)) {
			abort("Main type not specified.", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE);
		}
		
		// Java project
		IJavaProject javaProject = JavaLaunchConfigurationHelper.getJavaProject(configuration);
						
		// VM install type
		String vmInstallTypeId = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
		if (vmInstallTypeId == null) {
			abort("JRE type not specified", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_VM_INSTALL_TYPE);
		}		
		IVMInstallType type = JavaRuntime.getVMInstallType(vmInstallTypeId);
		if (type == null) {
			abort(MessageFormat.format("JRE type {0} does not exist.", new String[] {vmInstallTypeId}), null, IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_TYPE_DOES_NOT_EXIST);
		}
		
		// VM
		String vmInstallId = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL, (String)null);
		if (vmInstallId == null) {
			abort("JRE not specified.", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_VM_INSTALL); 
		}
		IVMInstall install = type.findVMInstall(vmInstallId);
		if (install == null) {
			abort(MessageFormat.format("JRE {0} does not exist.", new String[]{vmInstallId}), null, IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST);
		}		
		IVMRunner runner = install.getVMRunner(mode);
		if (runner == null) {
			abort(MessageFormat.format("Internal error: JRE {0} does not specify a VM Runner.", new String[]{vmInstallId}), null, IJavaLaunchConfigurationConstants.ERR_VM_RUNNER_DOES_NOT_EXIST);
		}
		
		// Working directory
		String workingDir = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
		if ((workingDir != null) && (workingDir.trim().length() > 0)) {
			File dir = new File(workingDir);
			if (!dir.isDirectory()) {
				abort(MessageFormat.format("Working directory does not exist: {0}", new String[] {workingDir}), null, IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
			}
		}
		
		// Program & VM args
		String pgmArgs = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");	
		String vmArgs = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""); 
		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
		
		// Classpath
		List classpathList = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, (List)null);
		String[] classpath;
		if (classpathList == null) {
			classpath = JavaRuntime.computeDefaultRuntimeClassPath(javaProject);
		} else {
			classpath = new String[classpathList.size()];
			classpathList.toArray(classpath);
		}
		
		// Create VM config
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, classpath);
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDir);

		// Bootpath
		List bootpathList = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH, (List)null);
		if (bootpathList != null) {
			String[] bootpath = new String[bootpathList.size()];
			bootpathList.toArray(bootpath);
			runConfig.setBootClassPath(bootpath);
		}
		
		// Launch the configuration
		VMRunnerResult result = runner.run(runConfig);
		
		if (result == null) {
			return null;
		}
		// Create & return Launch
		ISourceLocator sourceLocator = new JavaSourceLocator(javaProject);
		Launch launch = new Launch(configuration, mode, sourceLocator, result.getProcesses(), result.getDebugTarget());
		return launch;
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
	 * @see JavaLocalApplicationLaunchConfigurationHelper#abort(String, Throwable, int)
	 */
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		JavaLaunchConfigurationHelper.abort(message, exception, code);
	}	
}

