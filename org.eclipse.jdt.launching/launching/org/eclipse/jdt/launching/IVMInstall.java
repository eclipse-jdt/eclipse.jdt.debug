/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package org.eclipse.jdt.launching;import java.io.File;import org.eclipse.core.runtime.IPath;

/**
 * Represents a particular installation of a VM. A VM instance holds all parameters
 * specific to a VM installation. Unlike VM types, VM instances can be created and
 * configured dynamically at run-time. This is typically done by the user 
 * interactively in the UI.
 * <p>
 * Instance are factories for creating VM runners for actually running Java programs
 * in the various different run modes.
 * </p>
 * <p>
 * This interface is intended to be implemented by clients that contribute
 * to the <code>"org.eclipse.jdt.launching.vmType"</code> extension point.
 * </p>
 */
public interface IVMInstall {
	/**
	 * Returns a VM runner that runs this installed VM in the given mode.
	 * 
	 * @param mode the mode the VM should be launched in; one of the constants
	 *   declared in <code>org.eclipse.debug.core.ILaunchManager</code>
	 * @return 	a VMRunner for a given mode May return null if the given mode
	 * 			is not supported by this VM.
	 * @see org.eclipse.debug.core.ILaunchManager
	 */
	IVMRunner getVMRunner(String mode);
	/**
	 * Returns the id for this VM. VM ids are unique within the VMs 
	 * of a given VM type. The VM id is not intended to be presented to users.
	 * 
	 * @return the VM identifier. Must not return null.
	 */
	String getId();
	/**
	 * Returns the display name of this VM.
	 * The VM name is intended to be presented to users.
	 * 
	 * @return the display name of this VM. May return null.
	 */
	String getName();
	/**
	 * Sets the display name of this VM.
	 * The VM name is intended to be presented to users.
	 * 
	 * @param name the display name of this VM
	 */
	void setName(String name);
	/**
	 * Returns the root directory of the install location of this VM.
	 * 
	 * @return the root directory of this VM installation. May
	 * 			return null.
	 */
	File getInstallLocation();
	/**
	 * Sets the root directory of the install location of this VM.
	 * 
	 * @param installLocation the root directory of this VM installation
	 */
	void setInstallLocation(File installLocation);
		
	/**
	 * Returns the VM type of this VM.
	 * 
	 * @return the VM type that created this IVMInstall instance
	 */
	IVMInstallType getVMInstallType();
	
	/**
	 * Returns the library locations of this IVMInstall.
	 * @see IVMInstall#setLibraryLocations(LibraryLocation[])
	 * @return 	The library locations of this IVMInstall.
	 * 			Returns null when not set or previously set to null.
	 * @since 2.0
	 */
	LibraryLocation[] getLibraryLocations();	
	
	/**
	 * Sets the library locations of this IVMInstall.
	 * @param	locations The <code>LibraryLocation</code>s to associate
	 * 			with this IVMInstall.
	 * 			May be null to clear the property.
	 * @since 2.0
	 */
	void setLibraryLocations(LibraryLocation[] locations);	
}
