package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * An action delegate that toggles the state of its viewer to
 * show/hide qualified names.
 */
public class ShowQualifiedAction extends VariableFilterAction {
	
	/**
	 * @see VariableFilterAction#getPreferenceKey()
	 */
	protected String getPreferenceKey() {
		return IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES; 
	}	

	/**
	 * This method is not actually called - this action is not a filter. Instead
	 * it sets an attribute on the viewer's model presentation.
	 * 
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		setValue(action.isChecked());
		
		final StructuredViewer viewer = getStructuredViewer();
		ILabelProvider labelProvider= (ILabelProvider)viewer.getLabelProvider();
		if (labelProvider instanceof IDebugModelPresentation) {
			IDebugModelPresentation debugLabelProvider= (IDebugModelPresentation)labelProvider;
			debugLabelProvider.setAttribute(JDIModelPresentation.DISPLAY_QUALIFIED_NAMES, (getValue() ? Boolean.TRUE : Boolean.FALSE));
			BusyIndicator.showWhile(viewer.getControl().getDisplay(), new Runnable() {
				public void run() {
					viewer.refresh();
					IPreferenceStore store = getPreferenceStore();
					String key = getView().getSite().getId() + "." + getPreferenceKey(); //$NON-NLS-1$
					store.setValue(key, getValue());
					JDIDebugUIPlugin.getDefault().savePluginPreferences();						
				}
			});
		}		
	}	
}