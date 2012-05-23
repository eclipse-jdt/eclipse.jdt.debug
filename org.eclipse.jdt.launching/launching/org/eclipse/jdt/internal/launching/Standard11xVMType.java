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


import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.LibraryLocation;

/**
 * A VM install type for VMs the conform to the 1.1.x standard
 * JDK installion layout, and command line options.
 */
public class Standard11xVMType extends StandardVMType {
		
	/**
	 * @see org.eclipse.jdt.internal.launching.StandardVMType#getDefaultSystemLibrary(java.io.File)
	 */
	@Override
	protected IPath getDefaultSystemLibrary(File installLocation) {
		return new Path(installLocation.getPath()).append("lib").append("classes.zip"); //$NON-NLS-2$ //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.launching.AbstractVMInstallType#doCreateVMInstall(java.lang.String)
	 */
	@Override
	protected IVMInstall doCreateVMInstall(String id) {
		return new Standard11xVM(this, id);
	}
	
	/**
	 * @see org.eclipse.jdt.internal.launching.StandardVMType#getDefaultSystemLibrarySource(java.io.File)
	 */
	@Override
	protected IPath getDefaultSystemLibrarySource(File libLocation) {
		setDefaultRootPath(""); //$NON-NLS-1$
		return Path.EMPTY;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#getName()
	 */
	@Override
	public String getName() {
		return LaunchingMessages.Standard11xVMType_Standard_1_1_x_VM_1; 
	}	
	
	/**
	 * Returns <code>null</code> - not supported.
	 * 
	 * @see StandardVMType#getDefaultExtensionDirectory(File)
	 */
	@Override
	protected File getDefaultExtensionDirectory(File installLocation) {
		return null;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.launching.StandardVMType#getDefaultEndorsedDirectory(java.io.File)
	 */
	@Override
	protected File getDefaultEndorsedDirectory(File installLocation) {
		return null;
	}

	/**
	 * @see org.eclipse.jdt.launching.IVMInstallType#getDefaultLibraryLocations(java.io.File)
	 */
	@Override
	public LibraryLocation[] getDefaultLibraryLocations(File installLocation) {
		IPath libPath = getDefaultSystemLibrary(installLocation);
		File lib = libPath.toFile();
		if (lib.exists()) {
			return new LibraryLocation[] {new LibraryLocation(libPath, getDefaultSystemLibrarySource(lib), getDefaultPackageRootPath())};
		}
		return new LibraryLocation[0];
	}

	/**
	 * Return <code>true</code> if the appropriate system libraries can be found for the
	 * specified java executable, <code>false</code> otherwise.
	 */
	@Override
	protected boolean canDetectDefaultSystemLibraries(File javaHome, File javaExecutable) {
		LibraryLocation[] locations = getDefaultLibraryLocations(javaHome);
		String version = getVMVersion(javaHome, javaExecutable);
		return locations.length > 0 && version.startsWith("1.1"); //$NON-NLS-1$
	}
}
