/*
 * (c) Copyright IBM Corp. 2002, 2003.
 * All Rights Reserved.
 */
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
}
