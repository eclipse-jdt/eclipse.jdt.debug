package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * An view filter action that shows/hides static fields in a view
 */
public class ShowStaticFieldsAction extends ToggleFilterAction {

	/**
	 * The filter this action applies to the viewer
	 */
	private static final ViewerFilter fgStaticFilter= new StaticFilter();
	
	public ShowStaticFieldsAction() {
	}

	/**
	 * @see ToggleFilterAction#getViewerFilter()
	 */
	protected ViewerFilter getViewerFilter() {
		return fgStaticFilter;
	}
	
	/**
	 * @see ToggleDelegateAction#initActionId()
	 */
	protected void initActionId() {
		fId= JDIDebugUIPlugin.getUniqueIdentifier() + getView().getSite().getId() + ".ShowStaticFieldsAction"; //$NON-NLS-1$
	}
	
	protected void setAction(IAction action) {
		super.setAction(action);
		action.setChecked(JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_SHOW_STATIC_FIELDS));
	}
}

class StaticFilter extends ViewerFilter {
		
	/**
	 * @see ViewerFilter#select(Viewer, Object, Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IAdaptable) {
			IJavaVariable var= (IJavaVariable) ((IAdaptable) element).getAdapter(IJavaVariable.class);
			if (var != null) {
				if (element.equals(viewer.getInput())) {
					//never filter out the root
					return true;
				}
				try {
					return !var.isStatic();
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e);
					return true;
				}
			}
		}
		return true;
	}

}
