/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.macosx;

import org.eclipse.jdt.internal.launching.StandardVMRunner;
import org.eclipse.jdt.launching.IVMInstall;

public class MacOSXVMRunner extends StandardVMRunner {
	
	/**
	 * Constructor
	 * @param vmInstance
	 */
	public MacOSXVMRunner(IVMInstall vmInstance) {
		super(vmInstance);
	}
}
