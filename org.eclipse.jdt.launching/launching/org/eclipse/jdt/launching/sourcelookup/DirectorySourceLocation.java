package org.eclipse.jdt.launching.sourcelookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;
 
/**
 * Locates source elements in a directory in the local
 * file system. Returns instances of <code>LocalFileStorage</code>.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Note: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IJavaSourceLocation
 * @since 2.0
 */
public class DirectorySourceLocation extends PlatformObject implements IJavaSourceLocation {

	/**
	 * The directory associated with this source location
	 */
	private File fDirectory;
	
	/**
	 * Constructs a new source location that will retrieve source
	 * elements from the given directory.
	 * 
	 * @param directory a directory
	 */
	public DirectorySourceLocation(File directory) {
		setDirectory(directory);
	}
	
	/**
	 * @see IJavaSourceLocation#findSourceElement(String)
	 */
	public Object findSourceElement(String name) throws CoreException {
		// guess at source name if an inner type
		String pathStr= name.replace('.', '/');
		int dotIndex= pathStr.lastIndexOf('/');
		int dollarIndex= pathStr.indexOf('$', dotIndex + 1);
		if (dollarIndex >= 0) {
			pathStr = pathStr.substring(0, dollarIndex);
		}		
		pathStr += ".java"; //$NON-NLS-1$
		try {
			IPath root = new Path(getDirectory().getCanonicalPath());
			root = root.append(new Path(pathStr));
			File file = root.toFile();
			if (file.exists()) {
				return new LocalFileStorage(file);
			}
		} catch (IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
		return null;
	}

	/**
	 * Sets the directory in which source elements will
	 * be searched for.
	 * 
	 * @param directory a directory
	 */
	private void setDirectory(File directory) {
		fDirectory = directory;
	}
	
	/**
	 * Returns the directory associated with this source
	 * location.
	 * 
	 * @return directory
	 */
	public File getDirectory() {
		return fDirectory;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {		
		return object instanceof DirectorySourceLocation &&
			 getDirectory().equals(((DirectorySourceLocation)object).getDirectory());
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getDirectory().hashCode();
	}	
}
