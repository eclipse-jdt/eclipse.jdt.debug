package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ISourceLocator;
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
	public void initializeDefaults(ILaunchConfigurationWorkingCopy configuration, Object object) {
		if (object instanceof ICompilationUnit) {
			initializeCompilationUnitDefaults(configuration, (ICompilationUnit)object);
		}
	}
	
	/**
	 * Initialize working copy defaults for an ICompilationUnit
	 */
	protected void initializeCompilationUnitDefaults(ILaunchConfigurationWorkingCopy workingCopy, ICompilationUnit compilationUnit) {
		
		// Java project
		IJavaProject javaProject = compilationUnit.getJavaProject();
		if ((javaProject == null) || !javaProject.exists()) {
			return;
		}
		workingCopy.setAttribute(JavaDebugUI.PROJECT_ATTR, javaProject.getElementName());
		
		// Main type
		String name = "";
		try {
			IType[] types = MainMethodFinder.findTargets(new BusyIndicatorRunnableContext(), new Object[] {compilationUnit});
			if ((types == null) || (types.length < 1)) {
				return;
			}
			name = types[0].getElementName();
			workingCopy.setAttribute(JavaDebugUI.MAIN_TYPE_ATTR, name);
		} catch (InterruptedException ie) {
		} catch (InvocationTargetException ite) {
		}	
		
		// Name
		//workingCopy.rename(name);	
		
		// VM install type
		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
		IVMInstallType vmInstallType = vmInstall.getVMInstallType();
		String vmInstallTypeID = vmInstallType.getId();
		workingCopy.setAttribute(JavaDebugUI.VM_INSTALL_TYPE_ATTR, vmInstallTypeID);

		// VM
		String vmInstallID = vmInstall.getId();
		workingCopy.setAttribute(JavaDebugUI.VM_INSTALL_ATTR, vmInstallID);
		
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
		String installTypeId = configuration.getAttribute(JavaDebugUI.VM_INSTALL_TYPE_ATTR, null);
		if (installTypeId == null) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.JRE_Type_not_specified._2"), null, JavaDebugUI.UNSPECIFIED_VM_INSTALL_TYPE); //$NON-NLS-1$
		}		
		IVMInstallType type = JavaRuntime.getVMInstallType(installTypeId);
		if (type == null) {
			abort(MessageFormat.format(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.VM_Install_type_does_not_exist"), new String[] {installTypeId}), null, JavaDebugUI.VM_INSTALL_TYPE_DOES_NOT_EXIST); //$NON-NLS-1$
		}
		
		// VM
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
		
		// Working directory
		String path = configuration.getAttribute(JavaDebugUI.WORKING_DIRECTORY_ATTR, null);
		if ((path != null) && (path.trim().length() > 0)) {
			File dir = new File(path);
			if (!dir.isDirectory()) {
				abort(MessageFormat.format(DebugUIMessages.getString("JavaApplicationLaunchConfiguration.Working_directory_does_not_exist"), new String[] {path}), null, JavaDebugUI.WORKING_DIRECTORY_DOES_NOT_EXIST); //$NON-NLS-1$
			}
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

		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainType.getFullyQualifiedName(), classpath);
		runConfig.setProgramArguments(args.getProgramArgumentsArray());
		runConfig.setVMArguments(args.getVMArgumentsArray());
		runConfig.setWorkingDirectory(path);
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
	 * Convenience method to get access to the java model.
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

