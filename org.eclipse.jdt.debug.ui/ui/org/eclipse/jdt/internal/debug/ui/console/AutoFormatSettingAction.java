package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;

public class AutoFormatSettingAction extends Action {
	private JavaStackTraceConsolePage fPage;
	private IPreferenceStore fPreferenceStore;

	public AutoFormatSettingAction(JavaStackTraceConsolePage page) {
		super("Auto Format", SWT.TOGGLE); 
		fPage = page;
		
		setToolTipText("Auto Format");  
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DETAIL_PANE_HIDE));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_DETAIL_PANE_HIDE));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_DETAIL_PANE_HIDE));
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
