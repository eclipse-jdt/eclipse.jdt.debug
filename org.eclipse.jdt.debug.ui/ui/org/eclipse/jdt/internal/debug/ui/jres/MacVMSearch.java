/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.launching.MacInstalledJREs;
import org.eclipse.jdt.internal.launching.MacInstalledJREs.JREDescriptor;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.osgi.util.NLS;

/**
 * Searches for installed JREs on the MAC, in known location.
 */
public class MacVMSearch {

	/**
	 * Returns an array of {@link VMStandin}s found at the standard Mac OS location
	 * or an empty listing, never <code>null</code>
	 * @param monitor
	 * @return a listing of {@link VMStandin}s at the standard Mac OS location or an empty listing
	 */
	public VMStandin[] search(IProgressMonitor monitor) {
		JREDescriptor[] descriptors = null;
		try {
			descriptors = new MacInstalledJREs().getInstalledJREs();
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e.getStatus());
			return new VMStandin[0];
		}
		SubMonitor localmonitor = SubMonitor.convert(monitor, JREMessages.MacVMSearch_0, descriptors.length);
		IVMInstallType macVMType = JavaRuntime.getVMInstallType(InstalledJREsBlock.MACOSX_VM_TYPE_ID);
		List<VMStandin> vms = new ArrayList<VMStandin>();
		if (macVMType != null) {
			for (int i = 0; i < descriptors.length; i++) {
				JREDescriptor descriptor = descriptors[i];
				String name = descriptor.getName();
				String id= descriptor.getId();
				try {
					File home= descriptor.getHome();
					if (home.exists()) {
						boolean isDefault= i == 0;
						VMStandin vm= new VMStandin(macVMType, id);
						vm.setInstallLocation(home);
						String format= isDefault ? JREMessages.MacVMSearch_1 : JREMessages.MacVMSearch_2;
						vm.setName(NLS.bind(format, new Object[] { name } ));
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
					if(localmonitor.isCanceled()) {
						break;
					}
					localmonitor.worked(1);
				}
				finally {
					localmonitor.done();
				}
			}
		}
		return vms.toArray(new VMStandin[vms.size()]);
	}
}
