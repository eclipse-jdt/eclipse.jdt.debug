package org.eclipse.jdt.launching.sourcelookup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Implementation of storage for a local file
 * (<code>java.io.File</code>).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @see DirectorySourceLocation
 * @see IStorage
 * @since 2.0
 */
public class LocalFileStorage extends PlatformObject implements IStorage {
	
	/**
	 * The file this storage refers to.
	 */ 
	private File fFile;
		
	/**
	 * Constructs and returns storage for the given file.
	 * 
	 * @param file a local file
	 */
	public LocalFileStorage(File file){
		setFile(file);
	}
	
	/**
	 * @see IStorage#getContents()
	 */
	public InputStream getContents() throws CoreException {
	
		try {
			return new FileInputStream(getFile());
		} catch (IOException e){
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
	}
	
	/**
	 * @see IStorage#getFullPath
	 */
	public IPath getFullPath() {
		try {
			return new Path(getFile().getCanonicalPath());
		} catch (IOException e) {
			LaunchingPlugin.log(e);
			return null;
		}
	}
	/**
	 * @see IStorage#getName
	 */
	public String getName() {
		return getFile().getName();
	}
	/**
	 * @see IStorage#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}
	
	/**
	 * Sets the file associated with this storage
	 * 
	 * @param file a local file
	 */
	private void setFile(File file) {
		fFile = file;	
	}
	
	/**
	 * Returns the file asscoiated with this storage
	 * 
	 * @return file
	 */
	public File getFile() {
		return fFile;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {		
		return object instanceof LocalFileStorage &&
			 getFile().equals(((LocalFileStorage)object).getFile());
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getFile().hashCode();
	}	
}


