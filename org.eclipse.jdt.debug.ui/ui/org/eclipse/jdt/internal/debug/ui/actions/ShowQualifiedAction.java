/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * An action delegate that toggles the state of its viewer to
 * show/hide qualified names.
 */
public class ShowQualifiedAction extends ViewFilterAction {
	
	/**
	 * @see ViewFilterAction#getPreferenceKey()
	 */
	@Override
	protected String getPreferenceKey() {
		return IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES; 
	}	

	/**
	 * This method is not actually called - this action is not a filter. Instead
	 * it sets an attribute on the viewer's model presentation.
	 * 
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		final StructuredViewer viewer = getStructuredViewer();
		IDebugView view = (IDebugView)getView().getAdapter(IDebugView.class);
		if (view != null) {
			IDebugModelPresentation pres = view.getPresentation(JDIDebugModel.getPluginIdentifier());
			if (pres != null) {
				pres.setAttribute(JDIModelPresentation.DISPLAY_QUALIFIED_NAMES, (getValue() ? Boolean.TRUE : Boolean.FALSE));
				BusyIndicator.showWhile(viewer.getControl().getDisplay(), new Runnable() {
					public void run() {
						viewer.refresh();
						IPreferenceStore store = getPreferenceStore();
						String key = getView().getSite().getId() + "." + getPreferenceKey(); //$NON-NLS-1$
						store.setValue(key, getValue());				
					}
				});
			}
		}		
	}	
}
