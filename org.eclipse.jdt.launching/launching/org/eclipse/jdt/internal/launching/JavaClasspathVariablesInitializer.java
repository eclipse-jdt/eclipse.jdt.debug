package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
			LibraryLocation[] locations= JavaRuntime.getLibraryLocations(vmInstall);
			if (variable.equals(JavaRuntime.JRELIB_VARIABLE)) {
				newPath= locations[0].getSystemLibraryPath();
			} else if (variable.equals(JavaRuntime.JRESRC_VARIABLE)) {
				newPath= locations[0].getSystemLibrarySourcePath();
			} else if (variable.equals(JavaRuntime.JRESRCROOT_VARIABLE)){
				newPath= locations[0].getPackageRootPath();
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
		IVMInstall vmInstall= JavaRuntime.getDefaultVMInstall();
		if (vmInstall != null) {
			IWorkspace workspace= ResourcesPlugin.getWorkspace();
			boolean wasAutobuild= setAutobuild(workspace, false);
			try {
				List changedVars= new ArrayList(3);
				List changedPaths= new ArrayList(3);
				LibraryLocation[] locations= JavaRuntime.getLibraryLocations(vmInstall);
				IPath library= locations[0].getSystemLibraryPath();
				if (changedJREVariable(library, JavaRuntime.JRELIB_VARIABLE)) {
					changedVars.add(JavaRuntime.JRELIB_VARIABLE);
					changedPaths.add(library);
				}
				IPath source= locations[0].getSystemLibrarySourcePath();
				if (changedJREVariable(source, JavaRuntime.JRESRC_VARIABLE)) {
					changedVars.add(JavaRuntime.JRESRC_VARIABLE);
					changedPaths.add(source);
				}
				IPath pkgRoot= locations[0].getPackageRootPath();
				if (changedJREVariable(pkgRoot, JavaRuntime.JRESRCROOT_VARIABLE)) {
					changedVars.add(JavaRuntime.JRESRCROOT_VARIABLE);
					changedPaths.add(pkgRoot);
				}
				JavaCore.setClasspathVariables((String[])changedVars.toArray(new String[changedVars.size()]), (IPath[])changedPaths.toArray(new IPath[changedPaths.size()]), monitor);
			} finally {
				setAutobuild(workspace, wasAutobuild);
			}
		}
	}
	
	
	private boolean changedJREVariable(IPath newPath, String var) throws CoreException {
		IPath oldPath= JavaCore.getClasspathVariable(var);
		return !newPath.equals(oldPath);
	}
	
	private void setJREVariable(IPath newPath, String var) throws CoreException {
		if (changedJREVariable(newPath, var)) {
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
