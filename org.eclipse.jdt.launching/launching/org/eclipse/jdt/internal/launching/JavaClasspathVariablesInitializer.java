package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

public class JavaClasspathVariablesInitializer extends ClasspathVariableInitializer {

	/**
	 * The monitor to use for progress reporting.
	 * May be null
	 */
	private IProgressMonitor fMonitor;
	
	/**
	 * @see ClasspathVariableInitializer#initialize(String)
	 */
	public void initialize(String variable) {
		IVMInstall vmInstall= JavaRuntime.getDefaultVMInstall();
		if (vmInstall != null) {
			IPath newPath= null;
			LibraryLocation desc= JavaRuntime.getLibraryLocation(vmInstall);
			if (variable.equals(JavaRuntime.JRELIB_VARIABLE)) {
				newPath= desc.getSystemLibraryPath();
			} else if (variable.equals(JavaRuntime.JRESRC_VARIABLE)) {
				newPath= desc.getSystemLibrarySourcePath();
			} else if (variable.equals(JavaRuntime.JRESRCROOT_VARIABLE)){
				newPath= desc.getPackageRootPath();
			}
			if (newPath == null) {
				return;
			}
			IWorkspace workspace= ResourcesPlugin.getWorkspace();
			IWorkspaceDescription wsDescription= workspace.getDescription();				
			boolean wasAutobuild= wsDescription.isAutoBuilding();
			try {
				setAutobuild(workspace, false);
				setJREVariable(newPath, variable);	
			} catch (CoreException ce) {
				LaunchingPlugin.log(ce);
				return;
			} finally {
				try {
					setAutobuild(workspace, wasAutobuild);
				} catch (CoreException ce) {
					LaunchingPlugin.log(ce);
				}
			}
		}
	}
	
	public void updateJREVariables(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		monitor.beginTask(LaunchingMessages.getString("JavaRuntime.Setting_JRE_classpath_variables"), 3); //$NON-NLS-1$
		IVMInstall vmInstall= JavaRuntime.getDefaultVMInstall();
		if (vmInstall != null) {
			IWorkspace workspace= ResourcesPlugin.getWorkspace();
			boolean wasAutobuild= setAutobuild(workspace, false);
			try {
				LibraryLocation desc= JavaRuntime.getLibraryLocation(vmInstall);
				IPath library= desc.getSystemLibraryPath();
				setMonitor(new SubProgressMonitor(monitor, 1));
				setJREVariable(library, JavaRuntime.JRELIB_VARIABLE);
				IPath source= desc.getSystemLibrarySourcePath();
				setMonitor(new SubProgressMonitor(monitor, 1));
				setJREVariable(source, JavaRuntime.JRESRC_VARIABLE);
				IPath pkgRoot= desc.getPackageRootPath();
				setMonitor(new SubProgressMonitor(monitor, 1));		
				setJREVariable(pkgRoot, JavaRuntime.JRESRCROOT_VARIABLE);
			} finally {
				setAutobuild(workspace, wasAutobuild);
			}
		}
	}
	
	private void setJREVariable(IPath newPath, String var) throws CoreException {
		IPath oldPath= JavaCore.getClasspathVariable(var);
		if (!newPath.equals(oldPath)) {
			JavaCore.setClasspathVariable(var, newPath, getMonitor());
		}
	}
	
	private boolean setAutobuild(IWorkspace ws, boolean newState) throws CoreException {
		IWorkspaceDescription wsDescription= ws.getDescription();
		boolean oldState= wsDescription.isAutoBuilding();
		if (oldState != newState) {
			wsDescription.setAutoBuilding(newState);
			ws.setDescription(wsDescription);
		}
		return oldState;
	}
	
	protected IProgressMonitor getMonitor() {
		if (fMonitor == null) {
			return new NullProgressMonitor();
		}
		return fMonitor;
	}

	protected void setMonitor(IProgressMonitor monitor) {
		fMonitor = monitor;
	}
}
