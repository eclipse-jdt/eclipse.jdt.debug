/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */package org.eclipse.jdt.launching;import java.io.File;import org.eclipse.core.runtime.IPath;import org.eclipse.core.runtime.Path;import org.eclipse.jdt.internal.launching.LaunchingMessages;


/** * Holder class for the configuration of the JRE library (for example rt.jar). * <p> * Clients may instantiate this class; it is not intended to be subclassed. * </p> */public final class LibraryLocation {
	private IPath fSystemLibrary;
	private IPath fSystemLibrarySource;
	private IPath fPackageRootPath;
		/**	 * @deprecated Use LibraryLocation(IPath, IPath, IPath) instead	 */
	public LibraryLocation(File library, File source, IPath packageRoot) {		if (library == null)			throw new IllegalArgumentException(LaunchingMessages.getString("libraryLocation.assert.libraryNotNull")); //$NON-NLS-1$
		fSystemLibrary= new Path(library.getPath());		if (source != null) {			fSystemLibrarySource= new Path(source.getPath());		} else {			fSystemLibrarySource= Path.EMPTY;		}		if (packageRoot != null) {			fPackageRootPath= packageRoot;		} else {			fPackageRootPath= Path.EMPTY;		}			}		/**	 * Creates a new LibraryLocation	 * @param libraryPath	The location of the JAR containing java.lang.Object	 * 					Must not be <code>null</code>.	 * @param sourcePath	The location of the zip file containing the sources for <code>library</code>	 * 					Must not be <code>null</code> (Use Path.EMPTY instead)	 * @param packageRoot The path inside the <code>source</code> zip file where packages names	 * 					  begin. If the source for java.lang.Object source is found at 	 * 					  "src/java/lang/Object.java" in the zip file, the 	 * 					  packageRoot should be "src"	 * 					  Must not be <code>null</code>. (Use Path.EMPTY instead)	 * @throws	IllegalArgumentException	If the library path is <code>null</code>.	 */		public LibraryLocation(IPath libraryPath, IPath sourcePath, IPath packageRoot) {		if (libraryPath == null)			throw new IllegalArgumentException(LaunchingMessages.getString("libraryLocation.assert.libraryNotNull")); //$NON-NLS-1$		fSystemLibrary= libraryPath;		fSystemLibrarySource= sourcePath;		fPackageRootPath= packageRoot;	}		
		/**	 * @return The JRE library jar location.	 * @deprecated Use getSystemLibraryPath instead	 */
	public File getSystemLibrary() {
		return fSystemLibrary.toFile();
	}
	
	/**	 * @return The JRE library source zip location.	 * @deprecated Use getSystemLibrarySourcePath instead 	 */	public File getSystemLibrarySource() {
		return fSystemLibrarySource.toFile();
	}		/**	 * @return The JRE library jar location.	 */	public IPath getSystemLibraryPath() {		return fSystemLibrary;	}		/**	 * @return The JRE library source zip location.	 */	public IPath getSystemLibrarySourcePath() {		return fSystemLibrarySource;	}	
	
	/**	 * @return The path to the default package in the sources zip file.	 */	public IPath getPackageRootPath() {
		return fPackageRootPath;
	}
}
