/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
		System.out.print(System.getProperty("java.version")); //$NON-NLS-1$
		System.out.print("|"); //$NON-NLS-1$
		System.out.print(System.getProperty("sun.boot.class.path")); //$NON-NLS-1$
		System.out.print("|"); //$NON-NLS-1$
		System.out.print(System.getProperty("java.ext.dirs")); //$NON-NLS-1$
		System.out.print("|"); //$NON-NLS-1$
		System.out.print(System.getProperty("java.endorsed.dirs")); //$NON-NLS-1$
	}
}
