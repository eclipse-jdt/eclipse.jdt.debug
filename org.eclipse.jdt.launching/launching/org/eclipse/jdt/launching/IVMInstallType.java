/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */ 

package org.eclipse.jdt.launching;import java.io.File;import org.eclipse.core.runtime.IPath;import org.eclipse.core.runtime.IStatus;

/**
 * Represents a particular type of VM for which there may be
 * any number of VM installations. An example of a VM type
 * might be the standard JRE which might have instances corresponding 
 * to different installed versions such as JRE 1.2.2 and
 * JRE 1.3.
 * <p>
 * This interface is intended to be implemented by clients that contribute
 * to the <code>"org.eclipse.jdt.launching.vmType"</code> extension point.
 * </p>
 * 
 * @see	IVMInstall
 */
public interface IVMInstallType {
	/**
	 * Creates a new instance of this VM Install type.
	 * The newly created IVMInstall is managed by this IVMInstallType.
	 * 
	 * @param	id	An id String that must be unique within this IVMInstallType.
	 * 
	 * @return the newly created VM instance
	 * 
	 * @throws	IllegalArgumentException	If the id exists already.
	 */
	IVMInstall createVMInstall(String id);
	/**
	 * Finds the VM with the given id.
	 * 
	 * @param id the VM id
	 * @return a VM instance, or <code>null</code> if not found
	 */
	IVMInstall findVMInstall(String id);
	
	/**
	 * Remove the given VM from the set of VMs managed by this VM type.
	 * Does nothing if the given VM is not managed to this type.
	 * A IVMInstall that is disposed may not be used anymore.
	 * 
	 * @param vm the VM to be disposed.
	 */
	void disposeVMInstall(String id);
	/**
	 * Returns all VM instances managed by this VM type.
	 * 
	 * @return the list of VM instances managed by this VM type
	 */
	IVMInstall[] getVMInstalls();
	/**
	 * Returns the display name of this VM type.
	 * 
	 * @return the name of this IVMInstallType
	 */ 
	String getName();
	
	/**
	 * Returns the globally unique id of this VM type.
	 * Clients are reponsible for providing a unique id.
	 * 
	 * @return the id of this IVMInstallType
	 */ 
	String getId();
	/**
	 * Validates the given location of a VM installation.
	 * <p>
	 * For example, an implementation might check whether the VM executeable 
	 * is present.
	 * </p>
	 * 
	 * @param installLocation the root directory of a potential installation for
	 *   this type of VM
	 * @return a status object describing whether the install location is valid
	 */
	IStatus validateInstallLocation(File installLocation);
	
	/**
	 * Tries to detect an installed VM that matches this IVMInstallType
	 * Typically, this method will detect the VM installation the
	 * platform runs on. Implementers should return null if they
	 * can't assure that a given vm install matches this IVMInstallType.
	 * @return The location of an VM installation that can be used
	 * 			with this IVMInstallType.
	 */
	File detectInstallLocation();
	
	/**
	 * Must return a <code>LibraryLocation</code> that represents the
	 * default configuration of this IVMInstallType if the VM was installed
	 * at <code>installLocation</code>.
	 * The returned <code>LibraryLocation</code> doesn't have to contain
	 * existing files, if the <code>installLocation</code> is not a valid install 
	 * location.
	 * 
	 * @see	LibraryLocation
	 * @see IVMInstallType#validateInstallLocation(File)
	 * 
	 * @return A default library location based on the given <code>installLocation</code>.
	 */
	LibraryLocation getDefaultLibraryLocation(File installLocation);
}
