package org.eclipse.jdt.internal.launching.macosx;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.launching.StandardVMRunner;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

public class MacOSXVMRunner extends StandardVMRunner {
	
	public MacOSXVMRunner(IVMInstall vmInstance) {
		super(vmInstance);
	}

	protected String constructProgramString(VMRunnerConfiguration config) {
		String command= null;
		Map map= config.getVMSpecificAttributesMap();
		if (map != null) {
			command = (String)map.get(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND);
		}
		StringBuffer buff= new StringBuffer(getJDKLocation());
		buff.append(File.separator);
		buff.append("bin"); //$NON-NLS-1$
		buff.append(File.separator);
		String jdkLocation= buff.toString();
		if (command == null) {
			buff.append("java"); //$NON-NLS-1$
			return adjustProgramString(buff.toString());
		} 
		
		buff.append(command);
		String program= buff.toString();
		File exe= new File(program + ".exe"); //$NON-NLS-1$
		File javaCommand= new File(program); 
		
		if (!exe.isFile() && !javaCommand.isFile()) {
			File java= new File(jdkLocation + "java.exe"); //$NON-NLS-1$
			if (java.isFile()) {
				program= java.getAbsolutePath();
			} else {
				java= new File(jdkLocation + "java"); //$NON-NLS-1$
				if (java.isFile()) {
					program= java.getAbsolutePath();
				}
			}
		}
		return program;
	}	
	
	protected boolean shouldIncludeInPath(String path) {
		return true;
	}
	
	protected Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
		
		String[] cmdLine2= new String[cmdLine.length + 2];
		
		String wrapper= MacOSXLaunchingPlugin.createWrapper(getClass(), "start_carbon.sh");
		
		int j= 0;
		cmdLine2[j++]= "/bin/sh";
		cmdLine2[j++]= wrapper;
		for (int i= 0; i < cmdLine.length; i++)
			cmdLine2[j++]= cmdLine[i];
		
		return super.exec(cmdLine2, workingDirectory);
	}
}
