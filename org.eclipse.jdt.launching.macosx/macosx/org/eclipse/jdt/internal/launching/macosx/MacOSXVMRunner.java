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

import java.io.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.launching.StandardVMRunner;
import org.eclipse.jdt.launching.IVMInstall;

public class MacOSXVMRunner extends StandardVMRunner {
	
	public MacOSXVMRunner(IVMInstall vmInstance) {
		super(vmInstance);
	}
	
	protected Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
		return super.exec(MacOSXLaunchingPlugin.wrap(getClass(), cmdLine), workingDirectory);
	}

	protected Process exec(String[] cmdLine, File workingDirectory, String[] envp) throws CoreException {
		return super.exec(MacOSXLaunchingPlugin.wrap(getClass(), cmdLine), workingDirectory, envp);
	}	
}
