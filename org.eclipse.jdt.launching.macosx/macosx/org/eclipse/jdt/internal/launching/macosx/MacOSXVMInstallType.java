/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeff Myers myersj@gmail.com - fix for #75201
 *     Ralf Ebert ralf@ralfebert.de - fix for #307109
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
import org.eclipse.jdt.internal.launching.MacInstalledJREs;
import org.eclipse.jdt.internal.launching.MacInstalledJREs.JREDescriptor;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.osgi.util.NLS;

/**
 * This class provides the implementation of the {@link IVMInstallType} for Mac OSX.
 * 
 * The default VM locations are outlined below. each VM except for developer VMs provide links in the 
 * <code>/System/Library/Frameworks/JavaVM.framework/Versions/</code> folder, with a link named 
 * <code>CurrentJDK</code> that points to the VM you have set using the Java preference tool in the system preferences.
 * <br><br>
 * The directory structure for Java VMs prior to Snow Leopard is as follows:
 * <pre>
 * /System/Library/Frameworks/JavaVM.framework/Versions/
 *   1.3.1/
 *     Classes/
 *       classes.jar
 *       ui.jar
 *     Home/
 *       src.jar
 * </pre>
 * 
 * The directory structure for developer VMs is:
 * <pre>
 * /Library/Java/JavaVirtualMachines/
 *   1.7.0.jdk/
 *     Contents/
 *       Home/
 *         src.zip
 * </pre>
 * 
 * The directory structure for  Snow Leopard and Lion VMs is:
 * <pre>
 * /System/Library/Java/JavaVirtualMachines/
 *   1.6.0.jdk/
 *     Contents/
 *       Home/
 *         src.zip
 * </pre>
 * @see http://developer.apple.com/library/mac/#releasenotes/Java/JavaSnowLeopardUpdate3LeopardUpdate8RN/NewandNoteworthy/NewandNoteworthy.html#//apple_ref/doc/uid/TP40010380-CH4-SW1
 */
public class MacOSXVMInstallType extends StandardVMType {
	
	/** The OS keeps all the JVM versions in this directory */
	private static final String JVM_VERSION_LOC= "/System/Library/Frameworks/JavaVM.framework/Versions/";	//$NON-NLS-1$
	private static final File JVM_VERSIONS_FOLDER= new File(JVM_VERSION_LOC);
	/** The name of a Unix link to MacOS X's default VM */
	private static final String CURRENT_JDK= "CurrentJDK";	//$NON-NLS-1$
	/** The root of a JVM */
	private static final String JVM_HOME= "Home";	//$NON-NLS-1$
	/** The doc (for all JVMs) lives here (if the developer kit has been expanded)*/
	private static final String JAVADOC_LOC= "/Developer/Documentation/Java/Reference/";	//$NON-NLS-1$
	/** The doc for 1.4.1 is kept in a sub directory of the above. */ 
	private static final String JAVADOC_SUBDIR= "/doc/api";	//$NON-NLS-1$
				
	@Override
	public String getName() {
		return MacOSXLaunchingPlugin.getString("MacOSXVMType.name"); //$NON-NLS-1$
	}
	
	@Override
	public IVMInstall doCreateVMInstall(String id) {
		return new MacOSXVMInstall(this, id);
	}
			
	/*
	 * @see IVMInstallType#detectInstallLocation()
	 */
	@Override
	public File detectInstallLocation() {
		try {
			// find all installed VMs
			File defaultLocation= null;
			JREDescriptor[] jres= new MacInstalledJREs().getInstalledJREs();
			for (int i= 0; i < jres.length; i++) {
				JREDescriptor descripor = jres[i];
				String name = jres[i].getName();
				File home= descripor.getHome();
				String id= descripor.getId();
				if (home.exists()) {
					boolean isDefault= i == 0;
					IVMInstall install= findVMInstall(id);
					if (install == null) {
						VMStandin vm= new VMStandin(this, id);
						vm.setInstallLocation(home);
						String format= MacOSXLaunchingPlugin.getString(isDefault
								? "MacOSXVMType.jvmDefaultName"		//$NON-NLS-1$
										: "MacOSXVMType.jvmName");				//$NON-NLS-1$
										vm.setName(MessageFormat.format(format, new Object[] { name } ));
						vm.setLibraryLocations(getDefaultLibraryLocations(home));
						vm.setJavadocLocation(getDefaultJavadocLocation(home));
						install= vm.convertToRealVM();
					}
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
			return defaultLocation;
		} catch (CoreException e) {
			MacOSXLaunchingPlugin.getDefault().getLog().log(e.getStatus());
			return detectInstallLocationOld();
		}
	}

	/**
	 * The proper way to find installed JREs is to parse the XML output produced from "java_home -X"
	 * (see bug 325777). However, if that fails, revert to the hard coded search. 
	 * 
	 * @return file that points to the default JRE install
	 */
	private File detectInstallLocationOld() {
		
		String javaVMName= System.getProperty("java.vm.name");	//$NON-NLS-1$
		if (javaVMName == null) {
			return null;
		}

		if (!JVM_VERSIONS_FOLDER.exists() || !JVM_VERSIONS_FOLDER.isDirectory()) {
			String message= NLS.bind(MacOSXLaunchingPlugin.getString("MacOSXVMType.error.jvmDirectoryNotFound"), JVM_VERSIONS_FOLDER);  //$NON-NLS-1$
			LaunchingPlugin.log(message);
			return null;
		}

		// find all installed VMs
		File defaultLocation= null;
		File[] versions= getAllVersionsOld();
		File currentJDK= getCurrentJDKOld();
		for (int i= 0; i < versions.length; i++) {
			File versionFile= versions[i];
			String version= versionFile.getName();
			File home= new File(versionFile, JVM_HOME);
			if (home.exists()) {
				boolean isDefault= currentJDK.equals(versionFile);
				IVMInstall install= findVMInstall(version);
				if (install == null) {
					VMStandin vm= new VMStandin(this, version);
					vm.setInstallLocation(home);
					String format= MacOSXLaunchingPlugin.getString(isDefault
												? "MacOSXVMType.jvmDefaultName"		//$NON-NLS-1$
												: "MacOSXVMType.jvmName");				//$NON-NLS-1$
					vm.setName(MessageFormat.format(format, new Object[] { version } ));
					vm.setLibraryLocations(getDefaultLibraryLocations(home));
					vm.setJavadocLocation(getDefaultJavadocLocation(home));
					
					install= vm.convertToRealVM();
				}
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
		return defaultLocation;
	}
	
	/**
	 * The proper way to find installed JREs is to parse the XML output produced from "java_home -X"
	 * (see bug 325777). However, if that fails, revert to the hard coded search. 
	 * 
	 * @return array of files that point to JRE install directories
	 */
	private File[] getAllVersionsOld() {
		File[] versionFiles= JVM_VERSIONS_FOLDER.listFiles();
		for (int i= 0; i < versionFiles.length; i++) {
			versionFiles[i]= resolveSymbolicLinks(versionFiles[i]);
		}
		return versionFiles;
	}

	/**
	 * The proper way to find the default JRE is to parse the XML output produced from "java_home -X"
	 * and take the first entry in the list. However, if that fails, revert to the hard coded search.
	 * 
	 * @return a file that points to the default JRE install directory
	 */
	private File getCurrentJDKOld() {
		return resolveSymbolicLinks(new File(JVM_VERSIONS_FOLDER, CURRENT_JDK));
	}
	
	private File resolveSymbolicLinks(File file) {
		try {
			return file.getCanonicalFile();
		} catch (IOException ex) {
			return file;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.launching.StandardVMType#getDefaultLibraryInfo(java.io.File)
	 */
	@Override
	protected LibraryInfo getDefaultLibraryInfo(File installLocation) {
		File classes = new File(installLocation, "../Classes"); //$NON-NLS-1$
		File lib1= new File(classes, "classes.jar"); //$NON-NLS-1$
		File lib2= new File(classes, "ui.jar"); //$NON-NLS-1$
		
		String[] libs = new String[] { lib1.toString(),lib2.toString() };
		
		File lib = new File(installLocation, "lib"); //$NON-NLS-1$
		File extDir = new File(lib, "ext"); //$NON-NLS-1$
		String[] dirs = null;
		if (extDir.exists())
			dirs = new String[] {extDir.getAbsolutePath()};
		else
			dirs = new String[0];

		File endDir = new File(lib, "endorsed"); //$NON-NLS-1$
		String[] endDirs = null;
		if (endDir.exists())
			endDirs = new String[] {endDir.getAbsolutePath()};
		else
			endDirs = new String[0]; 
		
		return new LibraryInfo("???", libs, dirs, endDirs);		 //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.launching.StandardVMType#getDefaultSystemLibrarySource(java.io.File)
	 */
	@Override
	protected IPath getDefaultSystemLibrarySource(File libLocation) {
		File parent= libLocation.getParentFile();
		while (parent != null) {
			File home= new File(parent, JVM_HOME);
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
		return Path.EMPTY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.launching.StandardVMType#validateInstallLocation(java.io.File)
	 */
	@Override
	public IStatus validateInstallLocation(File javaHome) {
		String id= MacOSXLaunchingPlugin.getUniqueIdentifier();
		File java= new File(javaHome, "bin"+File.separator+"java"); //$NON-NLS-2$ //$NON-NLS-1$
		if (java.isFile())
			return new Status(IStatus.OK, id, 0, "ok", null); //$NON-NLS-1$
		return new Status(IStatus.ERROR, id, 0, MacOSXLaunchingPlugin.getString("MacOSXVMType.error.notRoot"), null); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.launching.StandardVMType#getDefaultJavadocLocation(java.io.File)
	 */
	@Override
	public URL getDefaultJavadocLocation(File installLocation) {
		// try in local filesystem
		String id= null;	
		try {
			String post= File.separator + JVM_HOME;
			String path= installLocation.getCanonicalPath();
			if (path.startsWith(JVM_VERSION_LOC) && path.endsWith(post))
				id= path.substring(JVM_VERSION_LOC.length(), path.length()-post.length());
		} catch (IOException ex) {
			// we use the fall back from below
		}
		if (id != null) {
			String s= JAVADOC_LOC + id + JAVADOC_SUBDIR;
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

	/*
	 * Overridden to make it visible.
	 */
	@Override
	protected String getVMVersion(File javaHome, File javaExecutable) {
		return super.getVMVersion(javaHome, javaExecutable);
	}
}
