/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeff Myers myersj@gmail.com - fix for #75201
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.macosx;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.internal.launching.LibraryInfo;
import org.eclipse.jdt.internal.launching.StandardVMType;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;

/**
 * This plugins into the org.eclipse.jdt.launching.vmInstallTypes extension point
 */
public class MacOSXVMInstallType extends StandardVMType {
	
	/*
	 * The directory structure for Java VMs is as follows:
	 * 
	 * 	/System/Library/Frameworks/JavaVM.framework/Versions/
	 * 		1.3.1
	 * 			Classes
	 * 				classes.jar
	 * 				ui.jar
	 * 			Home
	 * 				src.jar
	 * 		1.4.1
	 * 			Classes
	 * 				classes.jar
	 * 				ui.jar
	 * 			Home
	 * 				src.jar
	 * 		CurrentJDK -> 1.3.1
	 */
	 
	private static final String JAVA_VM_NAME= "Java HotSpot(TM) Client VM";	//$NON-NLS-1$
	
	/** The OS keeps all the JVM versions in this directory */
	private static final String JVM_VERSION_LOC= "/System/Library/Frameworks/JavaVM.framework/Versions/";	//$NON-NLS-1$
	/** The name of a Unix link to MacOS X's default VM */
	private static final String CURRENT_JVM= "CurrentJDK";	//$NON-NLS-1$
	/** The root of a JVM */
	private static final String JVM_ROOT= "Home";	//$NON-NLS-1$
	/** The doc (for all JVMs) lives here (if the developer kit has been expanded)*/
	private static final String JAVADOC_LOC= "/Developer/Documentation/Java/Reference/";	//$NON-NLS-1$
	/** The doc for 1.4.1 is kept in a sub directory of the above. */ 
	private static final String JAVADOC_SUBDIR= "/doc/api";	//$NON-NLS-1$
		
				
	public String getName() {
		return MacOSXLaunchingPlugin.getString("MacOSXVMType.name"); //$NON-NLS-1$
	}
	
	public IVMInstall doCreateVMInstall(String id) {
		return new MacOSXVMInstall(this, id);
	}
			
	/*
	 * @see IVMInstallType#detectInstallLocation()
	 */
	public File detectInstallLocation() {
		
		String javaVMName= System.getProperty("java.vm.name");	//$NON-NLS-1$
		if (javaVMName == null || !JAVA_VM_NAME.equals(javaVMName)) 
			return null;

		// find all installed VMs
		File defaultLocation= null;
		File versionDir= new File(JVM_VERSION_LOC);
		if (versionDir.exists() && versionDir.isDirectory()) {
			File currentJDK= new File(versionDir, CURRENT_JVM);
			try {
				currentJDK= currentJDK.getCanonicalFile();
			} catch (IOException ex) {
				// NeedWork
			}
			File[] versions= versionDir.listFiles();
			for (int i= 0; i < versions.length; i++) {
				String version= versions[i].getName();
				File home= new File(versions[i], JVM_ROOT);
				if (home.exists()) {
					boolean isDefault= currentJDK.equals(versions[i]);
					IVMInstall install= findVMInstall(version);
					if (install == null && !CURRENT_JVM.equals(version)) {
						VMStandin vm= new VMStandin(this, version);
						vm.setInstallLocation(home);
						String format= MacOSXLaunchingPlugin.getString(isDefault
													? "MacOSXVMType.jvmDefaultName"		//$NON-NLS-1$
													: "MacOSXVMType.jvmName");				//$NON-NLS-1$
						vm.setName(MessageFormat.format(format, new Object[] { version } ));
						vm.setLibraryLocations(getDefaultLibraryLocations(home));
						URL doc= getDefaultJavadocLocation(home);
						if (doc != null)
							vm.setJavadocLocation(doc);
						
						IVMInstall rvm= vm.convertToRealVM();					
						if (isDefault) {
							defaultLocation= home;
							try {
								JavaRuntime.setDefaultVMInstall(rvm, null);
							} catch (CoreException e) {
								LaunchingPlugin.log(e);
							}
						}
					} else {
						if (isDefault) {
							defaultLocation= home;
							try {
								JavaRuntime.setDefaultVMInstall(install, null);
							} catch (CoreException e) {
								LaunchingPlugin.log(e);
							}
						}
					}
				}
			}
		}
		return defaultLocation;
	}

	/**
	 * Returns default library info for the given install location.
	 * 
	 * @param installLocation
	 * @return LibraryInfo
	 */
	protected LibraryInfo getDefaultLibraryInfo(File installLocation) {

		File classes = new File(installLocation, "../Classes"); //$NON-NLS-1$
		File lib1= new File(classes, "classes.jar"); //$NON-NLS-1$
		File lib2= new File(classes, "ui.jar"); //$NON-NLS-1$
		
		String[] libs = new String[] { lib1.toString(),lib2.toString() };
		
		File lib = new File(installLocation, "lib"); //$NON-NLS-1$
		File extDir = new File(lib, "ext"); //$NON-NLS-1$
		String[] dirs = null;
		if (extDir == null)
			dirs = new String[0];
		else
			dirs = new String[] {extDir.getAbsolutePath()};

		File endDir = new File(lib, "endorsed"); //$NON-NLS-1$
		String[] endDirs = null;
		if (endDir == null)
			endDirs = new String[0]; 
		else
			endDirs = new String[] {endDir.getAbsolutePath()};
		
		return new LibraryInfo("???", libs, dirs, endDirs);		 //$NON-NLS-1$
	}
	
	protected IPath getDefaultSystemLibrarySource(File libLocation) {
		File parent= libLocation.getParentFile();
		while (parent != null) {
			File home= new File(parent, JVM_ROOT);
			File parentsrc= new File(home, "src.jar"); //$NON-NLS-1$
			if (parentsrc.isFile()) {
				setDefaultRootPath("src");//$NON-NLS-1$
				return new Path(parentsrc.getPath());
			}
			parentsrc= new File(home, "src.zip"); //$NON-NLS-1$
			if (parentsrc.isFile()) {
				setDefaultRootPath(""); //$NON-NLS-1$
				return new Path(parentsrc.getPath());
			}
			parent = parent.getParentFile();
		}
		setDefaultRootPath(""); //$NON-NLS-1$
		return Path.EMPTY; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.launching.IVMInstallType#validateInstallLocation(java.io.File)
	 */
	public IStatus validateInstallLocation(File javaHome) {
		String id= MacOSXLaunchingPlugin.getUniqueIdentifier();
		File java= new File(javaHome, "bin"+File.separator+"java"); //$NON-NLS-2$ //$NON-NLS-1$
		if (java.isFile())
			return new Status(IStatus.OK, id, 0, "ok", null); //$NON-NLS-1$
		return new Status(IStatus.ERROR, id, 0, MacOSXLaunchingPlugin.getString("MacOSXVMType.error.notRoot"), null); //$NON-NLS-1$
	}
	
	/**
	 * @see org.eclipse.jdt.launching.AbstractVMInstallType#getDefaultJavadocLocation(java.io.File)
	 */
	public URL getDefaultJavadocLocation(File installLocation) {
		
		// try in local filesystem
		String id= null;	
		try {
			String post= File.separator + JVM_ROOT;
			String path= installLocation.getCanonicalPath();
			if (path.startsWith(JVM_VERSION_LOC) && path.endsWith(post))
				id= path.substring(JVM_VERSION_LOC.length(), path.length()-post.length());
		} catch (IOException ex) {
			// we use the fall back from below
		}
		if (id != null) {
			String s= JAVADOC_LOC + id + JAVADOC_SUBDIR;	//$NON-NLS-1$
			File docLocation= new File(s);
			if (!docLocation.exists()) {
				s= JAVADOC_LOC + id;
				docLocation= new File(s);
				if (!docLocation.exists())
					s= null;
			}
			if (s != null) {
				try {
					return new URL("file", "", s);	//$NON-NLS-1$ //$NON-NLS-2$
				} catch (MalformedURLException ex) {
					// we use the fall back from below
				}
			}
		}
		
		// fall back
		return super.getDefaultJavadocLocation(installLocation);
	}
}
