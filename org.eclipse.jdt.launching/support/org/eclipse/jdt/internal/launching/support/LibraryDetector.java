package org.eclipse.jdt.internal.launching.support;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

/**
 * Used to discover the boot path and extension dirs for a Java VM.
 */
public class LibraryDetector {

	/**
	 * Prints system properties to standard out.
	 * <ul>
	 * <li>sun.boot.class.path</li>
	 * <li>java.ext.dirs</li>
	 * </ul>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.print(System.getProperty("sun.boot.class.path")); //$NON-NLS-1$
		System.out.print("|"); //$NON-NLS-1$
		System.out.print(System.getProperty("java.ext.dirs")); //$NON-NLS-1$
	}
}
