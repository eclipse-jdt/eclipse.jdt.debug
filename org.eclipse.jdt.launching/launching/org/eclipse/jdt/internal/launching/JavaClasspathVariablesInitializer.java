/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

 
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
	@Override
	public void initialize(String variable) {
		IVMInstall vmInstall= JavaRuntime.getDefaultVMInstall();
		if (vmInstall != null) {
			IPath newPath= null;
			LibraryLocation[] locations= JavaRuntime.getLibraryLocations(vmInstall);
			// look for rt.jar or classes.zip (both may exist, so do exhaustive search)
			LibraryLocation rtjar = null;
			LibraryLocation classeszip = null;
			for (int i = 0; i < locations.length; i++) {
				LibraryLocation location = locations[i];
				String name = location.getSystemLibraryPath().lastSegment();
				if (name.equalsIgnoreCase("rt.jar")) { //$NON-NLS-1$
					rtjar = location;
				} else if (name.equalsIgnoreCase("classes.zip")) { //$NON-NLS-1$
					classeszip = location;
				}
			}
			// rt.jar if present, then classes.zip, else the first library
			LibraryLocation systemLib = rtjar;
			if (systemLib == null) {
				systemLib = classeszip;
			}
			if (systemLib == null && locations.length > 0) {
				systemLib = locations[0];
			}
			if (systemLib != null) {
				if (variable.equals(JavaRuntime.JRELIB_VARIABLE)) {
					newPath= systemLib.getSystemLibraryPath();
				} else if (variable.equals(JavaRuntime.JRESRC_VARIABLE)) {
					newPath= systemLib.getSystemLibrarySourcePath();
				} else if (variable.equals(JavaRuntime.JRESRCROOT_VARIABLE)){
					newPath= systemLib.getPackageRootPath();
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
	}
	
	private void setJREVariable(IPath newPath, String var) throws CoreException {
		JavaCore.setClasspathVariable(var, newPath, getMonitor());
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

}
