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
package org.eclipse.jdt.internal.debug.ui;


import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher;
import org.eclipse.jface.viewers.ILabelProvider;
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
		final String vmName= fLabelProvider.getText(target);
		final IStatus status;
		final String preference;
		final String alertMessage;
		if (exception == null) {
			status= new Status(IStatus.WARNING, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.WARNING, DebugUIMessages.getString("JDIDebugUIPlugin.The_target_VM_does_not_support_hot_code_replace_1"), null); //$NON-NLS-1$
			preference= IJDIPreferencesConstants.PREF_ALERT_HCR_NOT_SUPPORTED;
			alertMessage= DebugUIMessages.getString("JDIDebugUIPlugin.3"); //$NON-NLS-1$
		} else {
			status= exception.getStatus();
			preference= IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED;
			alertMessage= DebugUIMessages.getString("JDIDebugUIPlugin.1"); //$NON-NLS-1$
		}
		final String title= DebugUIMessages.getString("JDIDebugUIPlugin.Hot_code_replace_failed_1"); //$NON-NLS-1$
		final String message= MessageFormat.format(DebugUIMessages.getString("JDIDebugUIPlugin.{0}_was_unable_to_replace_the_running_code_with_the_code_in_the_workspace._2"), //$NON-NLS-1$
					new Object[] {vmName});
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
		final String dialogTitle= DebugUIMessages.getString("JDIDebugUIPlugin.Obsolete_methods_remain_1"); //$NON-NLS-1$
		final String message= MessageFormat.format(DebugUIMessages.getString("JDIDebugUIPlugin.{0}_contains_obsolete_methods_1"), new Object[] {vmName}); //$NON-NLS-1$
		final IStatus status= new Status(IStatus.WARNING, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.WARNING, DebugUIMessages.getString("JDIDebugUIPlugin.Stepping_may_be_hazardous_1"), null); //$NON-NLS-1$
		final String toggleMessage= DebugUIMessages.getString("JDIDebugUIPlugin.2"); //$NON-NLS-1$
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
