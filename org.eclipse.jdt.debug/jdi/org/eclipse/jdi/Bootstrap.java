package org.eclipse.jdi;/*
 * JDI Interface Specification class. 
 *
 * (BB)
 * (C) Copyright IBM Corp. 2000
 */


public class Bootstrap
{
	private static org.eclipse.jdi.internal.VirtualMachineManagerImpl fVirtualMachineManager;

	public Bootstrap() { }
	
	public static synchronized com.sun.jdi.VirtualMachineManager virtualMachineManager() {
		if (fVirtualMachineManager != null)
			return fVirtualMachineManager;
			
		fVirtualMachineManager = new org.eclipse.jdi.internal.VirtualMachineManagerImpl();
		return fVirtualMachineManager;
	}
}