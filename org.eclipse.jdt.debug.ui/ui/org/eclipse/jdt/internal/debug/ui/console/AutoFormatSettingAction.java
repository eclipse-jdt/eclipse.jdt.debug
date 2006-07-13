package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;

public class AutoFormatSettingAction extends Action {
	private JavaStackTraceConsolePage fPage;
	private IPreferenceStore fPreferenceStore;

	public AutoFormatSettingAction(JavaStackTraceConsolePage page) {
		super(ConsoleMessages.AutoFormatSettingAction_0, SWT.TOGGLE); 
		fPage = page;
		
		setToolTipText(ConsoleMessages.AutoFormatSettingAction_1);  
		setImageDescriptor(JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_ELCL_AUTO_FORMAT));
		setHoverImageDescriptor(JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_ELCL_AUTO_FORMAT));
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaDebugHelpContextIds.STACK_TRACE_CONSOLE);
        
        fPreferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
        boolean checked = fPreferenceStore.getBoolean(IJDIPreferencesConstants.PREF_AUTO_FORMAT_JSTCONSOLE);
		setChecked(checked);
	}

	public void run() {
		boolean checked = isChecked();
		JavaStackTraceConsoleViewer viewer = (JavaStackTraceConsoleViewer) fPage.getViewer();
		viewer.setAutoFormat(checked);
		fPreferenceStore.setValue(IJDIPreferencesConstants.PREF_AUTO_FORMAT_JSTCONSOLE, checked);
	}
}
