/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.LibraryLocation;

import com.ibm.icu.text.MessageFormat;

/**
 * Utility class for Standard VM type. Used to generate/retrieve information for
 * VMs defined by .ee property file.
 * 
 * @since 3.4
 */
public class EEVMType extends AbstractVMInstallType {
	
	/**
	 * VM Type id
	 */
	public static final String ID_EE_VM_TYPE = "org.eclipse.jdt.launching.EEVMType"; //$NON-NLS-1$
	
	/**
	 * Map of {EE File -> {Map of {PropertyName -> PropertyValue}}
	 */
	private static Map fgProperties = new HashMap();
	
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
	public static final String PROP_JAVA_HOME = "-Djava.home";  //$NON-NLS-1$
	public static final String PROP_DEBUG_ARGS = "-Dee.debug.args";  //$NON-NLS-1$
	
	/**
	 * Substitution in EE file - replaced with directory of EE file,
	 * to support absolute path names where needed.
	 */
	public static final String VAR_EE_HOME = "${ee.home}"; //$NON-NLS-1$
	
	private static final String[] REQUIRED_PROPERTIES = new String[]{PROP_EXECUTABLE, PROP_BOOT_CLASS_PATH, PROP_LANGUAGE_LEVEL, PROP_JAVA_HOME};

	/**
	 * Returns the library locations defined in the given definition file.
	 * 
	 * @param eeFile definition file
	 * @return library locations defined in the file or an empty collection if none
	 */
	public static LibraryLocation[] getLibraryLocations(File eeFile) {
		
		Map properties = getProperties(eeFile);
		if (properties == null) {
			return new LibraryLocation[]{};
		}
		
		List allLibs = new ArrayList(); 
		
		String dirs = getProperty(PROP_ENDORSED_DIRS, eeFile);
		if (dirs != null) {
			// Add all endorsed libraries - they are first, as they replace
			allLibs.addAll(StandardVMType.gatherAllLibraries(resolvePaths(dirs, eeFile)));
		}
		
		// next is the bootpath libraries
		dirs = getProperty(PROP_BOOT_CLASS_PATH, eeFile);
		if (dirs != null) {
			String[] bootpath = resolvePaths(dirs, eeFile);
			List boot = new ArrayList(bootpath.length);
			URL url = getJavadocLocation(eeFile);
			for (int i = 0; i < bootpath.length; i++) {
				IPath path = new Path(bootpath[i]);
				File lib = path.toFile(); 
				if (lib.exists() && lib.isFile()) {
					LibraryLocation libraryLocation = new LibraryLocation(path,
									getDefaultSourceLocation(eeFile),
									Path.EMPTY,
									url);
					boot.add(libraryLocation);
				}
			}
			allLibs.addAll(boot);
		}
				
		// Add all extension libraries
		dirs = getProperty(PROP_EXTENSION_DIRS, eeFile);
		if (dirs != null) {
			allLibs.addAll(StandardVMType.gatherAllLibraries(resolvePaths(dirs, eeFile)));
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
	

	/**
	 * Returns the javadoc location specified in the definition file or <code>null</code>
	 * if none.
	 * 
	 * @param eeFile definition file
	 * @return javadoc location specified in the definition file or <code>null</code> if none
	 */
	public static URL getJavadocLocation(File eeFile) {
		String version = getProperty(PROP_LANGUAGE_LEVEL, eeFile);
		if (version != null) {
			return StandardVMType.getDefaultJavadocLocation(version);
		}
		return null;
	}

	/**
	 * Returns arguments used to start this VM in debug mode or
	 * <code>null</code> if none.
	 * 
	 * @param eeFile description file
	 * @return debug VM arguments or <code>null</code> if default
	 */
	public static String getDebugArgs(File eeFile) {
		return getProperty(PROP_DEBUG_ARGS, eeFile);
	}
	
	/**
	 * Returns the definition file associated with the given VM or <code>null</code>
	 * if none.
	 * 
	 * @param vm VM install
	 * @return definition file or <code>null</code> if none. The file may/may not exist.
	 */
	public static File getDefinitionFile(IVMInstall vm) {
		if (vm instanceof AbstractVMInstall) {
			AbstractVMInstall avm = (AbstractVMInstall) vm;
			String path = avm.getAttribute(EEVMInstall.ATTR_DEFINITION_FILE);
			if (path != null) {
				return new File(path);
			}
		}
		return null;
	}
	
	/**
	 * Returns VM arguments defined in the given definition file or <code>null</code> if none.
	 * 
	 * @param eeFile definition file
	 * @return VM arguments or <code>null</code> if none
	 */
	public static String getVMArguments(File eeFile) {
		Map properties = getProperties(eeFile);
		if (properties != null) {
			List args = (List) fgArguments.get(eeFile);
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

	/**
	 * Returns a status indicating if the given definition file is valid.
	 * 
	 * @param eeFile definition file
	 * @return status indicating if the given definition file is valid
	 */
	public static IStatus validateDefinitionFile(File eeFile) {
		Map properties = getProperties(eeFile);
		if (properties == null) {
			return new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), MessageFormat.format(LaunchingMessages.EEVMType_0, new String[]{eeFile.getName()} ));
		}
		// validate required properties
		for (int i = 0; i < REQUIRED_PROPERTIES.length; i++) {
			String key = REQUIRED_PROPERTIES[i];
			String property = (String) properties.get(key);
			if (property == null) {
				return new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), MessageFormat.format(LaunchingMessages.EEVMType_1, new String[]{eeFile.getName(), key} ));
			}
		}
		return Status.OK_STATUS;
	}
	
	/**
	 * Returns the standard executable for the given ee file. The non-console executable
	 * is considered first.
	 * 
	 * @param installLocation ee file
	 * @return standard executable or <code>null</code> if none
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
		Map properties = (Map) fgProperties.get(eeFile);
		String eeHome = eeFile.getParentFile().getAbsolutePath();
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
						line = resolve(line, eeHome);
						int eq = line.indexOf('=');
						if (eq > 0) {
							String key = line.substring(0, eq);
							if (line.length() > eq + 1) {
								String value = line.substring(eq + 1).trim();
								properties.put(key, value);
							}
						}
						arguments.add(line);
					}
					line = bufferedReader.readLine();
				}
				fgProperties.put(eeFile, properties);
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
	 * Replaces and returns a string with all occurrences of
	 * "${ee.home} replaced with 'eeHome'.
	 * 
	 * @param value string to process
	 * @param eeHome replacement string
	 * @return resolved paths
	 */
	private static String resolve(String value, String eeHome) {
		int start = 0;
		int index = value.indexOf(VAR_EE_HOME, start);
		StringBuffer replaced = null;
		while (index >= 0) {
			if (replaced == null) {
				replaced = new StringBuffer();
			}
			replaced.append(value.substring(start, index));
			replaced.append(eeHome);
			start = index + VAR_EE_HOME.length();
			index = value.indexOf(VAR_EE_HOME, start);
		}
		if (replaced != null) {
			replaced.append(value.substring(start));
			return replaced.toString();
		}
		return value;
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

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractVMInstallType#doCreateVMInstall(java.lang.String)
	 */
	protected IVMInstall doCreateVMInstall(String id) {
		return new EEVMInstall(this, id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#detectInstallLocation()
	 */
	public File detectInstallLocation() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#getDefaultLibraryLocations(java.io.File)
	 */
	public LibraryLocation[] getDefaultLibraryLocations(File installLocationOrDefinitionFile) {
		return new LibraryLocation[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#getName()
	 */
	public String getName() {
		return LaunchingMessages.EEVMType_2;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#validateInstallLocation(java.io.File)
	 */
	public IStatus validateInstallLocation(File installLocation) {
		if (installLocation.exists()) {
			return Status.OK_STATUS;
		}
		return new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, MessageFormat.format(LaunchingMessages.EEVMType_3, new String[]{installLocation.getPath()}));
	}
	
	/**
	 * Clears any cached properties for the given file.
	 * 
	 * @param eeFile
	 */
	public synchronized static void clearProperties(File eeFile) {
		fgProperties.remove(eeFile);
	}
}
