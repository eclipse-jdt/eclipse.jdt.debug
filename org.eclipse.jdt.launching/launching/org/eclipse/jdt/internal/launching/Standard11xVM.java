package org.eclipse.jdt.internal.launching;

/*******************************************************************************
 * Copyright (c) 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;

/**
 * A 1.1.x VM
 */
public class Standard11xVM extends StandardVM {

	public Standard11xVM(IVMInstallType type, String id) {
		super(type, id);
	}


	/**
	 * @see org.eclipse.jdt.launching.IVMInstall#getVMRunner(String)
	 */
	public IVMRunner getVMRunner(String mode) {
		if (ILaunchManager.RUN_MODE.equals(mode)) {
			return new Standard11xVMRunner(this);
		}
		return null;
	}


}

