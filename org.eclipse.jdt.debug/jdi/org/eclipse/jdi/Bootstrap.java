package org.eclipse.jdi;

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
			
		fVirtualMachineManager = new org.eclipse.jdi.internal.VirtualMachineManagerImpl();
		return fVirtualMachineManager;
	}
}