/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.support;


/**
 * Used to discover the boot path, extension directories, and endorsed
 * directories for a Java VM.
 */
public class LibraryDetector {

	/**
	 * Prints system properties to standard out.
	 * <ul>
	 * <li>java.version</li>
	 * <li>sun.boot.class.path</li>
	 * <li>java.ext.dirs</li>
	 * <li>java.endorsed.dirs</li>
	 * </ul>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// if we are running raw j9
		if ("j9".equalsIgnoreCase(System.getProperty("java.vm.name"))) { //$NON-NLS-1$ //$NON-NLS-2$
			// Map class lib versions onto things that the launch infrastructure understands.  J9 
			// behaves like 1.4 wrt launch/debug
			String configuration = System.getProperty("com.ibm.oti.configuration"); //$NON-NLS-1$
			if ("found10".equals(configuration)) //$NON-NLS-1$
				System.out.print("1.4"); //$NON-NLS-1$
			else if ("found11".equals(configuration)) //$NON-NLS-1$
				System.out.print("1.4"); //$NON-NLS-1$
			else
				System.out.print(System.getProperty("java.version")); //$NON-NLS-1$
			System.out.print("|"); //$NON-NLS-1$
			System.out.print(System.getProperty("com.ibm.oti.system.class.path")); //$NON-NLS-1$
		} else {
			System.out.print(System.getProperty("java.version")); //$NON-NLS-1$
			System.out.print("|"); //$NON-NLS-1$
			System.out.print(System.getProperty("sun.boot.class.path")); //$NON-NLS-1$
		}
		System.out.print("|"); //$NON-NLS-1$
		System.out.print(System.getProperty("java.ext.dirs")); //$NON-NLS-1$
		System.out.print("|"); //$NON-NLS-1$
		System.out.print(System.getProperty("java.endorsed.dirs")); //$NON-NLS-1$
	}
}
