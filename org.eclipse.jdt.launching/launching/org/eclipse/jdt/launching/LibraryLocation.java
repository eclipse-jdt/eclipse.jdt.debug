/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.launching.LaunchingMessages;


/**
 * The location of a library (for example rt.jar).
 * <p>
 * Clients may instantiate this class; it is not intended to be subclassed.
 * </p>
 */
public final class LibraryLocation {
	private IPath fSystemLibrary;
	private IPath fSystemLibrarySource;
	private IPath fPackageRootPath;
	
	/**
	 * Creates a new library location.
	 * 
	 * @param libraryPath	The location of the JAR containing java.lang.Object
	 * 					Must not be <code>null</code>.
	 * @param sourcePath	The location of the zip file containing the sources for <code>library</code>
	 * 					Must not be <code>null</code> (Use Path.EMPTY instead)
	 * @param packageRoot The path inside the <code>source</code> zip file where packages names
	 * 					  begin. If the source for java.lang.Object source is found at 
	 * 					  "src/java/lang/Object.java" in the zip file, the 
	 * 					  packageRoot should be "src"
	 * 					  Must not be <code>null</code>. (Use Path.EMPTY or IPath.ROOT)
	 * @throws	IllegalArgumentException	If the library path is <code>null</code>.
	 */	
	public LibraryLocation(IPath libraryPath, IPath sourcePath, IPath packageRoot) {
		if (libraryPath == null)
			throw new IllegalArgumentException(LaunchingMessages.getString("libraryLocation.assert.libraryNotNull")); //$NON-NLS-1$

		fSystemLibrary= libraryPath;
		fSystemLibrarySource= sourcePath;
		fPackageRootPath= packageRoot;
	}		
		
	/**
	 * Returns the JRE library jar location.
	 * 
	 * @return The JRE library jar location.
	 */
	public IPath getSystemLibraryPath() {
		return fSystemLibrary;
	}
	
	/**
	 * Returns the JRE library source zip location.
	 * 
	 * @return The JRE library source zip location.
	 */
	public IPath getSystemLibrarySourcePath() {
		return fSystemLibrarySource;
	}	
	
	/**
	 * Returns the path to the default package in the sources zip file
	 * 
	 * @return The path to the default package in the sources zip file.
	 */
	public IPath getPackageRootPath() {
		return fPackageRootPath;
	}
	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof LibraryLocation) {
			LibraryLocation lib = (LibraryLocation)obj;
			return getSystemLibraryPath().equals(lib.getSystemLibraryPath()) 
				&& equals(getSystemLibrarySourcePath(), lib.getSystemLibrarySourcePath())
				&& equals(getPackageRootPath(), lib.getPackageRootPath());
		} 
		return false;
	}

	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return getSystemLibraryPath().hashCode();
	}
	
	/**
	 * Returns whether the given paths are equal - either may be <code>null</code>.
	 */
	protected boolean equals(IPath path1, IPath path2) {
		if (path1 == null) {
			return path2 == null;
		}
		if (path2 == null) {
			return false;
		}
		return path1.equals(path2);
	}

}
