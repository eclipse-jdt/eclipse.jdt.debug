/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.macosx;

import org.eclipse.jdt.internal.launching.StandardVMDebugger;
import org.eclipse.jdt.launching.IVMInstall;

/**
 * Special override for MacOSX wrapping
 */
public class MacOSXDebugVMRunner extends StandardVMDebugger {
	
	/**
	 * Constructor
	 * @param vmInstance
	 */
	public MacOSXDebugVMRunner(IVMInstall vmInstance) {
		super(vmInstance);
	}
}
