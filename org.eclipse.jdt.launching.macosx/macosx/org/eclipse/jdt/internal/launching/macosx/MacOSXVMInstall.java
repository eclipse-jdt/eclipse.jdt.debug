/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.launching.macosx;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;

public class MacOSXVMInstall extends AbstractVMInstall {

	/**
	 * Constructor for MacOSXVM
	 */
	MacOSXVMInstall(IVMInstallType type, String id) {
		super(type, id);
	}

	/**
	 * @see IVMInstall#getVMRunner(String)
	 */
	public IVMRunner getVMRunner(String mode) {
		if (ILaunchManager.RUN_MODE.equals(mode))
			return new MacOSXVMRunner(this);
		
		if (ILaunchManager.DEBUG_MODE.equals(mode))
			return new MacOSXDebugVMRunner(this);
		
		return null;
	}
}
