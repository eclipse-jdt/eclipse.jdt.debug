package org.eclipse.jdt.launching;

import org.eclipse.jdt.core.IJavaProject;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A VM install changed listener is notified when the VM install assigned
 * to a project changes, or when the workspace default VM install changes.
 * Listeners register with <code>JavaRuntime</code>.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 2.0
 */
public interface IVMInstallChangedListener {
	
	/**
	 * Notification that the VM install assigned to the given project
	 * has changed.
	 * 
	 * @param project the project for which the VM install assignment
	 *  has changed
	 * @param previous the VM install that was previously assigned
	 * 	to the given project, possibly <code>null</code>
	 * @param current the VM install that is currently assigned to the
	 * 	project, possibly <code>null</code>
	 */
	public void projectVMInstallChanged(IJavaProject project, IVMInstall previous, IVMInstall current);
	
	/**
	 * Notification that the workspace default VM install
	 * has changed.
	 * 
	 * @param previous the VM install that was previously assigned
	 * 	to the workspace, possibly <code>null</code>
	 * @param current the VM install that is currently assigned to the
	 * 	workspace, possibly <code>null</code>
	 */
	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current);	

}
