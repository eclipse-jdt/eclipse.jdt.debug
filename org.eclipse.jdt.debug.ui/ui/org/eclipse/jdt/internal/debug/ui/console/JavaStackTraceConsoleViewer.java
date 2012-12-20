/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.ITextOperationTarget;

import org.eclipse.ui.console.TextConsoleViewer;

import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;

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
	 * @see org.eclipse.jface.text.source.SourceViewer#doOperation(int)
	 */
	@Override
	public void doOperation(int operation) {
		super.doOperation(operation);

		if (fAutoFormat && operation == ITextOperationTarget.PASTE)
			fConsole.format();
	}

	/**
	 * Sets the state of the autoformat action
	 * @param checked the desired state of the autoformat action
	 */
	public void setAutoFormat(boolean checked) {
		fAutoFormat = checked;
	}
}
