package org.eclipse.jdt.launching.sourcelookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
 
/**
 * Locates source elements in an acrhive (zip) in the local
 * file system. Returns instances of <code>ZipEntryStorage</code>.
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
public class ArchiveSourceLocation extends PlatformObject implements IJavaSourceLocation {
	
	/**
	 * Cache of shared zip files. Zip files are closed
	 * when the launching plug-in is shutdown.
	 */
	private static HashMap fZipFileCache = new HashMap(5);

	/**
	 * Returns a zip file with the given name
	 * 
	 * @param name zip file name
	 * @exception IOException if unable to create the specified zip
	 * 	file
	 */
	private static ZipFile getZipFile(String name) throws IOException {
		ZipFile zip = (ZipFile)fZipFileCache.get(name);
		if (zip == null) {
			zip = new ZipFile(name);
			fZipFileCache.put(name, zip);
		}
		return zip;
	}
	
	/**
	 * Closes all zip files that have been opened,
	 * and removes them from the zip file cache.
	 * This method is only to be called by the launching
	 * plug-in on shutdown.
	 */
	public static void shutdown() {
		Iterator iter = fZipFileCache.values().iterator();
		while (iter.hasNext()) {
			ZipFile file = (ZipFile)iter.next();
			try {
				file.close();
			} catch (IOException e) {
				LaunchingPlugin.log(e);
			}
		}
		fZipFileCache.clear();
	}
	
	/**
	 * The archive associated with this source location
	 */
	private ZipFile fArchive;
	
	/**
	 * The root source folder in the archive
	 */
	private IPath fRootPath;
	
	/**
	 * Constructs a new source location that will retrieve source
	 * elements from the zip file with the given name.
	 * 
	 * @param archive zip file
	 * @param sourceRoot a path to the root source folder in the
	 *  specified archive, or <code>null</code> if the root source folder
	 *  is the root of the archive
	 * @exception IOException if unable to access the zip file
	 */
	public ArchiveSourceLocation(String archiveName, String sourceRoot) throws IOException {
		super();
		ZipFile zip = getZipFile(archiveName);
		setArchive(zip);
		setRootPath(sourceRoot);
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
		IPath path = new Path(pathStr); 
		if (getRootPath() != null) {
			path = getRootPath().append(path);
		}
		ZipEntry entry = getArchive().getEntry(path.toString());
		if (entry != null) {
			return new ZipEntryStorage(getArchive(), entry);
		}
		return null;
	}

	/**
	 * Sets the archive in which source elements will
	 * be searched for.
	 * 
	 * @param archive a zip file
	 */
	private void setArchive(ZipFile archive) {
		fArchive = archive;
	}
	
	/**
	 * Returns the archive associated with this source
	 * location.
	 * 
	 * @return zip file
	 */
	public ZipFile getArchive() {
		return fArchive;
	}
	
	/**
	 * Sets the location of the root source folder within
	 * the archive, or <code>null</code> if the root source
	 * folder is the root of the arhcive
	 * 
	 * @param path the location of the root source folder within
	 * the archive, or <code>null</code> if the root source
	 * folder is the root of the arhcive
	 */
	public void setRootPath(String path) {
		if (path == null || path.trim().length() == 0) {
			fRootPath = null;
		} else {
			fRootPath = new Path(path);
		}
	}
	
	/**
	 * Returns the location of the root source folder within
	 * the archive, or <code>null</code> if the root source
	 * folder is the root of the arhcive
	 * 
	 * @return the location of the root source folder within
	 * the archive, or <code>null</code> if the root source
	 * folder is the root of the arhcive
	 */
	public IPath getRootPath() {
		return fRootPath;
	}	
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {		
		return object instanceof ArchiveSourceLocation &&
			 getArchive().equals(((ArchiveSourceLocation)object).getArchive());
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getArchive().hashCode();
	}	
}
