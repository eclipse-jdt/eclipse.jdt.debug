/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;


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
