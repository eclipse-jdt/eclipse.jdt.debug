package org.eclipse.jdt.internal.launching;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

/**
 * A container for information resulting from doing 'java -version' on a 
 * particular VM.  
 */
public class JavaVersionInfo {
		
	/**
	 * All VMs output a line of text that looks like:
	 * 
	 * java version "XXXX"
	 * 
	 * in response to the 'java -version' command.  This String
	 * holds the XXXX (without quotes).
	 */
	private String fVersionString;

	/**
	 * Indicates whether the String "IBM" was found anywhere in the 'java -
	 * version' output.
	 */
	private boolean fIBMFound;

	public JavaVersionInfo(String version, boolean ibmFlag) {
		setVersionString(version);
		setIBMFound(ibmFlag);
	}

	public static JavaVersionInfo getEmptyJavaVersionInfo() {
		return new JavaVersionInfo("", false);
	}
	
	private void setVersionString(String version) {
		fVersionString = version;
	}

	public String getVersionString() {
		return fVersionString;
	}

	private void setIBMFound(boolean found) {
		fIBMFound = found;
	}

	public boolean ibmFound() {
		return fIBMFound;
	}
	
	public boolean ibm14Found() {
		return ibmFound() && getVersionString().startsWith("1.4"); //$NON-NLS-1$
	}	

}
