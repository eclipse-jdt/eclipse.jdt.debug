/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.classpath;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.launching.DefaultProjectClasspathEntry;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

/**
 * Edits project default classpath.
 *
 * @since 3.2
 */
public class DefaultClasspathEntryDialog extends MessageDialog {

	private final DefaultProjectClasspathEntry fEntry;
	private Button fButton;

	public DefaultClasspathEntryDialog(Shell parentShell, IRuntimeClasspathEntry entry) {
		super(parentShell, ClasspathMessages.DefaultClasspathEntryDialog_0, null,
				NLS.bind(ClasspathMessages.DefaultClasspathEntryDialog_1, entry.getJavaProject().getElementName()),
				MessageDialog.NONE, new String[]{ClasspathMessages.DefaultClasspathEntryDialog_2, ClasspathMessages.DefaultClasspathEntryDialog_3}, 0);
		fEntry = (DefaultProjectClasspathEntry) entry;
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_BOTH);
		boolean wslocked = Platform.getPreferencesService().getBoolean(
				LaunchingPlugin.ID_PLUGIN,
				JavaRuntime.PREF_ONLY_INCLUDE_EXPORTED_CLASSPATH_ENTRIES,
				false,
				null);
		if(wslocked) {
			Link link = new Link(comp, SWT.NONE);
			link.setLayoutData(new GridData());
			link.setText(ClasspathMessages.DefaultClasspathEntryDialog_property_locked);
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					UIJob job = new UIJob(ClasspathMessages.DefaultClasspathEntryDialog_show_preferences) {
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							JDIDebugUIPlugin.showPreferencePage("org.eclipse.jdt.debug.ui.JavaDebugPreferencePage"); //$NON-NLS-1$
							return Status.OK_STATUS;
						}
					};
					job.setPriority(Job.INTERACTIVE);
					job.schedule();
					close();
				}
			});
			SWTFactory.createVerticalSpacer(comp, 1);
		}
		fButton = SWTFactory.createCheckButton(comp, ClasspathMessages.DefaultClasspathEntryDialog_4, null, fEntry.isExportedEntriesOnly(), 1);
		if(wslocked) {
			fButton.setEnabled(false);
		}
		return comp;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 0) {
			fEntry.setExportedEntriesOnly(fButton.getSelection());
		}
		super.buttonPressed(buttonId);
	}
}