package org.eclipse.jdt.internal.launching;

/*******************************************************************************
 * Copyright (c) 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

/**
 *  <b>IMPLEMENTATION IN PROGRESS</b>
 * 
 * JRE Container - resolves a classpath container variable to a JRE
 */
public class JREContainer implements IClasspathContainer {

	/**
	 * Corresponding JRE
	 */
	private IVMInstall fVMInstall = null;
	
	/**
	 * Container path used to resolve to this JRE
	 */
	private IPath fPath = null;
	
	/**
	 * Constructs a JRE classpath conatiner on the given VM install
	 * 
	 * @param vm vm install
	 * @param path container path used to resolve this JRE
	 */
	public JREContainer(IVMInstall vm, IPath path) {
		fVMInstall = vm;
		fPath = path;
	}
	
	/**
	 * @see IClasspathContainer#getClasspathEntries()
	 */
	public IClasspathEntry[] getClasspathEntries() {
		LibraryLocation[] libs = JavaRuntime.getLibraryLocations(fVMInstall);
		IClasspathEntry[] entries = new IClasspathEntry[libs.length];
		for (int i = 0; i < libs.length; i++) {
			entries[i] = JavaCore.newLibraryEntry(libs[i].getSystemLibraryPath(), libs[i].getSystemLibrarySourcePath(), libs[i].getPackageRootPath());
		}
		return entries;
	}

	/**
	 * @see IClasspathContainer#getDescription()
	 */
	public String getDescription() {
		StringBuffer desc = new StringBuffer(LaunchingMessages.getString("JREContainer.JRE_System_Library_1")); //$NON-NLS-1$
		desc.append(" ["); //$NON-NLS-1$
		desc.append(fVMInstall.getName());
		desc.append("]"); //$NON-NLS-1$
		return desc.toString();
	}

	/**
	 * @see IClasspathContainer#getKind()
	 */
	public int getKind() {
		return IClasspathContainer.K_DEFAULT_SYSTEM;
	}

	/**
	 * @see IClasspathContainer#getPath()
	 */
	public IPath getPath() {
		return fPath;
	}

}
