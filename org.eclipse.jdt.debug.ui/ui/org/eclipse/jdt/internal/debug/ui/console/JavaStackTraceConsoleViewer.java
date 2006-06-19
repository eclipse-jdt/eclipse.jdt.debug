package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.console.TextConsoleViewer;

public class JavaStackTraceConsoleViewer extends TextConsoleViewer {

	private JavaStackTraceConsole fConsole;
	private boolean fAutoFormat = false;

	public JavaStackTraceConsoleViewer(Composite parent, JavaStackTraceConsole console) {
		super(parent, console);
		fConsole = console;
		
		IPreferenceStore fPreferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
        fAutoFormat = fPreferenceStore.getBoolean(IJDIPreferencesConstants.PREF_AUTO_FORMAT_JSTCONSOLE);
	}

	public void doOperation(int operation) {
		super.doOperation(operation);

		if (fAutoFormat && operation == ITextOperationTarget.PASTE)
			fConsole.format();
	}

	public void setAutoFormat(boolean checked) {
		fAutoFormat = checked;
	}
}
