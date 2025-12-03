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

import static org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants.PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdi.internal.ClassTypeImpl;
import org.eclipse.jdi.internal.ReferenceTypeImpl;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.ui.DebugUIMessages;
import org.eclipse.jdt.internal.debug.ui.ErrorDialogWithToggle;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.NoLineNumberAttributesStatusHandler;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.test.OrderedTestSuite;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

/**
 * This test is checking the {@link NoLineNumberAttributesStatusHandler} the functionality that the ok button click is saving the state in the
 * preference store.
 */
public class NoLineNumberAttributesStatusHandlerTest extends AbstractDebugUiTests {

	public static junit.framework.Test suite() {
		return new OrderedTestSuite(NoLineNumberAttributesStatusHandlerTest.class);
	}

	private static final class ErrorDialogWithToggleRunnable implements Runnable {

		private final IPreferenceStore preferenceStore;
		private final IStatus status;
		private final boolean toggleValue;

		private ErrorDialogWithToggleRunnable(IPreferenceStore preferenceStore, IStatus status, boolean toggleValue) {
			this.preferenceStore = preferenceStore;
			this.status = status;
			this.toggleValue = toggleValue;
		}

		@Override
		public void run() {
			Shell shell = PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
			class ErrorDialogWithToggleForTest extends ErrorDialogWithToggle {

				public ErrorDialogWithToggleForTest(Shell parentShell, String dialogTitle, String message, IStatus status, String preferenceKey, String toggleMessage, IPreferenceStore store) {
					super(parentShell, dialogTitle, message, status, preferenceKey, toggleMessage, store);
				}

				/**
				 * Overridden to make public.
				 */
				@Override
				public Button getToggleButton() {
					return super.getToggleButton();
				}

				/**
				 * Overridden to make public.
				 */
				@Override
				public void buttonPressed(int id) {
					super.buttonPressed(id);
				}
			}
			ErrorDialogWithToggleForTest dialog = new ErrorDialogWithToggleForTest(shell, DebugUIMessages.NoLineNumberAttributesStatusHandler_Java_Breakpoint_1, NLS.bind(DebugUIMessages.NoLineNumberAttributesStatusHandler_2, "HelloWorld"), status, PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT, DebugUIMessages.NoLineNumberAttributesStatusHandler_3, preferenceStore);
			final boolean originalMode = ErrorDialog.AUTOMATED_MODE;
			ErrorDialog.AUTOMATED_MODE = false;
			try {
				dialog.create();
				dialog.getToggleButton().setSelection(toggleValue);
				dialog.getShell().addShellListener(new ShellAdapter() {
					@Override
					public void shellActivated(ShellEvent e) {
						// To see dialog: processUiEvents(500);
						processUiEvents();
						dialog.buttonPressed(IDialogConstants.OK_ID);
					}
				});
				dialog.open();
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				ErrorDialog.AUTOMATED_MODE = originalMode;
				dialog.close();
			}
		}

	}

	private static final class ListLogListener implements ILogListener {
		private final List<IStatus> loggedEntries;

		private ListLogListener(List<IStatus> loggedEntries) {
			this.loggedEntries = loggedEntries;
		}

		@Override
		public void logging(IStatus status, String plugin) {
			if (status.isMultiStatus() && status.getChildren().length == 1) {
				loggedEntries.add(status.getChildren()[0]);
			}
		}
	}

	public NoLineNumberAttributesStatusHandlerTest(String name) {
		super(name);
	}

	@Test
	public void testPreferenceSettings() throws Exception {
		List<IStatus> loggedEntries = new ArrayList<>();
		ILogListener listener = new ListLogListener(loggedEntries);

		Platform.addLogListener(listener);
		try {
			IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();

			IStatus status = new Status(IStatus.ERROR, "org.eclipse.jdt.debug", JavaLineBreakpoint.NO_LINE_NUMBERS, "Teststatus", null);

			// No errors should be logged after "don't tell me" preference is set
			boolean dontTellMeAgain = true;
			simulateErrorDialogWithToggleExecution(preferenceStore, status, dontTellMeAgain);
			assertFalse("Wrong preference set for alert", preferenceStore.getBoolean(PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT));
			assertTrue("Expected no logged entries but got: " + loggedEntries, loggedEntries.isEmpty());
			triggerNoLineAttributesStatusHandler(status);
			assertEquals("Expected no logged entries but got: " + loggedEntries, 0, Collections.frequency(loggedEntries, status));

			// Error should be logged if "don't tell me" preference is not set
			dontTellMeAgain = false;
			simulateErrorDialogWithToggleExecution(preferenceStore, status, dontTellMeAgain);
			assertTrue("Wrong preference set for alert", preferenceStore.getBoolean(PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT));
			assertTrue("Expected no logged entries but got: " + loggedEntries, loggedEntries.isEmpty());
			triggerNoLineAttributesStatusHandler(status);
			assertEquals(1, Collections.frequency(loggedEntries, status));
			loggedEntries.clear();

			// No errors should be logged after "don't tell me" preference is set again
			dontTellMeAgain = true;
			simulateErrorDialogWithToggleExecution(preferenceStore, status, dontTellMeAgain);
			assertFalse("Wrong preference set for alert", preferenceStore.getBoolean(PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT));
			assertTrue("Expected no logged entries but got: " + loggedEntries, loggedEntries.isEmpty());
			triggerNoLineAttributesStatusHandler(status);
			assertEquals("Expected no logged entries but got: " + loggedEntries, 0, Collections.frequency(loggedEntries, status));
		} finally {
			Platform.removeLogListener(listener);
		}
	}

	private void triggerNoLineAttributesStatusHandler(IStatus status) {
		ReferenceTypeImpl referenceTypeImpl = new ClassTypeImpl(null, null);
		referenceTypeImpl.setName("TestRefTypeName");
		NoLineNumberAttributesStatusHandler statusHandler = (NoLineNumberAttributesStatusHandler) DebugPlugin.getDefault().getStatusHandler(status);
		statusHandler.handleStatus(status, referenceTypeImpl);
	}

	private void simulateErrorDialogWithToggleExecution(IPreferenceStore preferenceStore, IStatus status, boolean toggleValue) throws Exception {
		ErrorDialogWithToggleRunnable runnable = new ErrorDialogWithToggleRunnable(preferenceStore, status, toggleValue);
		sync(runnable);
	}
}
