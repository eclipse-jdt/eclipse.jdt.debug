package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class JavaHotCodeReplaceListener implements IJavaHotCodeReplaceListener {

	private ILabelProvider fLabelProvider= new DelegatingModelPresentation();

	/**
	 * @see IJavaHotCodeReplaceListener#hotCodeReplaceSucceeded(IJavaDebugTarget)
	 */
	public void hotCodeReplaceSucceeded(IJavaDebugTarget target) {
	}

	/**
	 * @see IJavaHotCodeReplaceListener#hotCodeReplaceFailed(DebugException)
	 */
	public void hotCodeReplaceFailed(final IJavaDebugTarget target, final DebugException exception) {
		if (!JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED)) {
			return;
		}
		// do not report errors for snippet editor targets
		// that do not support HCR. HCR is simulated by using
		// a new class loader for each evaluation
		ILaunch launch = target.getLaunch();
		if (launch != null) {
			if (launch.getAttribute(ScrapbookLauncher.SCRAPBOOK_LAUNCH) != null) {
				if (!target.supportsHotCodeReplace()) {
					return;
				}
			}
		}
		final Display display= JDIDebugUIPlugin.getStandardDisplay();
		if (display.isDisposed()) {
			return;
		}
		display.asyncExec(new Runnable() {
			public void run() {
				if (display.isDisposed()) {
					return;
				}
				Shell shell= JDIDebugUIPlugin.getActiveWorkbenchShell();
				String vmName= fLabelProvider.getText(target);
				IStatus status;
				if (exception == null) {
					status= new Status(IStatus.WARNING, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.WARNING, DebugUIMessages.getString("JDIDebugUIPlugin.The_target_VM_does_not_support_hot_code_replace_1"), null); //$NON-NLS-1$
				} else {
					status= exception.getStatus();
				}
				ErrorDialogWithToggle dialog= new ErrorDialogWithToggle(shell, DebugUIMessages.getString("JDIDebugUIPlugin.Hot_code_replace_failed_1"), //$NON-NLS-1$
					MessageFormat.format(DebugUIMessages.getString("JDIDebugUIPlugin.{0}_was_unable_to_replace_the_running_code_with_the_code_in_the_workspace._2"), //$NON-NLS-1$
					new Object[] {vmName}), status, IStatus.WARNING | IStatus.ERROR | IStatus.INFO, IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED,
					DebugUIMessages.getString("JDIDebugUIPlugin.Always_alert_me_of_hot_code_replace_failure_1"), JDIDebugUIPlugin.getDefault().getPreferenceStore()); //$NON-NLS-1$
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
		display.asyncExec(new Runnable() {
			public void run() {
				if (display.isDisposed()) {
					return;
				}
				Shell shell= JDIDebugUIPlugin.getActiveWorkbenchShell();
				String vmName= fLabelProvider.getText(target);
				IStatus status;
				status= new Status(IStatus.WARNING, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.WARNING, DebugUIMessages.getString("JDIDebugUIPlugin.Stepping_may_be_hazardous_1"), null); //$NON-NLS-1$
				ErrorDialogWithToggle dialog= new ErrorDialogWithToggle(shell, DebugUIMessages.getString("JDIDebugUIPlugin.Obsolete_methods_remain_1"), //$NON-NLS-1$
					MessageFormat.format(DebugUIMessages.getString("JDIDebugUIPlugin.{0}_contains_obsolete_methods_1"), //$NON-NLS-1$
					new Object[] {vmName}), status, IStatus.WARNING | IStatus.ERROR | IStatus.INFO, IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS,
					DebugUIMessages.getString("JDIDebugUIPlugin.Always_alert_me_of_obsolete_methods_1"), JDIDebugUIPlugin.getDefault().getPreferenceStore()); //$NON-NLS-1$
				dialog.open();
			}
		});
	}

}
