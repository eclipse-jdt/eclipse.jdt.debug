package org.eclipse.jdi;

import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
		} catch (Exception exception) { // fall through
		}
		if (fVirtualMachineManager == null) {
			// If any exceptions occurred, we'll end up here
			fVirtualMachineManager= new org.eclipse.jdi.internal.VirtualMachineManagerImpl();
		}
		
		return fVirtualMachineManager;
	}
}