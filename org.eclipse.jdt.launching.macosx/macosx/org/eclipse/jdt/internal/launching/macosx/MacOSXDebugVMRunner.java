/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package org.eclipse.jdt.internal.launching.macosx;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.launching.StandardVMDebugger;
import org.eclipse.jdt.launching.IVMInstall;

public class MacOSXDebugVMRunner extends StandardVMDebugger {
	
	public MacOSXDebugVMRunner(IVMInstall vmInstance) {
		super(vmInstance);
	}
	
	protected Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
		
		String[] cmdLine2= new String[cmdLine.length + 2];
		
		String wrapper= MacOSXLaunchingPlugin.createWrapper(getClass(), "start_carbon.sh");	//$NON-NLS-1$
		
		int j= 0;
		cmdLine2[j++]= "/bin/sh";	//$NON-NLS-1$
		cmdLine2[j++]= wrapper;
		for (int i= 0; i < cmdLine.length; i++)
			cmdLine2[j++]= cmdLine[i];
		
		return super.exec(cmdLine2, workingDirectory);
	}
}