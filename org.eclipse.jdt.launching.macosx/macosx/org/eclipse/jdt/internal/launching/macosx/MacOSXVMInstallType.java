/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.launching.macosx;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.LibraryLocation;

/**
 * This plugins into the org.eclipse.jdt.launching.vmInstallTypes extension point
 */
public class MacOSXVMInstallType extends AbstractVMInstallType {

	public IVMInstall doCreateVMInstall(String id) {
		return new MacOSXVMInstall(this, id);
	}
	
	public String getName() {
		return MacOSXLauncherMessages.getString("MacOSXVMType.name"); //$NON-NLS-1$
	}
	
	public IStatus validateInstallLocation(File installLocation) {
		File java= new File(installLocation, "bin"+File.separator+"java"); //$NON-NLS-2$ //$NON-NLS-1$
		if (!java.isFile()) {
			return new Status(IStatus.ERROR, MacOSXLaunchingPlugin.getUniqueIdentifier(), 0, MacOSXLauncherMessages.getString("MacOSXVMType.error.notRoot"), null); //$NON-NLS-1$
		}
		return new Status(IStatus.OK, MacOSXLaunchingPlugin.getUniqueIdentifier(), 0, "ok", null); //$NON-NLS-1$
	}

	/**
	 * @see IVMInstallType#detectInstallLocation()
	 */
	public File detectInstallLocation() {
		if (!"Java HotSpot(TM) Client VM".equals(System.getProperty("java.vm.name"))) //$NON-NLS-2$ //$NON-NLS-1$
			return null;
			
		String javaHome= System.getProperty("java.home");	//$NON-NLS-1$
		if (javaHome != null) {
			File home = new File(javaHome); 
			if (home.exists())
				return home;
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
