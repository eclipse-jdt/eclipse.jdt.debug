/*******************************************************************************
 * Copyright (c) 2005, 2019 IBM Corporation and others.
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
 *     Paul Pazderski - Bug 343023: Clear the initial stack trace console message on first edit
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.console.TextConsoleViewer;

/**
 * provides the viewer for Java stack trace consoles
 */
public class JavaStackTraceConsoleViewer extends TextConsoleViewer {

	private JavaStackTraceConsole fConsole;
	private boolean fAutoFormat = false;

	/**
	 * Constructor
	 * @param parent the parent to add this viewer to
	 * @param console the console associated with this viewer
	 */
	public JavaStackTraceConsoleViewer(Composite parent, JavaStackTraceConsole console) {
		super(parent, console);
		fConsole = console;
		getTextWidget().setOrientation(SWT.LEFT_TO_RIGHT);

		IPreferenceStore fPreferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		fAutoFormat = fPreferenceStore.getBoolean(IJDIPreferencesConstants.PREF_AUTO_FORMAT_JSTCONSOLE);
	}

	/**
	 * Additional to the parents customization this override implements the clearing of the initial stack trace console content on first edit. It
	 * modifies the first event as if the user selected the whole initial content before typing.
	 *
	 * @param command
	 *            the document command representing the verify event
	 */
	@Override
	protected void customizeDocumentCommand(DocumentCommand command) {
		if (fConsole.showsUsageHint) {
			command.offset = 0;
			command.length = getDocument().getLength();
			command.caretOffset = command.length;
		}
		super.customizeDocumentCommand(command);
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewer#doOperation(int)
	 */
	@Override
	public void doOperation(int operation) {
		super.doOperation(operation);

		if (fAutoFormat && operation == ITextOperationTarget.PASTE) {
			fConsole.format();
		}
	}

	/**
	 * Sets the state of the autoformat action
	 * @param checked the desired state of the autoformat action
	 */
	public void setAutoFormat(boolean checked) {
		fAutoFormat = checked;
	}
}
