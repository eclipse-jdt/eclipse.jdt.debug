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

	protected void createActions() {
		super.createActions();
		
		IActionBars actionBars= getSite().getActionBars();
		fAutoFormat = new AutoFormatSettingAction(this);
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fAutoFormat);
	}

	protected TextConsoleViewer createViewer(Composite parent) {
		return new JavaStackTraceConsoleViewer(parent, (JavaStackTraceConsole) getConsole());
	}
}
