package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */ 

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;

/**
 * This interface is implemented by clients of the <code>AddVMDialog</code>.
 */
public interface IAddVMDialogRequestor {

	/**
	 * Reply whether or not a new VM of the specified name and type would
	 * constitute a duplicate.
	 * 
	 * @param type the type of a potential new VM
	 * @param name the name of a potential new VM
	 * @return whether a new VM of the specified type and name would be a duplicate VM
	 */
	public boolean isDuplicateName(IVMInstallType type, String name);
	
	/**
	 * Notification that a VM has been added from the <code>AddVMDialog</code>.
	 * 
	 * @param vm the added vm
	 */
	public void vmAdded(IVMInstall vm);
	
}
