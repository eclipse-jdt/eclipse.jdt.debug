package org.eclipse.jdt.internal.launching;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

/**
 * Stores the boot path and extension directories associated with a VM.
 */
public class LibraryInfo {

	private String[] fBootpath;
	private String[] fExtensionDirs;
	
	public LibraryInfo(String[] bootpath, String[] extDirs) {
		fBootpath = bootpath;
		fExtensionDirs = extDirs;
	}
	
	/**
	 * Returns a collection of extension directory paths for this VM install.
	 * 
	 * @return a collection of absolute paths
	 */
	public String[] getExtensionDirs() {
		return fExtensionDirs;
	}
	
	/**
	 * Returns a collection of bootpath entries for this VM install.
	 * 
	 * @return a collection of absolute paths
	 */
	public String[] getBootpath() {
		return fBootpath;
	}
}
