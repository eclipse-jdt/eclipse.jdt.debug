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
package org.eclipse.jdt.internal.debug.ui;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class JavaHotCodeReplaceListener implements IJavaHotCodeReplaceListener {

	private ILabelProvider fLabelProvider= DebugUITools.newDebugModelPresentation();

	/**
	 * @see IJavaHotCodeReplaceListener#hotCodeReplaceSucceeded(IJavaDebugTarget)
	 */
	public void hotCodeReplaceSucceeded(IJavaDebugTarget target) {
	}

	/**
	 * @see IJavaHotCodeReplaceListener#hotCodeReplaceFailed(IJavaDebugTarget, DebugException)
	 */
	public void hotCodeReplaceFailed(final IJavaDebugTarget target, final DebugException exception) {
		if ((exception != null &&!JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED)) ||
			((exception == null) && !JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_ALERT_HCR_NOT_SUPPORTED))) {
			return;
		}
		// do not report errors for snippet editor targets
		// that do not support HCR. HCR is simulated by using
		// a new class loader for each evaluation
		ILaunch launch = target.getLaunch();
		if (launch.getAttribute(ScrapbookLauncher.SCRAPBOOK_LAUNCH) != null) {
			if (!target.supportsHotCodeReplace()) {
					return;
			}
		}
		final Display display= JDIDebugUIPlugin.getStandardDisplay();
		if (display.isDisposed()) {
			return;
		}
		
		String name = null;
		try {
			name = target.getName();
		}
		catch(DebugException e) {
			name = fLabelProvider.getText(target);
		}
		final String vmName = name;
		final IStatus status;
		final String preference;
		final String alertMessage;
		ILaunchConfiguration config = target.getLaunch().getLaunchConfiguration();
		final String launchName = (config != null ? config.getName() : DebugUIMessages.JavaHotCodeReplaceListener_0);
		if (exception == null) {
			status = new Status(IStatus.WARNING, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.WARNING, DebugUIMessages.JDIDebugUIPlugin_The_target_VM_does_not_support_hot_code_replace_1, null);
			preference= IJDIPreferencesConstants.PREF_ALERT_HCR_NOT_SUPPORTED;
			alertMessage= DebugUIMessages.JDIDebugUIPlugin_3; 
		} else {
			status = new Status(IStatus.WARNING, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.WARNING, exception.getMessage(), exception.getCause());
			preference= IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED;
			alertMessage= DebugUIMessages.JDIDebugUIPlugin_1; 
		}
		final String title = DebugUIMessages.JDIDebugUIPlugin_Hot_code_replace_failed_1; 
		final String message = NLS.bind(DebugUIMessages.JDIDebugUIPlugin__0__was_unable_to_replace_the_running_code_with_the_code_in_the_workspace__2, new Object[] {vmName, launchName});
		display.asyncExec(new Runnable() {
			public void run() {
				if (display.isDisposed()) {
					return;
				}
				Shell shell= JDIDebugUIPlugin.getActiveWorkbenchShell();
				HotCodeReplaceErrorDialog dialog= new HotCodeReplaceErrorDialog(shell, title, message, status, preference, alertMessage, JDIDebugUIPlugin.getDefault().getPreferenceStore(), target);
				dialog.setBlockOnOpen(false);
				dialog.open();
			}
		});
	}
	
	/**
	 * @see IJavaHotCodeReplaceListener#obsoleteMethods(IJavaDebugTarget)
	 */
	public void obsoleteMethods(final IJavaDebugTarget target) {
		if (!JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS)) {
			return;
		}
		final Display display= JDIDebugUIPlugin.getStandardDisplay();
		if (display.isDisposed()) {
			return;
		}
		final String vmName= fLabelProvider.getText(target);
		final String dialogTitle= DebugUIMessages.JDIDebugUIPlugin_Obsolete_methods_remain_1; 
		final String message= NLS.bind(DebugUIMessages.JDIDebugUIPlugin__0__contains_obsolete_methods_1, new Object[] {vmName}); 
		final IStatus status= new Status(IStatus.WARNING, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.WARNING, DebugUIMessages.JDIDebugUIPlugin_Stepping_may_be_hazardous_1, null); 
		final String toggleMessage= DebugUIMessages.JDIDebugUIPlugin_2; 
		display.asyncExec(new Runnable() {
			public void run() {
				if (display.isDisposed()) {
					return;
				}
				Shell shell= JDIDebugUIPlugin.getActiveWorkbenchShell();
				HotCodeReplaceErrorDialog dialog= new HotCodeReplaceErrorDialog(shell, dialogTitle, message, status, IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS,
					toggleMessage, JDIDebugUIPlugin.getDefault().getPreferenceStore(), target);
				dialog.setBlockOnOpen(false);
				dialog.open();
			}
		});
	}

}
