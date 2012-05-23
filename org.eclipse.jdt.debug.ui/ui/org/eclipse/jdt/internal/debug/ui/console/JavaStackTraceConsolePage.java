/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.console.TextConsoleViewer;

public class JavaStackTraceConsolePage extends TextConsolePage {
	
	private AutoFormatSettingAction fAutoFormat;

	public JavaStackTraceConsolePage(TextConsole console, IConsoleView view) {
		super(console, view);
	}

	@Override
	protected void createActions() {
		super.createActions();
		
		IActionBars actionBars= getSite().getActionBars();
		fAutoFormat = new AutoFormatSettingAction(this);
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fAutoFormat);
	}

	@Override
	protected TextConsoleViewer createViewer(Composite parent) {
		return new JavaStackTraceConsoleViewer(parent, (JavaStackTraceConsole) getConsole());
	}
}
