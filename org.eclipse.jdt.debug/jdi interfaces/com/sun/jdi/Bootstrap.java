package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public class Bootstrap
{
	private static org.eclipse.jdi.internal.VirtualMachineManagerImpl fVirtualMachineManager;

	public Bootstrap() { }
	
	public static synchronized com.sun.jdi.VirtualMachineManager virtualMachineManager() {
		return org.eclipse.jdi.Bootstrap.virtualMachineManager();
	}
}
