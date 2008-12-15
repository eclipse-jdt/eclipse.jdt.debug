/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;

/**
 * Searches for installed JREs on the MAC, in known location.
 */
public class MacVMSearch {
	
	/** The OS keeps all the JVM versions in this directory */
	private static final String JVM_VERSION_LOC= "/System/Library/Frameworks/JavaVM.framework/Versions/";	//$NON-NLS-1$
	/** The name of a Unix link to MacOS X's default VM */
	private static final String CURRENT_JVM= "CurrentJDK";	//$NON-NLS-1$
	/** The root of a JVM */
	private static final String JVM_ROOT= "Home";	//$NON-NLS-1$

	/**
	 * Returns an array of {@link VMStandin}s found at the standard Mac OS location
	 * or an empty listing, never <code>null</code>
	 * @param monitor
	 * @return a listing of {@link VMStandin}s at the standard Mac OS location or an empty listing
	 */
	public VMStandin[] search(IProgressMonitor monitor) {
		List vms = new ArrayList();
		IVMInstallType macVMType = JavaRuntime.getVMInstallType(InstalledJREsBlock.MACOSX_VM_TYPE_ID);
		if (macVMType != null) {
			// find all installed VMs
			File versionDir= new File(JVM_VERSION_LOC);
			if (versionDir.exists() && versionDir.isDirectory()) {
				File currentJDK= new File(versionDir, CURRENT_JVM);
				try {
					currentJDK= currentJDK.getCanonicalFile();
				} catch (IOException ex) {
					// NeedWork
				}
				File[] versions= versionDir.listFiles();
				SubMonitor localmonitor = SubMonitor.convert(monitor, JREMessages.MacVMSearch_0, versions.length);
				try {
					for (int i= 0; i < versions.length; i++) {
						String version= versions[i].getName();
						File home= new File(versions[i], JVM_ROOT);
						if (home.exists()) {
							boolean isDefault= currentJDK.equals(versions[i]);
							if (!CURRENT_JVM.equals(version)) {
								VMStandin vm= new VMStandin(macVMType, version);
								vm.setInstallLocation(home);
								String format= isDefault ? JREMessages.MacVMSearch_1 : JREMessages.MacVMSearch_2;
								vm.setName(MessageFormat.format(format, new Object[] { version } ));
								vm.setLibraryLocations(macVMType.getDefaultLibraryLocations(home));
								URL doc= ((AbstractVMInstallType)macVMType).getDefaultJavadocLocation(home);
								if (doc != null) {
									vm.setJavadocLocation(doc);
								}
								String arguments = ((AbstractVMInstallType)macVMType).getDefaultVMArguments(home);
								if (arguments != null) {
									vm.setVMArgs(arguments);
								}
								vms.add(vm);
							}
						}
						if(localmonitor.isCanceled()) {
							break;
						}
						localmonitor.worked(1);
					}
				}
				finally {
					if(localmonitor != null) {
						localmonitor.done();
					}
				}
			}
		}
		return (VMStandin[]) vms.toArray(new VMStandin[vms.size()]);
	}
}
