/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.osgi.service.environment.Constants;

/**
 * A VM install type for VMs the conform to the standard
 * JDK installion layout.
 */
public class StandardVMType extends AbstractVMInstallType {
	
	/**
	 * The root path for the attached src
	 */
	private String fDefaultRootPath;
	
	/**
	 * Map of the install path for which we were unable to generate
	 * the library info during this session.
	 */
	private static Map fgFailedInstallPath= new HashMap();
		
	/**
	 * Convenience handle to the system-specific file separator character
	 */															
	private static final char fgSeparator = File.separatorChar;

	/**
	 * The list of locations in which to look for the java executable in candidate
	 * VM install locations, relative to the VM install location.
	 */
	private static final String[] fgCandidateJavaLocations = {
							"bin" + fgSeparator + "javaw",                                //$NON-NLS-2$ //$NON-NLS-1$
							"bin" + fgSeparator + "javaw.exe",                            //$NON-NLS-2$ //$NON-NLS-1$
							"jre" + fgSeparator + "bin" + fgSeparator + "javaw",          //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
							"jre" + fgSeparator + "bin" + fgSeparator + "javaw.exe",      //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$									
							"bin" + fgSeparator + "java",                                 //$NON-NLS-2$ //$NON-NLS-1$
							"bin" + fgSeparator + "java.exe",                             //$NON-NLS-2$ //$NON-NLS-1$
							"jre" + fgSeparator + "bin" + fgSeparator + "java",           //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
							"jre" + fgSeparator + "bin" + fgSeparator + "java.exe"};      //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$							
	
	/**
	 * Starting in the specified VM install location, attempt to find the 'java' executable
	 * file.  If found, return the corresponding <code>File</code> object, otherwise return
	 * <code>null</code>.
	 */
	public static File findJavaExecutable(File vmInstallLocation) {
		
		// Try each candidate in order.  The first one found wins.  Thus, the order
		// of fgCandidateJavaLocations is significant.
		for (int i = 0; i < fgCandidateJavaLocations.length; i++) {
			File javaFile = new File(vmInstallLocation, fgCandidateJavaLocations[i]);
			if (javaFile.isFile()) {
				return javaFile;
			}
		}		
		return null;							
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#getName()
	 */
	public String getName() {
		return LaunchingMessages.StandardVMType_Standard_VM_3; //$NON-NLS-1$
	}

	
	protected IVMInstall doCreateVMInstall(String id) {
		return new StandardVM(this, id);
	}
	
	/**
	 * Return library information corresponding to the specified install
	 * location. If the info does not exist, create it using the given Java
	 * executable.
	 */
	protected synchronized LibraryInfo getLibraryInfo(File javaHome, File javaExecutable) {
		
		// See if we already know the info for the requested VM.  If not, generate it.
		String installPath = javaHome.getAbsolutePath();
		LibraryInfo info = LaunchingPlugin.getLibraryInfo(installPath);
		if (info == null) {
			info= (LibraryInfo)fgFailedInstallPath.get(installPath);
			if (info == null) {
				info = generateLibraryInfo(javaHome, javaExecutable);
				if (info == null) {
					info = getDefaultLibraryInfo(javaHome);
					fgFailedInstallPath.put(installPath, info);
				} else {
				    // only persist if we were able to generate info - see bug 70011
				    LaunchingPlugin.setLibraryInfo(installPath, info);
				}
			}
		} 
		return info;
	}	
	
	/**
	 * Return <code>true</code> if the appropriate system libraries can be found for the
	 * specified java executable, <code>false</code> otherwise.
	 */
	protected boolean canDetectDefaultSystemLibraries(File javaHome, File javaExecutable) {
		LibraryLocation[] locations = getDefaultLibraryLocations(javaHome);
		String version = getVMVersion(javaHome, javaExecutable);
		return locations.length > 0 && !version.startsWith("1.1"); //$NON-NLS-1$
	}
	
	/**
	 * Returns the version of the VM at the given location, with the given
	 * executable.
	 * 
	 * @param javaHome
	 * @param javaExecutable
	 * @return String
	 */
	protected String getVMVersion(File javaHome, File javaExecutable) {
		LibraryInfo info = getLibraryInfo(javaHome, javaExecutable);
		return info.getVersion();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#detectInstallLocation()
	 */
	public File detectInstallLocation() {
		// do not detect on the Mac OS
		if (Platform.getOS().equals(Constants.OS_MACOSX)) {
			return null;
		}		
		
		// Retrieve the 'java.home' system property.  If that directory doesn't exist, 
		// return null.
		File javaHome; //$NON-NLS-1$
		try {
			javaHome= new File (System.getProperty("java.home")).getCanonicalFile(); //$NON-NLS-1$
		} catch (IOException e) {
			LaunchingPlugin.log(e);
			return null;
		}
		if (!javaHome.exists()) {
			return null;
		}

		// Find the 'java' executable file under the java home directory.  If it can't be
		// found, return null.
		File javaExecutable = findJavaExecutable(javaHome);
		if (javaExecutable == null) {
			return null;
		}
		
		// If the reported java home directory terminates with 'jre', first see if 
		// the parent directory contains the required libraries
		boolean foundLibraries = false;
		if (javaHome.getName().equalsIgnoreCase("jre")) { //$NON-NLS-1$
			File parent= new File(javaHome.getParent());			
			if (canDetectDefaultSystemLibraries(parent, javaExecutable)) {
				javaHome = parent;
				foundLibraries = true;
			}
		}	
		
		// If we haven't already found the libraries, look in the reported java home dir
		if (!foundLibraries) {
			if (!canDetectDefaultSystemLibraries(javaHome, javaExecutable)) {
				return null;
			}			
		}
		
		return javaHome;
	}

	/**
	 * Return an <code>IPath</code> corresponding to the single library file containing the
	 * standard Java classes for most VMs version 1.2 and above.
	 */
	protected IPath getDefaultSystemLibrary(File javaHome) {
		IPath jreLibPath= new Path(javaHome.getPath()).append("lib").append("rt.jar"); //$NON-NLS-2$ //$NON-NLS-1$
		if (jreLibPath.toFile().isFile()) {
			return jreLibPath;
		}
		return new Path(javaHome.getPath()).append("jre").append("lib").append("rt.jar"); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
	}
	
	/**
	 * Returns a path to the source attachment for the given libaray, or
	 * an empty path if none.
	 * 
	 * @param libLocation
	 * @return a path to the source attachment for the given library, or
	 *  an empty path if none
	 */
	protected IPath getDefaultSystemLibrarySource(File libLocation) {
		File parent= libLocation.getParentFile();
		while (parent != null) {
			File parentsrc= new File(parent, "src.jar"); //$NON-NLS-1$
			if (parentsrc.isFile()) {
				setDefaultRootPath("src");//$NON-NLS-1$
				return new Path(parentsrc.getPath());
			}
			parentsrc= new File(parent, "src.zip"); //$NON-NLS-1$
			if (parentsrc.isFile()) {
				setDefaultRootPath(""); //$NON-NLS-1$
				return new Path(parentsrc.getPath());
			}
			parent = parent.getParentFile();
		}
		setDefaultRootPath(""); //$NON-NLS-1$
		return Path.EMPTY; //$NON-NLS-1$
	}

	protected IPath getDefaultPackageRootPath() {
		return new Path(getDefaultRootPath());
	}

	/**
	 * NOTE: We do not add libraries from the "endorsed" directory explicitly, as
	 * the bootpath contains these entries already (if they exist).
	 * 
	 * @see org.eclipse.jdt.launching.IVMInstallType#getDefaultLibraryLocations(File)
	 */
	public LibraryLocation[] getDefaultLibraryLocations(File installLocation) {

		// Determine the java executable that corresponds to the specified install location
		// and use this to generate library info.  If no java executable was found, 
		// the 'standard' libraries will be returned.
		File javaExecutable = findJavaExecutable(installLocation);
		LibraryInfo libInfo;
		if (javaExecutable == null) {
			libInfo = getDefaultLibraryInfo(installLocation);
		} else {
			libInfo = getLibraryInfo(installLocation, javaExecutable);
		}
				
		String[] bootpath = libInfo.getBootpath();
		
		List extensions = gatherAllLibraries(libInfo.getExtensionDirs(), installLocation);
		List allLibs = new ArrayList(bootpath.length + extensions.size());
		
		URL url = getDefaultJavadocLocation(installLocation);
		for (int i = 0; i < bootpath.length; i++) {
			IPath path = new Path(bootpath[i]);
			File lib = path.toFile(); 
			if (lib.exists() && lib.isFile()) {
				allLibs.add(new LibraryLocation(path,
								getDefaultSystemLibrarySource(lib),
								getDefaultPackageRootPath(),
								url));
			}
			
		}
				
		// Add all extension directories
		Iterator iter = extensions.iterator();
		while (iter.hasNext()) {
			LibraryLocation lib = (LibraryLocation)iter.next();
			// check for dups, in case bootpath contains an ext dir entry (see bug 50201)
			if (!isDuplicateLibrary(allLibs, lib)) {
				allLibs.add(lib);
			}
		}
				
		return (LibraryLocation[])allLibs.toArray(new LibraryLocation[allLibs.size()]);
	}
	
	/**
	 * Returns whether the given library is already contained in the given list.
	 * Rather than checking the library for equality (which considers source attachments),
	 * we check the actual OS path to the library for equality.
	 * 
	 * @param libs list of library locations
	 * @param dup possible dup
	 * @return whether dup is contained in list of libraries
	 */
	private boolean isDuplicateLibrary(List libs, LibraryLocation dup) {
		String osPath = dup.getSystemLibraryPath().toOSString();
		for (int i = 0; i < libs.size(); i++) {
			LibraryLocation location = (LibraryLocation) libs.get(i);
			if (location.getSystemLibraryPath().toOSString().equalsIgnoreCase(osPath)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns default library info for the given install location.
	 * 
	 * @param installLocation
	 * @return LibraryInfo
	 */
	protected LibraryInfo getDefaultLibraryInfo(File installLocation) {
		IPath rtjar = getDefaultSystemLibrary(installLocation);
		File extDir = getDefaultExtensionDirectory(installLocation);
		File endDir = getDefaultEndorsedDirectory(installLocation);
		String[] dirs = null;
		if (extDir == null) {
			dirs = new String[0];
		} else {
			dirs = new String[] {extDir.getAbsolutePath()};
		}
		String[] endDirs = null;
		if (endDir == null) {
			endDirs = new String[0]; 
		} else {
			endDirs = new String[] {endDir.getAbsolutePath()};
		}
		return new LibraryInfo("???", new String[] {rtjar.toOSString()}, dirs, endDirs);		 //$NON-NLS-1$
	}
	
	/**
	 * Returns a list of all zips and jars contained in the given directories.
	 * 
	 * @param dirPaths a list of absolute paths of directories to search
	 * @return List of all zips and jars
	 */
	protected List gatherAllLibraries(String[] dirPaths, File installLocation) {
		List libraries = new ArrayList();
		URL defaultJavadocLocation = getDefaultJavadocLocation(installLocation);
		for (int i = 0; i < dirPaths.length; i++) {
			File extDir = new File(dirPaths[i]);
			if (extDir != null && extDir.exists() && extDir.isDirectory()) {
				String[] names = extDir.list();
				for (int j = 0; j < names.length; j++) {
					String name = names[j];
					File jar = new File(extDir, name);
					if (jar.isFile()) {
						int length = name.length();
						if (length > 4) {
							String suffix = name.substring(length - 4);
							if (suffix.equalsIgnoreCase(".zip") || suffix.equalsIgnoreCase(".jar")) { //$NON-NLS-1$ //$NON-NLS-2$
								try {
									IPath libPath = new Path(jar.getCanonicalPath());
									LibraryLocation library = new LibraryLocation(libPath, Path.EMPTY, Path.EMPTY, defaultJavadocLocation);
									libraries.add(library);
								} catch (IOException e) {
									LaunchingPlugin.log(e);
								}
							}
						}
					}
				}
			}			
		}
		return libraries;
	}
		
	/**
	 * Returns the default location of the extension directory, based on the given
	 * install location. The resulting file may not exist, or be <code>null</code>
	 * if an extension directory is not supported.
	 * 
	 * @param installLocation 
	 * @return default extension directory or <code>null</code>
	 */
	protected File getDefaultExtensionDirectory(File installLocation) {
		File jre = null;
		if (installLocation.getName().equalsIgnoreCase("jre")) { //$NON-NLS-1$
			jre = installLocation;
		} else {
			jre = new File(installLocation, "jre"); //$NON-NLS-1$
		}
		File lib = new File(jre, "lib"); //$NON-NLS-1$
		File ext = new File(lib, "ext"); //$NON-NLS-1$
		return ext;
	}

	/**
	 * Returns the default location of the endorsed directory, based on the
	 * given install location. The resulting file may not exist, or be
	 * <code>null</code> if an endorsed directory is not supported.
	 * 
	 * @param installLocation 
	 * @return default endorsed directory or <code>null</code>
	 */
	protected File getDefaultEndorsedDirectory(File installLocation) {
		File lib = new File(installLocation, "lib"); //$NON-NLS-1$
		File ext = new File(lib, "endorsed"); //$NON-NLS-1$
		return ext;
	}

	protected String getDefaultRootPath() {
		return fDefaultRootPath;
	}

	protected void setDefaultRootPath(String defaultRootPath) {
		fDefaultRootPath = defaultRootPath;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#validateInstallLocation(java.io.File)
	 */
	public IStatus validateInstallLocation(File javaHome) {
		IStatus status = null;
		if (Platform.getOS().equals(Constants.OS_MACOSX)) {
			status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0, LaunchingMessages.StandardVMType_Standard_VM_not_supported_on_MacOS__1, null); //$NON-NLS-1$
		} else {
			File javaExecutable = findJavaExecutable(javaHome);
			if (javaExecutable == null) {
				status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0, LaunchingMessages.StandardVMType_Not_a_JDK_Root__Java_executable_was_not_found_1, null); //$NON-NLS-1$			
			} else {
				if (canDetectDefaultSystemLibraries(javaHome, javaExecutable)) {
					status = new Status(IStatus.OK, LaunchingPlugin.getUniqueIdentifier(), 0, LaunchingMessages.StandardVMType_ok_2, null); //$NON-NLS-1$
				} else {
					status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0, LaunchingMessages.StandardVMType_Not_a_JDK_root__System_library_was_not_found__1, null); //$NON-NLS-1$
				}
			}
		}
		return status;		
	}

	/**
	 * Generates library information for the given java executable. A main
	 * program is run (<code>org.eclipse.jdt.internal.launching.support.
	 * LibraryDetector</code>), that dumps the system properties for bootpath
	 * and extension directories. This output is then parsed and cached for
	 * future reference.
	 * 
	 * @return library info or <code>null</code> if none
	 */	
	protected LibraryInfo generateLibraryInfo(File javaHome, File javaExecutable) {
		LibraryInfo info = null;
		
		// if this is 1.1.X, the properties will not exist		
		IPath classesZip = new Path(javaHome.getAbsolutePath()).append("lib").append("classes.zip"); //$NON-NLS-1$ //$NON-NLS-2$
		if (classesZip.toFile().exists()) {
			return new LibraryInfo("1.1.x", new String[] {classesZip.toOSString()}, new String[0], new String[0]); //$NON-NLS-1$
		}
		//locate the launching support jar - it contains the main program to run
		File file = LaunchingPlugin.getFileInPlugin(new Path("lib/launchingsupport.jar")); //$NON-NLS-1$
		if (file.exists()) {	
			String javaExecutablePath = javaExecutable.getAbsolutePath();
			String[] cmdLine = new String[] {javaExecutablePath, "-classpath", file.getAbsolutePath(), "org.eclipse.jdt.internal.launching.support.LibraryDetector"};  //$NON-NLS-1$ //$NON-NLS-2$
			Process p = null;
			try {
				p = Runtime.getRuntime().exec(cmdLine);
				IProcess process = DebugPlugin.newProcess(new Launch(null, ILaunchManager.RUN_MODE, null), p, "Library Detection"); //$NON-NLS-1$
				for (int i= 0; i < 200; i++) {
					// Wait no more than 10 seconds (200 * 50 mils)
					if (process.isTerminated()) {
						break;
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
				}
				info = parseLibraryInfo(process);
			} catch (IOException ioe) {
				LaunchingPlugin.log(ioe);
			} finally {
				if (p != null) {
					p.destroy();
				}
			}
		}
		if (info == null) {
		    // log error that we were unable to generate library info - see bug 70011
		    LaunchingPlugin.log(MessageFormat.format("Failed to retrieve default libraries for {0}", new String[]{javaHome.getAbsolutePath()})); //$NON-NLS-1$
		}
		return info;
	}
	
	/**
	 * Parses the output from 'LibraryDetector'.
	 */
	protected LibraryInfo parseLibraryInfo(IProcess process) {
		IStreamsProxy streamsProxy = process.getStreamsProxy();
		String text = null;
		if (streamsProxy != null) {
			text = streamsProxy.getOutputStreamMonitor().getContents();
		}
		if (text != null && text.length() > 0) {
			int index = text.indexOf("|"); //$NON-NLS-1$
			if (index > 0) { 
				String version = text.substring(0, index);
				text = text.substring(index + 1);
				index = text.indexOf("|"); //$NON-NLS-1$	
				if (index > 0) {
					String bootPaths = text.substring(0, index);
					String[] bootPath = parsePaths(bootPaths);
					 
					text = text.substring(index + 1);
					index = text.indexOf("|"); //$NON-NLS-1$
					
					if (index > 0) {
						String extDirPaths = text.substring(0, index);
						String endorsedDirsPath = text.substring(index + 1);
						String[] extDirs = parsePaths(extDirPaths);
						String[] endDirs = parsePaths(endorsedDirsPath);
						return new LibraryInfo(version, bootPath, extDirs, endDirs);
					} 
				}
			}
		} 
		return null;
	}
	
	protected String[] parsePaths(String paths) {
		List list = new ArrayList();
		int pos = 0;
		int index = paths.indexOf(File.pathSeparatorChar, pos);
		while (index > 0) {
			String path = paths.substring(pos, index);
			list.add(path);
			pos = index + 1;	
			index = paths.indexOf(File.pathSeparatorChar, pos);
		}
		String path = paths.substring(pos);
		if (!path.equals("null")) { //$NON-NLS-1$
			list.add(path);
		}
		return (String[])list.toArray(new String[list.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#disposeVMInstall(java.lang.String)
	 */
	public void disposeVMInstall(String id) {
		IVMInstall vm = findVMInstall(id);
		if (vm != null) {
			String path = vm.getInstallLocation().getAbsolutePath();
            LaunchingPlugin.setLibraryInfo(path, null);
            fgFailedInstallPath.remove(path);
		}		
		super.disposeVMInstall(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractVMInstallType#getDefaultJavadocLocation(java.io.File)
	 */
	public URL getDefaultJavadocLocation(File installLocation) {
		File javaExecutable = findJavaExecutable(installLocation);
		if (javaExecutable != null) {
			LibraryInfo libInfo = getLibraryInfo(installLocation, javaExecutable);
			if (libInfo != null) {
				String version = libInfo.getVersion();
				if (version != null) {
					try {
						if (version.startsWith("1.5")) { //$NON-NLS-1$
							return new URL("http://java.sun.com/j2se/1.5.0/docs/api/"); //$NON-NLS-1$
						} else if (version.startsWith("1.4")) { //$NON-NLS-1$
							return new URL("http://java.sun.com/j2se/1.4.2/docs/api/"); //$NON-NLS-1$
						} else if (version.startsWith("1.3")) { //$NON-NLS-1$
							return new URL("http://java.sun.com/j2se/1.3/docs/api/"); //$NON-NLS-1$
						} else if (version.startsWith("1.2")) { //$NON-NLS-1$
							return new URL("http://java.sun.com/products/jdk/1.2/docs/api"); //$NON-NLS-1$
						}
					} catch (MalformedURLException e) {
					}
				}
			}
		}
		return null;
	}

}
