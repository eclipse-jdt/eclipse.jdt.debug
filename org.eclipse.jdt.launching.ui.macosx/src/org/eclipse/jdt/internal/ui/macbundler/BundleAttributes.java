/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.macbundler;
   
/**
 * All keys used in the BundleDescription.
 */
public interface BundleAttributes {
	static final String LAUNCHER = "CFBundleExecutable"; //$NON-NLS-1$
	
	static final String ALL= "ALL"; //$NON-NLS-1$
	
	static final String GETINFO = "GetInfo"; //$NON-NLS-1$
	static final String IDENTIFIER = "Identifier"; //$NON-NLS-1$
	static final String ICONFILE = "IconFile"; //$NON-NLS-1$
	static final String VMOPTIONS = "VMOptions"; //$NON-NLS-1$
	static final String ARGUMENTS = "Arguments"; //$NON-NLS-1$
	static final String WORKINGDIR = "WorkingDir"; //$NON-NLS-1$
	static final String VERSION = "Version"; //$NON-NLS-1$
	static final String MAINCLASS = "MainClass"; //$NON-NLS-1$
	static final String SIGNATURE = "Signature"; //$NON-NLS-1$
	static final String DESTINATIONDIRECTORY = "DestinationDirectory"; //$NON-NLS-1$
	static final String APPNAME = "AppName"; //$NON-NLS-1$
	static final String JVMVERSION = "JVMVersion"; //$NON-NLS-1$
	static final String USES_SWT = "UsesSWT"; //$NON-NLS-1$
}
