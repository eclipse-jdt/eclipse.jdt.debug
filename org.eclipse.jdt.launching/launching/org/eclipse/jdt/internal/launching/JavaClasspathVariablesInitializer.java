/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


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
				try {
					setJREVariable(newPath, variable);
				}
				catch (CoreException e) {
					LaunchingPlugin.log(e);
				}
			}
		}
	}

	private void setJREVariable(IPath newPath, String var) throws CoreException {
		JavaCore.setClasspathVariable(var, newPath, getMonitor());
	}

	protected IProgressMonitor getMonitor() {
		if (fMonitor == null) {
			return new NullProgressMonitor();
		}
		return fMonitor;
	}

}
