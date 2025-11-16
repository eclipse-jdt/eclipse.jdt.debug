/*******************************************************************************
 * Copyright (c) 2025 Advantest Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Advantest Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.debug.tests.TestDebugTarget;
import org.eclipse.jdt.internal.debug.ui.DebugUIMessages;
import org.eclipse.jdt.internal.debug.ui.HotCodeReplaceErrorDialog;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

public class HotCodeReplaceErrorDialogTest extends AbstractDebugUiTests {

	private final class HotCodeReplaceErrorDialogSimRunnable implements Runnable {
		private final IStatus status;
		private final String toggleMessage;
		private final String prefAlertObsoleteMethods;
		private final String toggleMessage2;
		private final IPreferenceStore preferenceStore;
		private final String message;
		private final String dialogTitle;
		private final Shell shell;

		private HotCodeReplaceErrorDialogSimRunnable(IStatus status, String toggleMessage, String prefAlertObsoleteMethods, String toggleMessage2, IPreferenceStore preferenceStore, String message, String dialogTitle, Shell shell) {
			this.status = status;
			this.toggleMessage = toggleMessage;
			this.prefAlertObsoleteMethods = prefAlertObsoleteMethods;
			this.toggleMessage2 = toggleMessage2;
			this.preferenceStore = preferenceStore;
			this.message = message;
			this.dialogTitle = dialogTitle;
			this.shell = shell;
		}

		@Override
		public void run() {
			class HotCodeReplaceErrorDialogExtension extends HotCodeReplaceErrorDialog {
				private HotCodeReplaceErrorDialogExtension(Shell parentShell, String dialogTitle, String message, IStatus status, String preferenceKey, String toggleMessage, String toggleMessage2, IPreferenceStore store, IDebugTarget target) {
					super(parentShell, dialogTitle, message, status, preferenceKey, toggleMessage, toggleMessage2, store, target);
				}

				@Override
				public void buttonPressed(int id, IDebugTarget target) {
					super.buttonPressed(id, target);
				}
			}

			HotCodeReplaceErrorDialogExtension errorDialog = new HotCodeReplaceErrorDialogExtension(shell, dialogTitle, message, status, prefAlertObsoleteMethods, toggleMessage, toggleMessage2, preferenceStore, new TestDebugTarget());
			final boolean originalMode = ErrorDialog.AUTOMATED_MODE;
			ErrorDialog.AUTOMATED_MODE = false;
			try {
				errorDialog.create();
				errorDialog.getShell().addShellListener(new ShellAdapter() {
					@Override
					public void shellActivated(ShellEvent e) {
						// To see dialog: processUiEvents(500);
						processUiEvents();
						errorDialog.buttonPressed(IDialogConstants.OK_ID, null);
					}
				});
				errorDialog.open();
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				ErrorDialog.AUTOMATED_MODE = originalMode;
				errorDialog.close();
			}
		}
	}

	public HotCodeReplaceErrorDialogTest(String name) {
		super(name);
	}

	@Test
	public void testHotCodeReplaceErrorDialog() {
		Shell shell = JDIDebugUIPlugin.getActiveWorkbenchShell();
		final String vmName = "Dummy VM";
		final String dialogTitle = DebugUIMessages.JDIDebugUIPlugin_Obsolete_methods_remain_1;
		final String message = NLS.bind(DebugUIMessages.JDIDebugUIPlugin__0__contains_obsolete_methods_1, new Object[] { vmName });
		final IStatus status = new Status(IStatus.WARNING, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.WARNING, DebugUIMessages.JDIDebugUIPlugin_Stepping_may_be_hazardous_1, null);
		final String toggleMessage = DebugUIMessages.JDIDebugUIPlugin_2;
		final String toggleMessage2 = DebugUIMessages.JDIDebugUIPlugin_5;
		IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		String prefAlertObsoleteMethods = IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS;

		Runnable dialogSimRunnable = new HotCodeReplaceErrorDialogSimRunnable(status, toggleMessage, prefAlertObsoleteMethods, toggleMessage2, preferenceStore, message, dialogTitle, shell);
		sync(dialogSimRunnable);
	}
}
