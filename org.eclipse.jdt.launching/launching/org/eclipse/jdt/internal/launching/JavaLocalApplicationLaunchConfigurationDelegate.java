package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
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
		
		IJavaProject javaProject = JavaLaunchConfigurationHelper.getJavaProject(configuration);
								
		IVMInstall vm  = verifyVMInstall(configuration);
		
		IVMRunner runner = vm.getVMRunner(mode);
		if (runner == null) {
			abort(MessageFormat.format("Internal error: JRE {0} does not specify a VM Runner.", new String[]{vm.getId()}), null, IJavaLaunchConfigurationConstants.ERR_VM_RUNNER_DOES_NOT_EXIST);
		}
		
		// Launch the configuration
		VMRunnerResult result = runner.run(configuration);
		
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
	
	/**
	 * Returns the VM install specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the VM install specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected IVMInstall getVMInstall(ILaunchConfiguration configuration) throws CoreException {
		String id = getVMInstallId(configuration);
		if (id != null) {
			IVMInstallType type = getVMInstallType(configuration);
			if (type != null) {
				return type.findVMInstall(id);
			}
		}
		return null;
	}

	/**
	 * Returns the VM install identifier specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the VM install identifier specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected String getVMInstallId(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL, (String)null);
	}

	/**
	 * Returns the VM install type specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the VM install type specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected IVMInstallType getVMInstallType(ILaunchConfiguration configuration) throws CoreException {
		String id = getVMInstallTypeId(configuration);
		if (id != null) {
			IVMInstallType type = JavaRuntime.getVMInstallType(id);
			if (type != null) {
				return type;
			}
		}
		return null;
	}

	/**
	 * Returns the VM install type identifier specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the VM install type identifier specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected String getVMInstallTypeId(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
	}

	/**
	 * Verifies a VM install type is specified by the given 
	 * launch configuration, and returns the VM install type.
	 * 
	 * @param configuration launch configuration
	 * @return the VM install type specified by the given 
	 *  launch configuration
	 * @exception CoreException if unable to retrieve the attribute
	 * 	or the attribute is unspecified
	 */	
	protected IVMInstallType verifyVMInstallType(ILaunchConfiguration configuration) throws CoreException {
		String vmInstallTypeId = getVMInstallTypeId(configuration);
		if (vmInstallTypeId == null) {
			abort("JRE type not specified", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_VM_INSTALL_TYPE);
		}		
		IVMInstallType type = getVMInstallType(configuration);
		if (type == null) {
			abort(MessageFormat.format("JRE type {0} does not exist.", new String[] {vmInstallTypeId}), null, IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_TYPE_DOES_NOT_EXIST);
		}	
		return type;	
	}

	/**
	 * Verifies a VM install is specified by the given 
	 * launch configuration, and returns the VM install.
	 * 
	 * @param configuration launch configuration
	 * @return the VM install specified by the given 
	 *  launch configuration
	 * @exception CoreException if unable to retrieve the attribute
	 * 	or the attribute is unspecified
	 */	
	protected IVMInstall verifyVMInstall(ILaunchConfiguration configuration) throws CoreException {
		verifyVMInstallType(configuration);
		String id = getVMInstallId(configuration);
		if (id == null) {
			abort("JRE not specified.", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_VM_INSTALL); 
		}
		IVMInstall vm = getVMInstall(configuration);
		if (vm == null) {
			abort(MessageFormat.format("JRE {0} does not exist.", new String[]{id}), null, IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST);
		}
		return vm;
	}	
}

