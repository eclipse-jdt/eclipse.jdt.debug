package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * An view filter action that shows/hides final fields in a view
 */
public class ShowFinalFieldsAction extends ToggleFilterAction {

	/**
	 * The filter this action applies to the viewer
	 */
	private FinalFilter fFinalFilter;

	class FinalFilter extends ViewerFilter {
		
		/**
		 * @see ViewerFilter#select(Viewer, Object, Object)
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof IAdaptable) {
				IJavaVariable var= (IJavaVariable) ((IAdaptable) element).getAdapter(IJavaVariable.class);
				if (var != null) {
					if (element.equals(getViewer().getInput())) {
						//never filter out the root
						return true;
					}
					try {
						return !var.isFinal();
					} catch (DebugException e) {
						JDIDebugUIPlugin.log(e.getStatus());
						return true;
					}
				}
			}
			return true;
		}

	}

	public ShowFinalFieldsAction() {
		setViewerFilter(new FinalFilter());
	}
	
	protected String getActionId() {
		return JDIDebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier() + ".ShowFinalFieldsAction"; //$NON-NLS-1$
	}

	/**
	 * @see ToggleFilterAction#getViewerFilter()
	 */
	protected ViewerFilter getViewerFilter() {
		return fFinalFilter;
	}
	
	protected void setViewerFilter(FinalFilter filter) {
		fFinalFilter= filter;
	}

	/**
	 * @see ToggleDelegateAction#getShowText()
	 */
	protected String getShowText() {
		return ActionMessages.getString("ShowFinalFieldsAction.Show_Final_Fields_1"); //$NON-NLS-1$
	}

	/**
	 * @see ToggleDelegateAction#getHideText()
	 */
	protected String getHideText() {
		return ActionMessages.getString("ShowFinalFieldsAction.Hide_Final_Fields_2"); //$NON-NLS-1$
	}
}
