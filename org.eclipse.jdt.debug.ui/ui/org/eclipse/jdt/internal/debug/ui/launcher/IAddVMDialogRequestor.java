package org.eclipse.jdt.internal.debug.ui.launcher;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/ 

import org.eclipse.jdt.launching.IVMInstall;

/**
 * This interface is implemented by clients of the <code>AddVMDialog</code>.
 */
public interface IAddVMDialogRequestor {

	/**
	 * Reply whether or not a new VM of the specified name would
	 * constitute a duplicate.
	 * 
	 * @param name the name of a potential new VM
	 * @return whether a new VM with the specified name would be a duplicate VM
	 */
	public boolean isDuplicateName(String name);
	
	/**
	 * Notification that a VM has been added from the <code>AddVMDialog</code>.
	 * 
	 * @param vm the added vm
	 */
	public void vmAdded(IVMInstall vm);
	
}
