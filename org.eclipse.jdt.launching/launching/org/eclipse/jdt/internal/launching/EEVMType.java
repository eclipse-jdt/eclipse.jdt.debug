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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	 * Map of {EE File -> String containing vm arguments}
	 */
	private static Map fgArguments = new HashMap();
	
	public static final String PROP_ENDORSED_DIRS = "-Dee.endorsed.dirs";  //$NON-NLS-1$
	public static final String PROP_BOOT_CLASS_PATH = "-Dee.bootclasspath";  //$NON-NLS-1$
	public static final String PROP_SOURCE_DEFAULT = "-Dee.src";  //$NON-NLS-1$
	public static final String PROP_SOURCE_MAP = "-Dee.src.map";  //$NON-NLS-1$
	public static final String PROP_JAVADOC_LOC = "-Dee.javadoc";  //$NON-NLS-1$
	public static final String PROP_ADDITIONAL_DIRS = "-Dee.additional.dirs";  //$NON-NLS-1$
	public static final String PROP_EXTENSION_DIRS = "-Dee.ext.dirs";  //$NON-NLS-1$
	public static final String PROP_LANGUAGE_LEVEL = "-Dee.language.level";  //$NON-NLS-1$
	public static final String PROP_CLASS_LIB_LEVEL = "-Dee.class.library.level";  //$NON-NLS-1$
	public static final String PROP_EXECUTABLE = "-Dee.executable";  //$NON-NLS-1$
	public static final String PROP_EXECUTABLE_CONSOLE = "-Dee.executable.console";  //$NON-NLS-1$
	public static final String PROP_JAVA_HOME = "-Djava.home";  //$NON-NLS-1$
	public static final String PROP_DEBUG_ARGS = "-Dee.debug.args";  //$NON-NLS-1$
	public static final String PROP_NAME = "-Dee.name";  //$NON-NLS-1$
	
	// Regex constants for handling the source mapping
	public static final Character WILDCARD_SINGLE_CHAR = new Character('?');
	public static final Character WILDCARD_MULTI_CHAR = new Character('*');
	public static final String REGEX_SPECIAL_CHARS = "+()^$.{}[]|\\"; //$NON-NLS-1$
	
	/**
	 * Any line found in the ee file starting with this string will not be added to the vm argument list
	 */
	private static final String EE_ARG_FILTER = "-Dee."; //$NON-NLS-1$
	
	/**
	 * Substitution in EE file - replaced with directory of EE file,
	 * to support absolute path names where needed.
	 */
	public static final String VAR_EE_HOME = "${ee.home}"; //$NON-NLS-1$
	
	private static final String[] REQUIRED_PROPERTIES = new String[]{PROP_EXECUTABLE, PROP_BOOT_CLASS_PATH, PROP_LANGUAGE_LEVEL, PROP_JAVA_HOME};

	/**
	 * Returns the javadoc location specified in the definition file or <code>null</code>
	 * if none.
	 * 
	 * @param eeFile definition file
	 * @return vm name specified in the definition file or <code>null</code> if none
	 */
	public static String getVMName(File eeFile) {
		String name = getProperty(PROP_NAME, eeFile);
		if (name == null){
			name = eeFile.getName();
		}
		return name;
	}

	
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
			IPath src = getSourceLocation(eeFile);
			URL url = getJavadocLocation(eeFile);
			for (int i = 0; i < bootpath.length; i++) {
				IPath path = new Path(bootpath[i]);
				File lib = path.toFile(); 
				if (lib.exists() && lib.isFile()) {
					LibraryLocation libraryLocation = new LibraryLocation(path,	src, Path.EMPTY, url);
					boot.add(libraryLocation);
				}
			}
			allLibs.addAll(boot);
		}
		
		// Add all additional libraries
		dirs = getProperty(PROP_ADDITIONAL_DIRS, eeFile);
		if (dirs != null) {
			allLibs.addAll(StandardVMType.gatherAllLibraries(resolvePaths(dirs, eeFile)));
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
				liter.remove();
			}
		}
		
		// If the ee.src.map property is specified, use it to associate source locations with the libraries
		addSourceLocationsToLibraries(getSourceMap(eeFile), allLibs);
		
		return (LibraryLocation[])allLibs.toArray(new LibraryLocation[allLibs.size()]);
	}
	
	
	/**
	 * Returns the location of the source archive for the given ee file or the empty
	 * path if none.
	 * 
	 * @param eeFile property file
	 * @return source archive location or Path.EMPTY if none
	 */
	protected static IPath getSourceLocation(File eeFile) {
		String src = getProperty(PROP_SOURCE_DEFAULT, eeFile);
		if (src != null) {
			src = makePathAbsolute(src, new Path(eeFile.getParentFile().getAbsolutePath()));
			return new Path(src);
		}
		return Path.EMPTY;
	}
	
	
	/**
	 * Uses the given src map to find source libraries that are associated with the
	 * library locations in the list.  The library locations are updated with the
	 * found source path.
	 * 
	 * @param srcMap mapping of library location regexs to source location regexs
	 * @param libraries list of (@link LibraryLocation} objects to update with source locations
	 * @see #getSourceMap(File)
	 */
	private static void addSourceLocationsToLibraries(Map srcMap, List libraries){
		for (Iterator patternIterator = srcMap.keySet().iterator(); patternIterator.hasNext();) {
			// Try each library regex pattern and see what libraries apply.
			String currentKey = (String) patternIterator.next();
			Pattern currentPattern = Pattern.compile(currentKey);
			Matcher matcher = currentPattern.matcher(""); //$NON-NLS-1$
			for (Iterator locationIterator = libraries.iterator(); locationIterator.hasNext();) {
				LibraryLocation currentLibrary = (LibraryLocation) locationIterator.next();
				matcher.reset(currentLibrary.getSystemLibraryPath().toOSString());
				if (matcher.find()){
					// Found a file that the pattern applies to, use the map to get the source location
					String sourceLocation = matcher.replaceAll((String)srcMap.get(currentKey));
					IPath sourcePath = new Path(sourceLocation);
					// Only add the source archive if it exists
					if (sourcePath.toFile().exists()){
						currentLibrary.setSystemLibrarySource(sourcePath);
					}
					
				}
			}
		}
	}
	
	/**
	 * Creates a map (regex string to regex string) mapping library locations to their
	 * source locations.  This is done by taking the ee.src.map property from the ee file
	 * which allows a list of mappings that can use the wildcards ? (any one char) and *
	 * (any series of chars).  The property is converted to a map of regex strings used by 
	 * {@link #addSourceLocationsToLibraries(Map, List)}.
	 * <pre>
	 * Example property, separated onto separate lines for easier reading
	 * -Dee.src.map=${ee.home}\lib\charconv?.zip=lib\charconv?-src.zip;
	 *              ${ee.home}\lib\jclDEE\classes.zip=lib\jclDEE\source\source.zip;
	 *              ${ee.home}\lib\jclDEE\*.zip=lib\jclDEE\source\*-src.zip;
	 *              ${ee.home}\lib\jclDEE\ext\*.???=lib\jclDEE\source\*-src.???;
	 * </pre>
	 * 
	 * 
	 * @param eeFile file to load the map property from
	 * @return map containing regexs mapping library locations to their source locations
	 */
	private static Map getSourceMap(File eeFile){
		String srcMapString = getProperty(PROP_SOURCE_MAP, eeFile);
		Map srcMap = new HashMap();
		if (srcMapString != null){
			// Entries must be separated by the file separator and have an equals splitting the lib location from the src location
			String[] entries = srcMapString.split(File.pathSeparator);
			for (int i = 0; i < entries.length; i++) {
				int index = entries[i].indexOf('=');
				if (index > 0 && index < entries[i].length()-1){
					IPath root = new Path(eeFile.getParentFile().getAbsolutePath());
					String key = entries[i].substring(0,index);
					String value = entries[i].substring(index+1);
					key = makePathAbsolute(key, root);
					value = makePathAbsolute(value, root);
					
					List wildcards = new ArrayList();
					StringBuffer keyBuffer = new StringBuffer();
				    char [] chars = key.toCharArray();
				    // Convert lib location to a regex, replace wildcards with grouped equivalents, keep track of used wildcards, allow '\' and '/' to be used, escape special chars
					for (int j = 0; j < chars.length; j++) {
						if (chars[j] == WILDCARD_MULTI_CHAR.charValue()) {
							wildcards.add(WILDCARD_MULTI_CHAR);
							keyBuffer.append("(.*)"); //$NON-NLS-1$
						} else if (chars[j] == WILDCARD_SINGLE_CHAR.charValue()) {
							wildcards.add(WILDCARD_SINGLE_CHAR);
							keyBuffer.append("(.)"); //$NON-NLS-1$
						} else if (REGEX_SPECIAL_CHARS.indexOf(chars[j]) != -1) {
							keyBuffer.append('\\').append(chars[j]);
						} else {
						    keyBuffer.append(chars[j]);
						}
					}
					
					int currentWild = 0;
					StringBuffer valueBuffer = new StringBuffer();
					chars = value.toCharArray();
					// Convert src location to a regex, replace wildcards with their group number, allow '\' and '/' to be used, escape special chars
					for (int j = 0; j < chars.length; j++) {
						if (chars[j] == WILDCARD_MULTI_CHAR.charValue() || chars[j] == WILDCARD_SINGLE_CHAR.charValue()) {
							if (currentWild < wildcards.size()){
								Character wild = (Character)wildcards.get(currentWild);
								if (chars[j] == wild.charValue()) {
									valueBuffer.append('$').append(currentWild+1);
									currentWild++;
								} else {
									LaunchingPlugin.log(MessageFormat.format(LaunchingMessages.EEVMType_5, new String[]{entries[i]}));
									break;
								}
							} else {
								LaunchingPlugin.log(MessageFormat.format(LaunchingMessages.EEVMType_5, new String[]{entries[i]}));
								break;
							}
						} else if (REGEX_SPECIAL_CHARS.indexOf(chars[j]) != -1) {
							valueBuffer.append('\\').append(chars[j]);
						} else {
							valueBuffer.append(chars[j]);
						}
					}
					
					srcMap.put(keyBuffer.toString(), valueBuffer.toString());
					
				} else {
					LaunchingPlugin.log(MessageFormat.format(LaunchingMessages.EEVMType_6, new String[]{entries[i]}));
				}
			}
		}
		return srcMap;
	}
	
	
	/**
	 * Returns the javadoc location specified in the definition file or <code>null</code>
	 * if none.
	 * 
	 * @param eeFile definition file
	 * @return javadoc location specified in the definition file or <code>null</code> if none
	 */
	public static URL getJavadocLocation(File eeFile) {
		if (hasProperty(PROP_JAVADOC_LOC, eeFile)){
			String javadoc = getProperty(PROP_JAVADOC_LOC, eeFile);
			if (javadoc != null){
				try{
					URL url = new URL(javadoc);
					if ("file".equalsIgnoreCase(url.getProtocol())){ //$NON-NLS-1$
						File file = new File(url.getFile());
						url = file.getCanonicalFile().toURL();
					}
					return url;
				} catch (MalformedURLException e){
					LaunchingPlugin.log(e);
					return null;
				} catch (IOException e){
					LaunchingPlugin.log(e);
					return null;
				}
			}
			return null;
		}
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
		getProperties(eeFile);  // Make sure the arguments have been loaded from the file.
		return (String)fgArguments.get(eeFile);
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
				StringBuffer arguments = new StringBuffer();
				bufferedReader = new BufferedReader(reader);
				String line = bufferedReader.readLine();
				while (line != null) {
					if (!line.startsWith("#")) { //$NON-NLS-1$
						if (line.trim().length() > 0){
							boolean appendArgument = !line.startsWith(EE_ARG_FILTER);
							int eq = line.indexOf('=');
							if (eq > 0) {
								String key = line.substring(0, eq);
								if (appendArgument){
									arguments.append(key).append('=');
								}
								String value = null;
								if (line.length() > eq + 1) {
									value = line.substring(eq + 1).trim();
									value = resolve(value, eeHome);
									if (appendArgument){
										if (value.indexOf(' ') > -1){
											arguments.append('"').append(value).append('"');
										} else {
											arguments.append(value);
										}
									}
								}
								properties.put(key, value);
								if (appendArgument){
									arguments.append(' ');	
								}
							} else {
								if (appendArgument){
									arguments.append(line).append(' ');
								}
							}
						}
					}
					line = bufferedReader.readLine();
				}
				if (arguments.charAt(arguments.length()-1) == ' '){
					arguments.deleteCharAt(arguments.length()-1);
				}
				fgProperties.put(eeFile, properties);
				fgArguments.put(eeFile, arguments.toString());
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
		String[] strings = paths.split(File.pathSeparator, -1);
		IPath root = new Path(eeFile.getParentFile().getAbsolutePath());
		for (int i = 0; i < strings.length; i++) {
			strings[i] = makePathAbsolute(strings[i], root);
		}
		return strings;
	}
	
	/**
	 * Returns a string representing the absolute form of the given path.  If the
	 * given path is not absolute, it is appended to the given root path.  The returned
	 * path will always be the OS specific string form of the path.
	 * 
	 * @param pathString string representing the path to make absolute
	 * @param root root to append non-absolute paths to
	 * @return absolute, OS specific path
	 */
	private static String makePathAbsolute(String pathString, IPath root){
		IPath path = new Path(pathString.trim());
		if (!path.isEmpty() && !path.isAbsolute()) {
			IPath filePath = root.append(path);
			return filePath.toOSString();
		} else {
			return path.toOSString();
		}
	}
	
	/**
	 * Returns the specified property from the given ee property file, or <code>null</code>
	 * if none.  Will also return <code>null</code> if the property exists but has an empty
	 * value in the property file.
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
	
	/**
	 * Returns whether the specified property from the given ee property file exists.
	 * 
	 * @param propertyName key
	 * @param eeFile property file
	 * @return whether the property exists
	 */
	public static boolean hasProperty(String propertyName, File eeFile) {
		Map properties = getProperties(eeFile);
		if (properties != null) {
			return properties.containsKey(propertyName);
		}
		return false;
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
			return new Status(IStatus.INFO, LaunchingPlugin.ID_PLUGIN, LaunchingMessages.EEVMType_4);
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
		fgArguments.remove(eeFile);
	}
}
