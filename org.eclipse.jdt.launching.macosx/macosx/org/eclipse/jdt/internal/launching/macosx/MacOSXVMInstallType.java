/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.launching.macosx;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

/**
 * This plugins into the org.eclipse.jdt.launching.vmInstallTypes extension point
 */
public class MacOSXVMInstallType extends AbstractVMInstallType {
	
	private static final String JVM_VERSION_LOC= "/System/Library/Frameworks/JavaVM.framework/Versions/";	//$NON-NLS-1$
	private static final String CURRENT_JVM= "CurrentJDK";	//$NON-NLS-1$
	private static final String JVM_SUBDIR= "Home";	//$NON-NLS-1$
	private static final String JAVADOC_LOC= "/Developer/Documentation/Java/Reference/api";	//$NON-NLS-1$
	
	private String fDefaultJDKID;
	
	public IVMInstall doCreateVMInstall(String id) {
		MacOSXVMInstall vm= new MacOSXVMInstall(this, id);
		String path= JVM_VERSION_LOC + id + File.separator + JVM_SUBDIR;
		File home = new File(path);
		vm.setInstallLocation(home);
		String name= "JVM " + id;
		if (id.equals(fDefaultJDKID))
			name +=  " (MacOS X Default)";
		vm.setName(name);
		vm.setLibraryLocations(getDefaultLibraryLocations(home));
		try {
			URL doc= new URL("file", "", JAVADOC_LOC);	//$NON-NLS-1$ //$NON-NLS-2$
			vm.setJavadocLocation(doc);
		} catch (MalformedURLException ex) {
		}
		return vm;
	}
	
	public String getName() {
		return MacOSXLauncherMessages.getString("MacOSXVMType.name"); //$NON-NLS-1$
	}
	
	public IStatus validateInstallLocation(File installLocation) {
		String id= MacOSXLaunchingPlugin.getUniqueIdentifier();
		File java= new File(installLocation, "bin"+File.separator+"java"); //$NON-NLS-2$ //$NON-NLS-1$
		if (java.isFile())
			return new Status(IStatus.OK, id, 0, "ok", null); //$NON-NLS-1$
		return new Status(IStatus.ERROR, id, 0, MacOSXLauncherMessages.getString("MacOSXVMType.error.notRoot"), null); //$NON-NLS-1$
	}

	/**
	 * @see IVMInstallType#detectInstallLocation()
	 */
	public File detectInstallLocation() {
		
		if (!"Java HotSpot(TM) Client VM".equals(System.getProperty("java.vm.name"))) //$NON-NLS-2$ //$NON-NLS-1$
			return null;
	
		// find all installed VMs
		File versionDir= new File(JVM_VERSION_LOC);
		if (versionDir.exists() && versionDir.isDirectory()) {
			File currentJDK= new File(versionDir, CURRENT_JVM);
			try {
				currentJDK= currentJDK.getCanonicalFile();
			}catch (IOException ex) {
			}
			File[] versions= versionDir.listFiles();
			for (int i= 0; i < versions.length; i++) {
				String version= versions[i].getName();
				File home=  new File(versions[i], JVM_SUBDIR);
				if (home.exists() && findVMInstall(version) == null && !CURRENT_JVM.equals(version)) {
					if (currentJDK.equals(versions[i])) {
						if (fDefaultJDKID == null)
							fDefaultJDKID= version;
						createVMInstall(version);							
						IVMInstall vm= findVMInstall(version);
						if (vm != null) {
							try {
								JavaRuntime.setDefaultVMInstall(vm, null);
							} catch (CoreException e) {
							}
						}
					} else
						createVMInstall(version);
				}
			}
		}
		return null;
	}

	/**
	 * @see IVMInstallType#getDefaultSystemLibraryDescription(File)
	 */
	public LibraryLocation[] getDefaultLibraryLocations(File installLocation) {
		IPath libHome= new Path(installLocation.toString()); //$NON-NLS-1$
		libHome= libHome.append(".."); //$NON-NLS-1$
		libHome= libHome.append("Classes"); //$NON-NLS-1$
		IPath lib= libHome.append("classes.jar"); //$NON-NLS-1$
		IPath uilib= libHome.append("ui.jar"); //$NON-NLS-1$
		
		IPath source= new Path(installLocation.toString());
		source= source.append("src.jar"); //$NON-NLS-1$
		
		IPath srcPkgRoot= new Path("src"); //$NON-NLS-1$
		
		return new LibraryLocation[] {
				new LibraryLocation(lib, source, srcPkgRoot),
				new LibraryLocation(uilib, source, srcPkgRoot)
		};
	}

}
