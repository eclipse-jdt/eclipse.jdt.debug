package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IViewPart;

/**
 * An action delegate that toggles the state of its viewer to
 * show/hide qualified names.
 */
public class ShowQualifiedAction extends ToggleDelegateAction {

	protected void valueChanged(boolean on) {
		if (getViewer().getControl().isDisposed()) {
			return;
		}		
		ILabelProvider labelProvider= (ILabelProvider)getViewer().getLabelProvider();
		if (labelProvider instanceof IDebugModelPresentation) {
			IDebugModelPresentation debugLabelProvider= (IDebugModelPresentation)labelProvider;
			debugLabelProvider.setAttribute(JDIModelPresentation.DISPLAY_QUALIFIED_NAMES, (on ? Boolean.TRUE : Boolean.FALSE));
			BusyIndicator.showWhile(getViewer().getControl().getDisplay(), new Runnable() {
				public void run() {
					getViewer().refresh();
				}
			});
		}
	}

	
	public void init(IViewPart viewPart) {
		super.init(viewPart);
		getAction().setChecked(JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES));
	}
}