/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.LibraryLocation;

import com.ibm.icu.text.MessageFormat;

/**
 * Utility class for Standard VM type. Used to generate/retrieve information for
 * VMs defined by .ee property file.
 * 
 * @since 3.4
 */
public class EEVMType {
	
	/**
	 * Map of {EE File -> {Map of {PropertyName -> PropertyValue}}
	 */
	private static Map fgProperites = new HashMap();
	
	/**
	 * Map of {EE File -> Ordered list of vm arguments}
	 */
	private static Map fgArguments = new HashMap();
	
	public static final String PROP_ENDORSED_DIRS = "-Dee.endorsed.dirs";  //$NON-NLS-1$
	public static final String PROP_BOOT_CLASS_PATH = "-Dee.bootclasspath";  //$NON-NLS-1$
	public static final String PROP_SOURCE_ARCHIVE = "-Dee.src";  //$NON-NLS-1$
	public static final String PROP_EXTENSION_DIRS = "-Dee.ext.dirs";  //$NON-NLS-1$
	public static final String PROP_LANGUAGE_LEVEL = "-Dee.language.level";  //$NON-NLS-1$
	public static final String PROP_CLASS_LIB_LEVEL = "-Dee.class.library.level";  //$NON-NLS-1$
	public static final String PROP_EXECUTABLE = "-Dee.executable";  //$NON-NLS-1$
	public static final String PROP_EXECUTABLE_CONSOLE = "-Dee.executable.console";  //$NON-NLS-1$
	
	private static final String[] REQUIRED_PROPERTIES = new String[]{PROP_EXECUTABLE, PROP_BOOT_CLASS_PATH, PROP_LANGUAGE_LEVEL};
	
	/**
	 * Returns whether the given install location corresponds to an .ee file.
	 * 
	 * @param installLocation
	 * @return whether the given install location corresponds to an .ee file.
	 */
	public static boolean isEEInstall(File installLocation) {
		if (installLocation.isFile()) {
			String name = installLocation.getName();
			if (name.endsWith(".ee")) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#getDefaultLibraryLocations(java.io.File)
	 */
	public static LibraryLocation[] getDefaultLibraryLocations(File installLocation) {
		
		Map properties = getProperties(installLocation);
		if (properties == null) {
			return new LibraryLocation[]{};
		}
		
		List allLibs = new ArrayList(); 
		
		String dirs = getProperty(PROP_ENDORSED_DIRS, installLocation);
		if (dirs != null) {
			// Add all endorsed libraries - they are first, as they replace
			allLibs.addAll(StandardVMType.gatherAllLibraries(resolvePaths(dirs, installLocation)));
		}
		
		// next is the bootpath libraries
		dirs = getProperty(PROP_BOOT_CLASS_PATH, installLocation);
		if (dirs != null) {
			String[] bootpath = resolvePaths(dirs, installLocation);
			List boot = new ArrayList(bootpath.length);
			URL url = getDefaultJavadocLocation(installLocation);
			for (int i = 0; i < bootpath.length; i++) {
				IPath path = new Path(bootpath[i]);
				File lib = path.toFile(); 
				if (lib.exists() && lib.isFile()) {
					LibraryLocation libraryLocation = new LibraryLocation(path,
									getDefaultSourceLocation(installLocation),
									Path.EMPTY,
									url);
					boot.add(libraryLocation);
				}
			}
			allLibs.addAll(boot);
		}
				
		// Add all extension libraries
		dirs = getProperty(PROP_EXTENSION_DIRS, installLocation);
		if (dirs != null) {
			allLibs.addAll(StandardVMType.gatherAllLibraries(resolvePaths(dirs, installLocation)));
		}
		
		
		//remove duplicates
		HashSet set = new HashSet();
		LibraryLocation lib = null;
		for(ListIterator liter = allLibs.listIterator(); liter.hasNext();) {
			lib = (LibraryLocation) liter.next();
			if(!set.add(lib.getSystemLibraryPath().toOSString())) {
				//did not add it, duplicate
				allLibs.remove(lib);
			}
		}
		return (LibraryLocation[])allLibs.toArray(new LibraryLocation[allLibs.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractVMInstallType#getDefaultJavadocLocation(java.io.File)
	 */
	public static URL getDefaultJavadocLocation(File installLocation) {
		String version = getProperty(PROP_LANGUAGE_LEVEL, installLocation);
		if (version != null) {
			return StandardVMType.getDefaultJavadocLocation(version);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractVMInstallType#getDefaultVMArguments(java.io.File)
	 */
	public static String getDefaultVMArguments(File installLocation) {
		Map properties = getProperties(installLocation);
		if (properties != null) {
			List args = (List) fgArguments.get(installLocation);
			StringBuffer buf = new StringBuffer();
			Iterator iterator = args.iterator();
			while (iterator.hasNext()) {
				String arg = (String) iterator.next();
				buf.append(arg);
				if (iterator.hasNext()) {
					buf.append(" "); //$NON-NLS-1$
				}
			}
			return buf.toString();
		}
		return null;
	}	
	
	/**
	 * Returns the location of the source archive for the given ee file or the empty
	 * path if none.
	 * 
	 * @param eeFile property file
	 * @return source archive location or Path.EMPTY if none
	 */
	protected static IPath getDefaultSourceLocation(File eeFile) {
		String src = getProperty(PROP_SOURCE_ARCHIVE, eeFile);
		if (src != null) {
			String[] paths = resolvePaths(src, eeFile);
			if (paths.length == 1) {
				return new Path(paths[0]);
			}
		}
		return Path.EMPTY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#validateInstallLocation(java.io.File)
	 */
	public static IStatus validateInstallLocation(File installLocation) {
		Map properties = getProperties(installLocation);
		if (properties == null) {
			return new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), MessageFormat.format(LaunchingMessages.EEVMType_0, new String[]{installLocation.getName()} ));
		}
		// validate required properties
		for (int i = 0; i < REQUIRED_PROPERTIES.length; i++) {
			String key = REQUIRED_PROPERTIES[i];
			String property = (String) properties.get(key);
			if (property == null) {
				return new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), MessageFormat.format(LaunchingMessages.EEVMType_1, new String[]{installLocation.getName(), key} ));
			}
		}
		return Status.OK_STATUS;
	}
	
	/**
	 * Returns the standard executable for the given ee file. The non-console executable
	 * is considered first.
	 * 
	 * @param installLocation ee file
	 * @return standard executable
	 */
	public static File getExecutable(File installLocation) { 
		String property = getProperty(PROP_EXECUTABLE, installLocation);
		if (property == null) {
			property = getProperty(PROP_EXECUTABLE_CONSOLE, installLocation);
		}
		if (property != null) {
			String[] paths = resolvePaths(property, installLocation);
			if (paths.length == 1) {
				return new File(paths[0]);
			}
		}
		return null;
	}
	
	/**
	 * Returns the language level specified in the given property file, or <code>null</code>.
	 * 
	 * @param eeFile property file
	 * @return language level or <code>null</code>
	 */
	public static String getJavaVersion(File eeFile) {
		return getProperty(PROP_LANGUAGE_LEVEL, eeFile);
	}

	/**
	 * Returns the properties for the given ee file, or <code>null</code> if none.
	 * 
	 * @param eeFile
	 * @return properties or <code>null</code> if none
	 */
	private static synchronized Map getProperties(File eeFile) {
		Map properties = (Map) fgProperites.get(eeFile);
		if (properties == null) {
			BufferedReader bufferedReader = null;
			try {
				FileReader reader = new FileReader(eeFile);
				properties = new HashMap();
				List arguments = new ArrayList();
				bufferedReader = new BufferedReader(reader);
				String line = bufferedReader.readLine();
				while (line != null) {
					if (!line.startsWith("#")) { //$NON-NLS-1$
						int eq = line.indexOf('=');
						if (eq > 0) {
							String key = line.substring(0, eq);
							if (line.length() > eq + 1) {
								String value = line.substring(eq + 1);
								properties.put(key, value);
							}
						}
						arguments.add(line);
					}
					line = bufferedReader.readLine();
				}
				fgProperites.put(eeFile, properties);
				fgArguments.put(eeFile, arguments);
			} catch (FileNotFoundException e) {
				properties = null;
			} catch (IOException e) {
				properties = null;
			} finally {
				try {
					if (bufferedReader != null) {
						 bufferedReader.close();
					}
				} catch (IOException e) {
				}				
			}
		}
		return properties;
	}
	
	/**
	 * Returns all path strings contained in the given string based on system
	 * path delimiter, resolved relative to the ee property file.
	 * 
	 * @param paths
	 * @param eeFile properties file
	 * @return array of individual paths
	 */
	private static String[] resolvePaths(String paths, File eeFile) {
		String[] strings = paths.split(File.pathSeparator);
		IPath root = new Path(eeFile.getParentFile().getAbsolutePath());
		for (int i = 0; i < strings.length; i++) {
			String string = strings[i].trim();
			IPath path = new Path(string);
			if (!path.isAbsolute()) {
				IPath filePath = root.append(path);
				strings[i] = filePath.toOSString();
			}
		}
		return strings;
	}
	
	/**
	 * Returns the specified property from the given ee property file, or <code>null</code>
	 * if none.
	 * 
	 * @param propertyName key
	 * @param eeFile property file
	 * @return property value or <code>null</code>
	 */
	public static String getProperty(String propertyName, File eeFile) {
		Map properties = getProperties(eeFile);
		if (properties != null) {
			return (String) properties.get(propertyName);
		}
		return null;
	}
}
