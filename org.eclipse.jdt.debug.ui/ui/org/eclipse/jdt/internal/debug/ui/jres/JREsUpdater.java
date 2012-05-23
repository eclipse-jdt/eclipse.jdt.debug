/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.internal.launching.VMDefinitionsContainer;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * Processes add/removed/changed VMs.
 */
public class JREsUpdater {
	
	// the VMs defined when this updated is instantiated
	private VMDefinitionsContainer fOriginalVMs;	
	
	/**
	 * Contstructs a new VM updater to update VM install settings.
	 */
	public JREsUpdater() {
		fOriginalVMs = new VMDefinitionsContainer();
		IVMInstall def = JavaRuntime.getDefaultVMInstall();
		if (def != null) {
			fOriginalVMs.setDefaultVMInstallCompositeID(JavaRuntime.getCompositeIdFromVM(def));
		}
	
		IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
		for (int i = 0; i < types.length; i++) {
			IVMInstall[] vms = types[i].getVMInstalls();
			for (int j = 0; j < vms.length; j++) {
				fOriginalVMs.addVM(vms[j]);
			}
		}
	}
	
	/**
	 * Updates VM settings and returns whether the update was successful.
	 * 
	 * @param jres new installed JREs
	 * @param defaultJRE new default VM
	 * @return whether the update was successful
	 */
	public boolean updateJRESettings(IVMInstall[] jres, IVMInstall defaultJRE) {
		
		// Create a VM definition container
		VMDefinitionsContainer vmContainer = new VMDefinitionsContainer();
		
		// Set the default VM Id on the container
		String defaultVMId = JavaRuntime.getCompositeIdFromVM(defaultJRE);
		vmContainer.setDefaultVMInstallCompositeID(defaultVMId);
		
		// Set the VMs on the container
		for (int i = 0; i < jres.length; i++) {
			vmContainer.addVM(jres[i]);
		}
		
		
		// Generate XML for the VM defs and save it as the new value of the VM preference
		saveVMDefinitions(vmContainer);
		
		return true;
	}
	
	private void saveVMDefinitions(final VMDefinitionsContainer container) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					monitor.beginTask(JREMessages.JREsUpdater_0, 100); 
					String vmDefXML = container.getAsXML();
					monitor.worked(40);
					IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN);
					if(prefs != null) {
						prefs.put(JavaRuntime.PREF_VM_XML, vmDefXML);
					}
					monitor.worked(30);
					JavaRuntime.savePreferences();
					monitor.worked(30);
				}  catch (CoreException e) {
					JDIDebugUIPlugin.log(e);
				} finally {
					monitor.done();
				}
				
			}
		};
		try {
			JDIDebugUIPlugin.getDefault().getWorkbench().getProgressService().busyCursorWhile(runnable);
		} catch (InvocationTargetException e) {
			JDIDebugUIPlugin.log(e);
		} catch (InterruptedException e) {
			JDIDebugUIPlugin.log(e);
		}
	}
}
