/*
 * (c) Copyright IBM Corp. 2002, 2003.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.launching.macosx;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;

public class MacOSXVMInstall extends AbstractVMInstall {

	MacOSXVMInstall(IVMInstallType type, String id) {
		super(type, id);
	}

	public IVMRunner getVMRunner(String mode) {
		if (ILaunchManager.RUN_MODE.equals(mode))
			return new MacOSXVMRunner(this);
		
		if (ILaunchManager.DEBUG_MODE.equals(mode))
			return new MacOSXDebugVMRunner(this);
		
		return null;
	}
}
