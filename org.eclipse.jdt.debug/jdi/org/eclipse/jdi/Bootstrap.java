/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi;

import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;


public class Bootstrap
{
	private static com.sun.jdi.VirtualMachineManager fVirtualMachineManager;

	public Bootstrap() { }
	
	public static synchronized com.sun.jdi.VirtualMachineManager virtualMachineManager() {
		if (fVirtualMachineManager != null)
			return fVirtualMachineManager;
		
		try {
			String className= JDIDebugPlugin.getDefault().getDescriptor().getExtensionPoint("jdiclient").getLabel(); //$NON-NLS-1$
			Class clazz= null;
			if (className != null) {
				clazz= Class.forName(className);
			}
			if (clazz != null) {
				fVirtualMachineManager = (com.sun.jdi.VirtualMachineManager)clazz.newInstance();
			}
		} catch (Throwable exception) { // fall through
			// ClassNotFoundException, NoClassDefFoundError
		}
		if (fVirtualMachineManager == null) {
			// If any exceptions occurred, we'll end up here
			fVirtualMachineManager= new org.eclipse.jdi.internal.VirtualMachineManagerImpl();
		}
		
		return fVirtualMachineManager;
	}
}
