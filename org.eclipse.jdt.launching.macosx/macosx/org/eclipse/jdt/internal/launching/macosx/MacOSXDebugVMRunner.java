/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package org.eclipse.jdt.internal.launching.macosx;

import org.eclipse.jdt.internal.launching.StandardVMDebugger;
import org.eclipse.jdt.launching.IVMInstall;

public class MacOSXDebugVMRunner extends StandardVMDebugger {
	
	public MacOSXDebugVMRunner(IVMInstall vmInstance) {
		super(vmInstance);
	}
	
}