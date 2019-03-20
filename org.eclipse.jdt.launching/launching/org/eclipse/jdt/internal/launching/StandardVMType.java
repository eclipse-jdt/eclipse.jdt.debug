/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Michael Allman - Bug 211648, Bug 156343 - Standard VM not supported on MacOS
 *     Thomas Schindl - Bug 399798, StandardVMType should allow to contribute default source and Javadoc locations for ext libraries
 *     Mikhail Kalkov - Bug 414285, On systems with large RAM, evaluateSystemProperties and generateLibraryInfo fail for 64-bit JREs
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.internal.core.OutputStreamMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.ILibraryLocationResolver;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.osgi.util.NLS;

/**
 * A VM install type for VMs the conform to the standard
 * JDK installation layout.
 */
public class StandardVMType extends AbstractVMInstallType {

	/**
	 * Constants for common {@link String}s
	 * @since 3.7
	 */
	private static final String RT_JAR = "rt.jar"; //$NON-NLS-1$
	private static final String SRC = "src"; //$NON-NLS-1$
	private static final String SRC_ZIP = "src.zip"; //$NON-NLS-1$
	private static final String SRC_JAR = "src.jar"; //$NON-NLS-1$
	private static final String JRE = "jre"; //$NON-NLS-1$
	private static final String LIB = "lib"; //$NON-NLS-1$
	private static final String BAR = "|"; //$NON-NLS-1$
	private static final String RELEASE_FILE = "release"; //$NON-NLS-1$
	private static final String JAVA_VERSION = "JAVA_VERSION"; //$NON-NLS-1$
	private static final String JRT_FS_JAR = "jrt-fs.jar"; //$NON-NLS-1$

	public static final String ID_STANDARD_VM_TYPE = "org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType"; //$NON-NLS-1$

	/**
	 * The minimal -Xmx size for launching a JVM. <br>
	 * <b>Note:</b> Must be omitted for Standard11xVM! <br>
	 * <b>Note:</b> Must be at least -Xmx16m for JRockit, see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=433455">bug 433455</a>.
	 *
	 * @since 3.7.100
	 */
	public static final String MIN_VM_SIZE = "-Xmx16m"; //$NON-NLS-1$

	/**
	 * Name filter for files ending in .jar or .zip
	 *
	 * @since 3.7.0
	 */
	private static FilenameFilter fgArchiveFilter = new FilenameFilter() {
		@Override
		public boolean accept(File arg0, String arg1) {
			return arg1.endsWith(".zip") || arg1.endsWith(".jar");  //$NON-NLS-1$//$NON-NLS-2$
		}
	};

	/**
	 * The root path for the attached source
	 */
	private String fDefaultRootPath = ""; //$NON-NLS-1$

	/**
	 * Map of the install path for which we were unable to generate
	 * the library info during this session.
	 */
	private static Map<String, LibraryInfo> fgFailedInstallPath = new HashMap<>();

	/**
	 * Cache for default library locations. See {@link #getDefaultLibraryLocations(File)}
	 * <br><br>
	 * Map&lt;{@link String}, {@link LibraryLocation}&gt;
	 *
	 * @since 3.7
	 */
	private static Map<String, List<LibraryLocation>> fgDefaultLibLocs = new HashMap<>();

	/**
	 * The list of locations in which to look for the java executable in candidate
	 * VM install locations, relative to the VM install location. From Java 9 onwards, there may not be a jre directory.
	 */
	private static final String[] fgCandidateJavaFiles = {"javaw", "javaw.exe", "java", "java.exe", "j9w", "j9w.exe", "j9", "j9.exe"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	private static final String[] fgCandidateJavaLocations = { File.separator, "bin" + File.separatorChar, //$NON-NLS-1$
			JRE + File.separatorChar + "bin" + File.separatorChar };//$NON-NLS-1$

	private static ILibraryLocationResolver[] fgLibraryLocationResolvers = null;

	/**
	 * Starting in the specified VM install location, attempt to find the 'java' executable
	 * file.  If found, return the corresponding <code>File</code> object, otherwise return
	 * <code>null</code>.
	 * @param vmInstallLocation the {@link File} location to look in
	 * @return the {@link File} for the Java executable or <code>null</code>
	 */
	public static File findJavaExecutable(File vmInstallLocation) {
		// Try each candidate in order.  The first one found wins.  Thus, the order
		// of fgCandidateJavaLocations and fgCandidateJavaFiles is significant.

		boolean isBin = false;
		String filePath = vmInstallLocation.getPath();
		int index = filePath.lastIndexOf(File.separatorChar);
		if (index > 0 && filePath.substring(index + 1).equals("bin")) { //$NON-NLS-1$
			isBin = true;
		}
		for (int i = 0; i < fgCandidateJavaFiles.length; i++) {
			for (int j = 0; j < fgCandidateJavaLocations.length; j++) {
				if (!isBin && j == 0) {
					// search in "." only under bin for java executables for Java 9 and above
					continue;
				}
				File javaFile = new File(vmInstallLocation, fgCandidateJavaLocations[j] + fgCandidateJavaFiles[i]);
				if (javaFile.isFile()) {
					return javaFile;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the listing of {@link ILibraryLocationResolver}s
	 *
	 * @return the known list of {@link ILibraryLocationResolver}s
	 * @since 3.7.0
	 */
	private static ILibraryLocationResolver[] getLibraryLocationResolvers() {
		if( fgLibraryLocationResolvers == null ) {
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(LaunchingPlugin.ID_PLUGIN, JavaRuntime.EXTENSION_POINT_LIBRARY_LOCATION_RESOLVERS);
			IConfigurationElement[] configs = extensionPoint.getConfigurationElements();
			// The "libraryLocationResolvers" extension point doesn't have any conflict resolution.
			// Sorting by namespace at least makes makes the order predictable.
			Arrays.sort(configs, new Comparator<IConfigurationElement>() {
				@Override
				public int compare(IConfigurationElement e1, IConfigurationElement e2) {
					return e1.getNamespaceIdentifier().compareTo(e2.getNamespaceIdentifier());
				}
			});
			List<ILibraryLocationResolver> resolvers = new ArrayList<>(configs.length);
			for( int i = 0; i < configs.length; i++ ) {
				IConfigurationElement e = configs[i];
				try {
					resolvers.add((ILibraryLocationResolver) e.createExecutableExtension("class")); //$NON-NLS-1$
				}
				catch (CoreException e1) {
					LaunchingPlugin.log(e1.getStatus());
				}
			}
			fgLibraryLocationResolvers = resolvers.toArray(new ILibraryLocationResolver[0]);
		}
		return fgLibraryLocationResolvers;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#getName()
	 */
	@Override
	public String getName() {
		return LaunchingMessages.StandardVMType_Standard_VM_3;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractVMInstallType#doCreateVMInstall(java.lang.String)
	 */
	@Override
	protected IVMInstall doCreateVMInstall(String id) {
		return new StandardVM(this, id);
	}

	/**
	 * Return library information corresponding to the specified install
	 * location. If the information does not exist, create it using the given Java
	 * executable.
	 * @param javaHome the Java home folder
	 * @param javaExecutable the Java executable file
	 * @return the {@link LibraryInfo} for the home and executable path or an empty object, never <code>null</code>
	 */
	protected synchronized LibraryInfo getLibraryInfo(File javaHome, File javaExecutable) {
		String installPath = javaHome.getAbsolutePath();
		LibraryInfo info = LaunchingPlugin.getLibraryInfo(installPath);
		if (info == null || LaunchingPlugin.timeStampChanged(installPath)) {
			info = fgFailedInstallPath.get(installPath);
			if (info == null) {
				String version = readReleaseVersion(javaHome);
				if (JavaCore.compareJavaVersions(version, JavaCore.VERSION_1_8) > 0) {
					info = new LibraryInfo(version, new String[0], new String[0], new String[0]);
					LaunchingPlugin.setLibraryInfo(installPath, info);
				} else {
					info = generateLibraryInfo(javaHome, javaExecutable);
					if (info == null) {
						// Bug 536943: try again in case we have a timing problem with process execution and output retrieval
						info = generateLibraryInfo(javaHome, javaExecutable);
					}
					if (info == null) {
						info = getDefaultLibraryInfo(javaHome);
						fgFailedInstallPath.put(installPath, info);
					} else {
						// only persist if we were able to generate information - see bug 70011
						LaunchingPlugin.setLibraryInfo(installPath, info);
					}
				}
			}
		}
		return info;
	}

	/**
	 * Return <code>true</code> if the appropriate system libraries can be found for the
	 * specified java executable, <code>false</code> otherwise.
	 * @param javaHome the Java home folder
	 * @param javaExecutable the Java executable file
	 * @return <code>true</code> if the default system libraries can be detected for the given install location
	 * <code>false</code> otherwise
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
	 * @param javaHome the Java home folder
	 * @param javaExecutable the Java executable file
	 * @return String
	 */
	protected String getVMVersion(File javaHome, File javaExecutable) {
		LibraryInfo info = getLibraryInfo(javaHome, javaExecutable);
		return info.getVersion();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#detectInstallLocation()
	 */
	@Override
	public File detectInstallLocation() {
		// We want a Mac OSX VM install so don't process the install location for this type
		if(Platform.OS_MACOSX.equals(Platform.getOS())) {
			return null;
		}
		return getJavaHomeLocation();
	}

	/**
	 * Returns the VM install location using the <code>java.home</code> system
	 * property or <code>null</code>.
	 *
	 * @return the install location of this type of VM based off of the
	 *  <code>java.home</code> system property, or <code>null</code> if not found
	 *
	 * @since 3.7
	 */
	protected File getJavaHomeLocation() {
		// Retrieve the 'java.home' system property.  If that directory doesn't exist,
		// return null.
		File javaHome;
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
		if (javaHome.getName().equalsIgnoreCase(JRE)) {
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
	 *
	 * @param javaHome the Java home folder
	 * @return the {@link IPath} to the <code>rt.jar</code> file
	 */
	protected IPath getDefaultSystemLibrary(File javaHome) {
		IPath jreLibPath= new Path(javaHome.getPath()).append(LIB).append(RT_JAR);
		if (jreLibPath.toFile().isFile()) {
			return jreLibPath;
		}
		return new Path(javaHome.getPath()).append(JRE).append(LIB).append(RT_JAR);
	}

	/**
	 * Returns a path to the source attachment for the given library, or
	 * an empty path if none.
	 *
	 * @param libLocation the {@link File} location of the library to find the source for
	 * @return a path to the source attachment for the given library, or
	 *  an empty path if none
	 */
	protected IPath getDefaultSystemLibrarySource(File libLocation) {
		File parent= libLocation.getParentFile();
		while (parent != null) {
			File parentsrc= new File(parent, SRC_JAR);
			if (parentsrc.isFile()) {
				setDefaultRootPath(SRC);
				return new Path(parentsrc.getPath());
			}
			parentsrc= new File(parent, SRC_ZIP);
			if (parentsrc.isFile()) {
				setDefaultRootPath(""); //$NON-NLS-1$
				return new Path(parentsrc.getPath());
			}
			parent = parent.getParentFile();
		}
		// if we didn't find any of the normal source files, look for J9 source
		IPath result = checkForJ9LibrarySource(libLocation);
		if (result != null) {
			return result;
		}
		// check for <lib>-src.jar pattern
		IPath libName = new Path(libLocation.getName());
		String extension = libName.getFileExtension();
		String prefix = libName.removeFileExtension().lastSegment();
		if (extension != null) {
			IPath srcPath = new Path(libLocation.getPath());
			srcPath = srcPath.removeLastSegments(1);
			StringBuilder buf = new StringBuilder();
			buf.append(prefix);
			buf.append("-src."); //$NON-NLS-1$
			buf.append(extension);
			srcPath = srcPath.append(buf.toString());
			if (srcPath.toFile().exists()) {
				return srcPath;
			}
		}
		setDefaultRootPath(""); //$NON-NLS-1$
		return Path.EMPTY;
	}

	// J9 has a known/fixed structure for its libraries and source locations.  Here just
	// look for the source associated with each lib.
	private IPath checkForJ9LibrarySource(File libLocation) {
		File parent= libLocation.getParentFile();
		String name = libLocation.getName();
		if (name.equalsIgnoreCase("classes.zip")) { //$NON-NLS-1$
			File source = new File(parent, "source/source.zip"); //$NON-NLS-1$
			return source.isFile() ? new Path(source.getPath()) : Path.EMPTY;
		}
		if (name.equalsIgnoreCase("locale.zip")) { //$NON-NLS-1$
			File source = new File(parent, "source/locale-src.zip"); //$NON-NLS-1$
			return source.isFile() ? new Path(source.getPath()) : Path.EMPTY;
		}
		if (name.equalsIgnoreCase("charconv.zip")) { //$NON-NLS-1$
			File source = new File(parent, "charconv-src.zip"); //$NON-NLS-1$
			return source.isFile() ? new Path(source.getPath()) : Path.EMPTY;
		}
		return null;
	}

	/**
	 * Returns the package root path
	 *
	 * @return the package root path
	 */
	protected IPath getDefaultPackageRootPath() {
		return new Path(getDefaultRootPath());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#getDefaultLibraryLocations(java.io.File)
	 */
	@Override
	public LibraryLocation[] getDefaultLibraryLocations(File installLocation) {
		//NOTE: We do not add libraries from the "endorsed" directory explicitly, as
		//the bootpath contains these entries already (if they exist).
		// Determine the java executable that corresponds to the specified install location
		// and use this to generate library information.  If no java executable was found,
		// the 'standard' libraries will be returned.
		List<LibraryLocation> allLibs = fgDefaultLibLocs.get(installLocation.getAbsolutePath());
		if(allLibs == null) {
			File javaExecutable = findJavaExecutable(installLocation);
			LibraryInfo libInfo;
			if (javaExecutable == null) {
				libInfo = getDefaultLibraryInfo(installLocation);
			} else {
				libInfo = getLibraryInfo(installLocation, javaExecutable);
			}

			// Add all endorsed libraries - they are first, as they replace
			allLibs = new ArrayList<>(gatherAllLibraries(libInfo.getEndorsedDirs()));
			URL url = getDefaultJavadocLocation(installLocation);
			if (libInfo.getBootpath().length == 0) {
				// TODO: Bug 489207: Temporary workaround for Jigsaw-previews that don't declare a bootpath.
				// JDT Core currently requires a non-empty library path, so let's give it jrt-fs.jar as a stand-in for now.
				// Code referencing org.eclipse.jdt.internal.compiler.util.JimageUtil.JRT_FS_JAR looks for this file.
				IPath sourceRootPath = Path.EMPTY;
				// src zip moved to lib folder from JDK 9 EA Build 151
				IPath path = new Path(installLocation.getAbsolutePath()).append(LIB).append(SRC_ZIP);
				File lib = path.toFile();
				if (lib.exists() && lib.isFile()) {
					sourceRootPath = getDefaultSystemLibrarySource(lib); // To attach source if available
				} else {
					path = new Path(installLocation.getAbsolutePath()).append(SRC_ZIP);
					lib = path.toFile();
					if (lib.exists() && lib.isFile()) {
						sourceRootPath = getDefaultSystemLibrarySource(lib); // To attach source if available
					}
				}
				IPath pathName = new Path(installLocation.getAbsolutePath()).append(LIB).append(JRT_FS_JAR);
				// From Java 9 149 version, we see that jrt-fs.jar is moved to lib directory so we need to look at both places
				File jrtfsJar = pathName.toFile();
				if (!jrtfsJar.exists()) {
					pathName = new Path(installLocation.getAbsolutePath()).append(JRT_FS_JAR);
				}

				LibraryLocation libraryLocation = new LibraryLocation(pathName,
						sourceRootPath, getDefaultPackageRootPath(),
						getDefaultJavadocLocation(installLocation));
				allLibs.add(libraryLocation);
			}

			// next is the boot path libraries
			String[] bootpath = libInfo.getBootpath();
			List<LibraryLocation> boot = new ArrayList<>(bootpath.length);

			for (int i = 0; i < bootpath.length; i++) {
				IPath path = new Path(bootpath[i]);
				File lib = path.toFile();
				if (lib.exists() && lib.isFile()) {
					LibraryLocation libraryLocation = new LibraryLocation(path,
									getDefaultSystemLibrarySource(lib),
									getDefaultPackageRootPath(),
									url);
					boot.add(libraryLocation);
				}
			}
			allLibs.addAll(boot);

			// Add all extension libraries
			allLibs.addAll(gatherAllLibraries(libInfo.getExtensionDirs()));

			//remove duplicates
			HashSet<String> set = new HashSet<>();
			LibraryLocation lib = null;
			for(ListIterator<LibraryLocation> liter = allLibs.listIterator(); liter.hasNext();) {
				lib = liter.next();
				IPath systemLibraryPath = lib.getSystemLibraryPath();
				String device = systemLibraryPath.getDevice();
				if (device != null) {
					// @see Bug 197866 - Installed JRE Wizard creates duplicate system libraries when drive letter is lower case
					systemLibraryPath = systemLibraryPath.setDevice(device.toUpperCase());
				}
				if(!set.add(systemLibraryPath.toOSString())) {
					//did not add it, duplicate
					liter.remove();
				}
			}
			fgDefaultLibLocs.put(installLocation.getAbsolutePath(), allLibs);
		}
		return allLibs.toArray(new LibraryLocation[allLibs.size()]);
	}

	/**
	 * Returns default library information for the given install location.
	 *
	 * @param installLocation the VM install location
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
	 * Returns a list of all zip's and jars contained in the given directories.
	 *
	 * @param dirPaths a list of absolute paths of directories to search
	 * @return List of all zip's and jars
	 */
	public static List<LibraryLocation> gatherAllLibraries(String[] dirPaths) {
		List<LibraryLocation> libraries = new ArrayList<>();
		for (int i = 0; i < dirPaths.length; i++) {
			File extDir = new File(dirPaths[i]);
			if (extDir.isDirectory()) {
				String[] names = extDir.list(fgArchiveFilter);
				if (names != null) {
					for (int j = 0; j < names.length; j++) {
						File jar = new File(extDir, names[j]);
						if (jar.isFile()) {
							try {
								IPath libPath = new Path(jar.getCanonicalPath());
								IPath sourcePath = Path.EMPTY;
								IPath packageRoot = Path.EMPTY;
								URL javadocLocation = null;
								URL indexLocation = null;
								for (ILibraryLocationResolver resolver : getLibraryLocationResolvers()) {
									try {
										sourcePath = resolver.getSourcePath(libPath);
										packageRoot = resolver.getPackageRoot(libPath);
										javadocLocation = resolver.getJavadocLocation(libPath);
										indexLocation = resolver.getIndexLocation(libPath);
										if (sourcePath != Path.EMPTY || packageRoot != Path.EMPTY || javadocLocation != null || indexLocation != null) {
											break;
										}
									} catch(Exception e) {
										LaunchingPlugin.log(e);
									}
								}
								LibraryLocation library = new LibraryLocation(libPath, sourcePath, packageRoot, javadocLocation, indexLocation);
								libraries.add(library);
							} catch (IOException e) {
								LaunchingPlugin.log(e);
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
	 * @param installLocation the VM install location
	 * @return default extension directory or <code>null</code>
	 */
	protected File getDefaultExtensionDirectory(File installLocation) {
		File jre = null;
		if (installLocation.getName().equalsIgnoreCase(JRE)) {
			jre = installLocation;
		} else {
			jre = new File(installLocation, JRE);
		}
		File lib = new File(jre, LIB);
		File ext = new File(lib, "ext"); //$NON-NLS-1$
		return ext;
	}

	/**
	 * Returns the default location of the endorsed directory, based on the
	 * given install location. The resulting file may not exist, or be
	 * <code>null</code> if an endorsed directory is not supported.
	 *
	 * @param installLocation the VM install location
	 * @return default endorsed directory or <code>null</code>
	 */
	protected File getDefaultEndorsedDirectory(File installLocation) {
		File lib = new File(installLocation, LIB);
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
	@Override
	public IStatus validateInstallLocation(File javaHome) {
		IStatus status = null;
		File javaExecutable = findJavaExecutable(javaHome);
		if (javaExecutable == null) {
			status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0, LaunchingMessages.StandardVMType_Not_a_JDK_Root__Java_executable_was_not_found_1, null); //
		} else {
			File javaHomeNew = javaHome;

			if (canDetectDefaultSystemLibraries(javaHomeNew, javaExecutable)) {
				status = new Status(IStatus.OK, LaunchingPlugin.getUniqueIdentifier(), 0, LaunchingMessages.StandardVMType_ok_2, null);
			} else {
				status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0, LaunchingMessages.StandardVMType_Not_a_JDK_root__System_library_was_not_found__1, null);
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
	 * @param javaHome the Java home folder
	 * @param javaExecutable the Java executable file
	 *
	 * @return library info or <code>null</code> if none
	 */
	protected LibraryInfo generateLibraryInfo(File javaHome, File javaExecutable) {
		LibraryInfo info = null;

		// if this is 1.1.X, the properties will not exist
		IPath classesZip = new Path(javaHome.getAbsolutePath()).append(LIB).append("classes.zip"); //$NON-NLS-1$
		if (classesZip.toFile().exists()) {
			return new LibraryInfo("1.1.x", new String[] {classesZip.toOSString()}, new String[0], new String[0]); //$NON-NLS-1$
		}
		//locate the launching support jar - it contains the main program to run
		File file = LaunchingPlugin.getFileInPlugin(new Path("lib/launchingsupport.jar")); //$NON-NLS-1$
		if (file != null && file.exists()) {
			String javaExecutablePath = javaExecutable.getAbsolutePath();
			String[] cmdLine = new String[] { javaExecutablePath, MIN_VM_SIZE,
					"-classpath", file.getAbsolutePath(), "org.eclipse.jdt.internal.launching.support.LibraryDetector" }; //$NON-NLS-1$ //$NON-NLS-2$
			Process p = null;
			try {
				String envp[] = null;
				if (Platform.OS_MACOSX.equals(Platform.getOS())) {
					Map<String, String> map = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironmentCasePreserved();
					if (map.remove(StandardVMDebugger.JAVA_JVM_VERSION) != null) {
						envp = new String[map.size()];
						Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
						int i = 0;
						while (iterator.hasNext()) {
							Entry<String, String> entry = iterator.next();
							envp[i] = entry.getKey() + "=" + entry.getValue(); //$NON-NLS-1$
							i++;
						}
					}
				}
				p = DebugPlugin.exec(cmdLine, null, envp);
				IProcess process = DebugPlugin.newProcess(new Launch(null, ILaunchManager.RUN_MODE, null), p, "Library Detection"); //$NON-NLS-1$
				process.setAttribute(IProcess.ATTR_CMDLINE, String.join(" ", cmdLine)); //$NON-NLS-1$
				IStreamMonitor outputStreamMonitor = process.getStreamsProxy().getOutputStreamMonitor();
				for (int i= 0; i < 600; i++) {
					// Wait no more than 30 seconds (600 * 50 milliseconds),
					// also ensure we are done reading the process stream, if we have a OutputStreamMonitor
					if (process.isTerminated() && isReadingDone(outputStreamMonitor)) {
						break;
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						LaunchingPlugin.log(e);
					}
				}
				checkProcessResult(process);
				info = parseLibraryInfo(process);
			} catch (Throwable ioe) {
				LaunchingPlugin.log(ioe);
			} finally {
				if (p != null) {
					p.destroy();
				}
			}
		}
		if (info == null) {
		    // log error that we were unable to generate library information - see bug 70011
		    LaunchingPlugin.log(NLS.bind("Failed to retrieve default libraries for {0}", new String[]{javaHome.getAbsolutePath()})); //$NON-NLS-1$
		}
		return info;
	}

	@SuppressWarnings("restriction")
	private boolean isReadingDone(IStreamMonitor monitor) {
		if (monitor instanceof OutputStreamMonitor) {
			return ((OutputStreamMonitor) monitor).isReadingDone();
		}
		return true;
	}

	/**
	 * Parses the output from 'LibraryDetector'.
	 *
	 * @param process the backing {@link IProcess} that was run
	 * @return the new {@link LibraryInfo} object or <code>null</code>
	 */
	protected LibraryInfo parseLibraryInfo(IProcess process) {
		IStreamsProxy streamsProxy = process.getStreamsProxy();
		String text = null;
		if (streamsProxy != null) {
			text = streamsProxy.getOutputStreamMonitor().getContents();
		}
		if (text != null && text.length() > 0) {
			int index = text.indexOf(BAR);
			if (index > 0) {
				String version = text.substring(0, index);
				text = text.substring(index + 1);
				index = text.indexOf(BAR);
				if (index > 0) {
					String bootPaths = text.substring(0, index);
					String[] bootPath = parsePaths(bootPaths);

					text = text.substring(index + 1);
					index = text.indexOf(BAR);

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
		List<String> list = new ArrayList<>();
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
		return list.toArray(new String[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#disposeVMInstall(java.lang.String)
	 */
	@Override
	public void disposeVMInstall(String id) {
		IVMInstall vm = findVMInstall(id);
		if (vm != null) {
			String path = vm.getInstallLocation().getAbsolutePath();
            LaunchingPlugin.setLibraryInfo(path, null);
            fgFailedInstallPath.remove(path);
            fgDefaultLibLocs.remove(path);
		}
		super.disposeVMInstall(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractVMInstallType#getDefaultJavadocLocation(java.io.File)
	 */
	@Override
	public URL getDefaultJavadocLocation(File installLocation) {
		File javaExecutable = findJavaExecutable(installLocation);
		if (javaExecutable != null) {
			LibraryInfo libInfo = getLibraryInfo(installLocation, javaExecutable);
			if (libInfo != null) {
				String version = libInfo.getVersion();
				return getDefaultJavadocLocation(version);
			}
		}
		return null;
	}

	/**
	 * Returns a default Javadoc location for a language version, or <code>null</code>.
	 *
	 * @param version language version such as "1.4"
	 * @return URL to default Javadoc location, or <code>null</code>
	 */
	public static URL getDefaultJavadocLocation(String version) {
		try {
			if (version.startsWith(JavaCore.VERSION_12)) {
				return new URL("https://docs.oracle.com/en/java/javase/12/docs/api/"); //$NON-NLS-1$
			} else if (version.startsWith(JavaCore.VERSION_11)) {
				return new URL("https://docs.oracle.com/en/java/javase/11/docs/api/"); //$NON-NLS-1$
			} else if (version.startsWith(JavaCore.VERSION_10)) {
				return new URL("https://docs.oracle.com/javase/10/docs/api/"); //$NON-NLS-1$
			} else if (version.startsWith(JavaCore.VERSION_9)) {
				return new URL("https://docs.oracle.com/javase/9/docs/api/"); //$NON-NLS-1$
			} else if (version.startsWith(JavaCore.VERSION_1_8)) {
				return new URL("https://docs.oracle.com/javase/8/docs/api/"); //$NON-NLS-1$
			} else if (version.startsWith(JavaCore.VERSION_1_7)) {
				return new URL("https://docs.oracle.com/javase/7/docs/api/"); //$NON-NLS-1$
			} else if (version.startsWith(JavaCore.VERSION_1_6)) {
				return new URL("https://docs.oracle.com/javase/6/docs/api/"); //$NON-NLS-1$
			} else if (version.startsWith(JavaCore.VERSION_1_5)) {
				return new URL("https://docs.oracle.com/javase/1.5.0/docs/api/"); //$NON-NLS-1$
			} else if (version.startsWith(JavaCore.VERSION_1_4)) {
				// archived: http://download.oracle.com/javase/1.4.2/docs/api/
				return new URL("https://docs.oracle.com/javase/1.5.0/docs/api/"); //$NON-NLS-1$
			} else if (version.startsWith(JavaCore.VERSION_1_3)) {
				// archived: http://download.oracle.com/javase/1.3/docs/api/
				return new URL("https://docs.oracle.com/javase/1.5.0/docs/api/"); //$NON-NLS-1$
			}
		} catch (MalformedURLException e) {
		}
		return null;
	}

	public synchronized String readReleaseVersion(File javaHome) {

		String version = ""; //$NON-NLS-1$

		if (Files.notExists(Paths.get(javaHome.getAbsolutePath(), RELEASE_FILE))) {
			return version;
		}
		try (Stream<String> lines = Files.lines(Paths.get(javaHome.getAbsolutePath(), RELEASE_FILE), Charset.defaultCharset()).filter(s -> s.contains(JAVA_VERSION))) {
			Optional<String> hasVersion = lines.findFirst();
			if (hasVersion.isPresent()) {
				String line = hasVersion.get();
				version = line.substring(14, line.length() - 1); // length of JAVA_VERSION + 2 in JAVA_VERSION="9"
			}
		}
		catch (UncheckedIOException | IOException e) {
			LaunchingPlugin.log(e);
		}

		return version;

	}

	/*
	 * Logs an error if the process is not yet done, or if the process exited with an error code.
	 */
	private static void checkProcessResult(IProcess process) throws DebugException {
		boolean isTerminated = process.isTerminated();
		if (!isTerminated) {
			String output = getOutput(process);
			Object[] errorInfo = { process.getAttribute(IProcess.ATTR_CMDLINE), output };
			String errorMessage = NLS.bind("Process not finished.\n Command line arguments: {0}\nOutput: {1}", errorInfo); //$NON-NLS-1$
			IllegalStateException exception = new IllegalStateException(errorMessage);
			LaunchingPlugin.log(exception);
		} else {
			int exitCode = process.getExitValue();
			if (exitCode != 0) {
				String output = getOutput(process);
				Object[] errorInfo = { Integer.valueOf(exitCode), process.getAttribute(IProcess.ATTR_CMDLINE), output };
				String errorMessage = NLS.bind("Process returned with error code \"{0}\".\nCommand line arguments: {1}\nOutput: {2}", errorInfo); //$NON-NLS-1$
				IllegalStateException exception = new IllegalStateException(errorMessage);
				LaunchingPlugin.log(exception);
			}
		}
	}

	private static String getOutput(IProcess process) {
		IStreamsProxy streamsProxy = process.getStreamsProxy();
		String output = "IProcess.getStreamsProxy() returned null"; //$NON-NLS-1$
		if (streamsProxy != null) {
			String[] lines = { "Standard output:", //$NON-NLS-1$
					streamsProxy.getOutputStreamMonitor().getContents(), "Standard error:", //$NON-NLS-1$
					streamsProxy.getErrorStreamMonitor().getContents() };
			output = String.join(System.lineSeparator(), Arrays.asList(lines));
		}
		return output;
	}
}
