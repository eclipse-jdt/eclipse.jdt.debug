package org.eclipse.jdt.internal.debug.ui.launcher;

/*******************************************************************************
 * Copyright (c) 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * When a project's JRE cannot be resolved for building, this status handler
 * is called.
 */
public class UnresolvedJREStatusHandler implements IStatusHandler {

	protected ILaunchConfigurationWorkingCopy fConfig;
	
	class JREDialog extends ErrorDialog {
		
		private JavaJRETab fJRETab;
			
		public JREDialog(IStatus status) {
			super(JDIDebugUIPlugin.getActiveWorkbenchShell(), LauncherMessages.getString("UnresolvedJREStatusHandler.Error_1"), LauncherMessages.getString("UnresolvedJREStatusHandler.Unable_to_resolve_system_library._Select_an_alternate_JRE._2"), status, IStatus.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * @see Dialog#createDialogArea(Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			Composite comp = (Composite)super.createDialogArea(parent);
			
			fJRETab = new JavaJRETab();
			fJRETab.setVMSpecificArgumentsVisible(false);
			fJRETab.createControl(comp);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			fJRETab.getControl().setLayoutData(gd);
			fJRETab.setDefaults(fConfig);
			fJRETab.initializeFrom(fConfig);
			
			return comp;
		}

		/**
		 * @see Dialog#okPressed()
		 */
		protected void okPressed() {
			fJRETab.performApply(fConfig);
			super.okPressed();
		}
	}
	
	/**
	 * @param source source is an instance of <code>IJavaProject</code>
	 * @return an instance of <code>IVMInstall</code> or <code>null</code>
	 * 
	 * @see IStatusHandler#handleStatus(IStatus, Object)
	 */
	public Object handleStatus(final IStatus status, Object source) throws CoreException {
		ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		fConfig = type.newInstance(null, "TEMP_CONFIG"); //$NON-NLS-1$

		Runnable r = new Runnable() {
			public void run() {
				JREDialog dialog = new JREDialog(status);
				dialog.open();				
			}
		};
		JDIDebugUIPlugin.getStandardDisplay().syncExec(r);
		
		IVMInstall vm = null;
		String typeId = fConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
		String name = fConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, (String)null);
		if (typeId == null) {
			vm = JavaRuntime.getDefaultVMInstall();
		} else {
			IVMInstallType vmType = JavaRuntime.getVMInstallType(typeId);
			if (vmType != null) {
				vm = vmType.findVMInstallByName(name);
			}
		}
		return vm;
	}
}