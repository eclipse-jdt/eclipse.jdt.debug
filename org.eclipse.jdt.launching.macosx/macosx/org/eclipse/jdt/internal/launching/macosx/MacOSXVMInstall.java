/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
