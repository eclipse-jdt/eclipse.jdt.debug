package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
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
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, IProgressMonitor)
	 */
	public ILaunch launch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
								
		String mainTypeName = verifyMainTypeName(configuration);

		IVMInstall vm = verifyVMInstall(configuration);

		IVMRunner runner = vm.getVMRunner(mode);
		if (runner == null) {
			abort(MessageFormat.format("Internal error: JRE {0} does not specify a VM Runner.", new String[]{vm.getId()}), null, IJavaLaunchConfigurationConstants.ERR_VM_RUNNER_DOES_NOT_EXIST);
		}

		File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null) {
			workingDirName = workingDir.getAbsolutePath();
		}
		
		// Program & VM args
		String pgmArgs = getProgramArguments(configuration);
		String vmArgs = getVMArguments(configuration);
		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
		
		// Classpath
		String[] classpath = getClasspath(configuration);
		
		// Create VM config
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, classpath);
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirName);

		// Bootpath
		String[] bootpath = getBootpath(configuration);
		runConfig.setBootClassPath(bootpath);
		
		// Launch the configuration
		VMRunnerResult result = runner.run(runConfig, monitor);		
		if (result == null) {
			return null;
		}
		
		// Create & return Launch:
		//  - set default source locator if none specified
		ISourceLocator sourceLocator = null;
		String id = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
		if (id == null) {
			IJavaProject javaProject = JavaLaunchConfigurationHelper.getJavaProject(configuration);
			sourceLocator = new JavaSourceLocator(javaProject);
		}
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
	 * launch configuration, that its home location
	 * is specified and exists, and returns the VM install.
	 * 
	 * @param configuration launch configuration
	 * @return the VM install specified by the given 
	 *  launch configuration
	 * @exception CoreException if unable to retrieve the attribute,
	 * 	the attribute is unspecified, or if the home location is
	 *  unspecified or does not exist
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
		File location = vm.getInstallLocation();
		if (location == null) {
			abort(MessageFormat.format("JRE home directory not specified for {0}.", new String[]{vm.getName()}), null, IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST);
		}
		if (!location.exists()) {
			abort(MessageFormat.format("JRE home directory for {0} does not exist: {1}", new String[]{vm.getName(), location.getAbsolutePath()}), null, IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST);
		}
				
		return vm;
	}	
	
	/**
	 * Returns the bootpath specified by the given launch
	 * configuration, as an array of Strings. The returned array
	 * is empty if no bootpath is specified.
	 * 
	 * @param configuration launch configuration
	 * @return the bootpath specified by the given 
	 *  launch configuration, possibly an empty array
	 * @exception CoreException if unable to retrieve the attribute
	 */	
	protected String[] getBootpath(ILaunchConfiguration configuration) throws CoreException {
		List bootpathList = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH, (List)null);
		String[] bootpath = null;
		if (bootpathList != null) {
			bootpath = new String[bootpathList.size()];
			bootpathList.toArray(bootpath);
		} else {
			bootpath = new String[0];
		}
		return bootpath;
	}

	/**
	 * Returns the classpath specified by the given launch
	 * configuration, as an array of Strings. The returned array
	 * is empty if no classpath is specified.
	 * 
	 * @param configuration launch configuration
	 * @return the classpath specified by the given 
	 *  launch configuration, possibly an empty array
	 * @exception CoreException if unable to retrieve the attribute
	 */	
	protected String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {
		List classpathList = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, (List)null);
		String[] classpath;
		if (classpathList == null) {
			IJavaProject project = getJavaProject(configuration);
			if (project == null) {
				classpath = new String[0];
			} else {
				classpath = JavaRuntime.computeDefaultRuntimeClassPath(project);
			}
		} else {
			classpath = new String[classpathList.size()];
			classpathList.toArray(classpath);
		}
		return classpath;
	}

	/**
	 * Returns the Java project specified by the given 
	 * launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the Java project specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected IJavaProject getJavaProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = getJavaProjectName(configuration);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				IJavaProject javaProject = JavaCore.create(project);
				if (javaProject != null && javaProject.exists()) {
					return javaProject;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the Java project name specified by the given 
	 * launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the Java project name specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected String getJavaProjectName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
	}

	/**
	 * Returns the main type name specified by the given 
	 * launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the main type name specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected String getMainTypeName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
	}

	/**
	 * Returns the program arguments specified by the given launch
	 * configuration, as a string. The returned string is empty if
	 * no program arguments are specified.
	 * 
	 * @param configuration launch configuration
	 * @return the program arguments specified by the given 
	 *  launch configuration, possibly an empty string
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
	}	

	/**
	 * Returns the VM arguments specified by the given launch
	 * configuration, as a string. The returned string is empty if
	 * no VM arguments are specified.
	 * 
	 * @param configuration launch configuration
	 * @return the VM arguments specified by the given 
	 *  launch configuration, possibly an empty string
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
	}	

	/**
	 * Returns the working directory specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the working directory specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected File getWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
		String path = getWorkingDirectoryPath(configuration);
		if (path != null) {
			File dir = new File(path);
			if (dir.isDirectory()) {
				return dir;
			}
		}
		return null;
	}

	/**
	 * Returns the working directory path specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the working directory path specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected String getWorkingDirectoryPath(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
	}

	/**
	 * Verifies a Java project is specified by the given 
	 * launch configuration, and returns the Java project.
	 * 
	 * @param configuration launch configuration
	 * @return the Java project specified by the given 
	 *  launch configuration
	 * @exception CoreException if unable to retrieve the attribute
	 * 	or the attribute is unspecified
	 */
	protected IJavaProject verifyJavaProject(ILaunchConfiguration configuration) throws CoreException {
		String name = getJavaProjectName(configuration);
		if (name == null) {
			abort("Java project not specified.", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_PROJECT);
		}
		IJavaProject project = getJavaProject(configuration);
		if (project == null) {
			abort("Project does not exist, or is not a Java project.", null, IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);
		}
		return project;
	}

	/**
	 * Verifies a main type name is specified by the given 
	 * launch configuration, and returns the main type name.
	 * 
	 * @param configuration launch configuration
	 * @return the main type name specified by the given 
	 *  launch configuration
	 * @exception CoreException if unable to retrieve the attribute
	 * 	or the attribute is unspecified
	 */
	protected String verifyMainTypeName(ILaunchConfiguration configuration) throws CoreException {
		String name = getMainTypeName(configuration);
		if (name == null) {
			abort("Main type not specified.", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE);
		}
		return name;
	}

	/**
	 * Verifies the working directory specified by the given 
	 * launch configuration exists, and returns the working
	 * directory, or <code>null</code> if none is specified.
	 * 
	 * @param configuration launch configuration
	 * @return the working directory specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */	
	protected File verifyWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
		String path = getWorkingDirectoryPath(configuration);
		if (path != null) {
			File dir = new File(path);
			if (!dir.isDirectory()) {
				abort(MessageFormat.format("Working directory does not exist: {0}", new String[] {path}), null, IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
			}
			return dir;
		}
		return null;
	}	
		
}

