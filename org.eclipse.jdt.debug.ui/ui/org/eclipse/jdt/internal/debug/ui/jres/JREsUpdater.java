/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.launching.VMDefinitionsContainer;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Processes add/removed/changed VMs.
 */
public class JREsUpdater {
	
	// the VMs defined when this updated is instantiated
	private VMDefinitionsContainer fOriginalVMs;	
	
	// shell for error dilaogs
	private Shell fShell;

	/**
	 * Contstructs a new VM updater to update VM install settings.
	 * 
	 * @param shell a shell on which to display error dialogs (if required),
	 *  or <code>null</code> if none
	 */
	public JREsUpdater(Shell shell) {
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
		fShell = shell;		
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
		
		// determine if a build is required
		boolean buildRequired = false;
		try {
			buildRequired = isBuildRequired(fOriginalVMs, vmContainer);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		} 
		boolean build = false;
		if (buildRequired) {
			// prompt the user to do a full build
			MessageDialog messageDialog = new MessageDialog(getShell(), JREMessages.getString("JREsPreferencePage.4"), null,  //$NON-NLS-1$
			JREMessages.getString("JREsPreferencePage.5"), //$NON-NLS-1$
			MessageDialog.QUESTION, new String[] {JREMessages.getString("JREsPreferencePage.6"), JREMessages.getString("JREsPreferencePage.7"), JREMessages.getString("JREsPreferencePage.8")}, 0); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			int button = messageDialog.open();
			if (button == 2) {
				return false;
			}
			build = button == 0;			
		}
		
		// Generate XML for the VM defs and save it as the new value of the VM preference
		saveVMDefinitions(vmContainer);
		
		// do a build if required
		if (build) {
			buildWorkspace();
		}
		
		return true;
	}
	
	/**
	 * Returns whether a re-build is required based on the previous and current
	 * VM definitions.
	 * 
	 * @param prev VMs defined in the workspace
	 * @param curr VMs that will be defined in the workspace
	 * @return whether the new JRE definitions required the workspace to be
	 * built
	 */
	private boolean isBuildRequired(VMDefinitionsContainer prev, VMDefinitionsContainer curr) throws CoreException {
		String prevDef = prev.getDefaultVMInstallCompositeID();
		String currDef = curr.getDefaultVMInstallCompositeID();
		
		boolean defaultChanged = !isEqual(prevDef, currDef);
		
		IJavaProject[] projects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
		
		//if the default VM changed, see if any projects reference it
		if (defaultChanged) {
			for (int i = 0; i < projects.length; i++) {
				IJavaProject project = projects[i];
				IClasspathEntry[] entries = project.getRawClasspath();
				for (int j = 0; j < entries.length; j++) {
					IClasspathEntry entry = entries[j];
					switch (entry.getEntryKind()) {
						case IClasspathEntry.CPE_VARIABLE:
							IPath path = entry.getPath();
							if (path.segmentCount() == 1 && path.segment(0).equals(JavaRuntime.JRELIB_VARIABLE)) {
								// a project references the default JRE via JRE_LIB
								return true;
							}
							break;
						case IClasspathEntry.CPE_CONTAINER:
							path = entry.getPath();
							if (path.segmentCount() == 1 && path.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
								// a project references the default JRE via JRE_CONTAIER
								return true;
							}
							break;
					}
				}
			}
		}
		
		// otherwise, if a referenced VM is removed or there is a library
		// change in a referenced VM, a build is required 
		List futureVMs = curr.getVMList();
		for (int i = 0; i < projects.length; i++) {
			IJavaProject project = projects[i];
			IVMInstall prevVM = JavaRuntime.getVMInstall(project);
			if (prevVM != null) {
				int index  = futureVMs.indexOf(prevVM);
				if (index >= 0) {
					IVMInstall futureVM = (IVMInstall)futureVMs.get(index);
					// the VM still exists, see if the libraries changed
					LibraryLocation[] prevLibs = JavaRuntime.getLibraryLocations(prevVM);
					LibraryLocation[] newLibs = JavaRuntime.getLibraryLocations(futureVM);
					if (prevLibs.length == newLibs.length) {
						for (int j = 0; j < newLibs.length; j++) {
							LibraryLocation newLib = newLibs[j];
							LibraryLocation prevLib = prevLibs[j];
							String newPath = newLib.getSystemLibraryPath().toOSString();
							String prevPath = prevLib.getSystemLibraryPath().toOSString();
							if (!newPath.equalsIgnoreCase(prevPath)) {
								// different libs or ordering, a re-build is required
								return true;
							}
						} 
					} else {
						// different number of libraries, a re-build is required
						return true;
					}
				} else {
					// the VM no longer exists, a re-build will be required
					return true;
				}
			}
		}
		
		
		return false;
	}
	
	
	private boolean isEqual(Object a, Object b) {
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return false;
		}
		return (a.equals(b));
	}	
	
	private void saveVMDefinitions(final VMDefinitionsContainer container) {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				// Generate XML for the VM defs and save it as the new value of the VM preference
				try {
					String vmDefXML = container.getAsXML();
					JavaRuntime.getPreferences().setValue(JavaRuntime.PREF_VM_XML, vmDefXML);
					JavaRuntime.savePreferences();
				} catch (IOException ioe) {
					JDIDebugUIPlugin.log(ioe);
				} catch (ParserConfigurationException e) {
					JDIDebugUIPlugin.log(e);
				} catch (TransformerException e) {
					JDIDebugUIPlugin.log(e);
				}
			}
		});
	}	
	
	private Shell getShell() {
		if (fShell == null) {
			return JDIDebugUIPlugin.getActiveWorkbenchShell();
		} else {
			return fShell;
		}
	} 
	
	private void buildWorkspace() {
		ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, true, new WorkspaceModifyOperation() {
				public void execute(IProgressMonitor monitor) throws InvocationTargetException{
					try {
						ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InterruptedException e) {
			// opearation canceled by user
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), JREMessages.getString("JREsPreferencePage.1"), JREMessages.getString("JREsPreferencePage.9")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}			
	
}
