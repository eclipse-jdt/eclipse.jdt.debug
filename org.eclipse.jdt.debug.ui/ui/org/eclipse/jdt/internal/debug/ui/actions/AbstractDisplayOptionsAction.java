/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Abstract implementation of an action which prompts the user to set some 
 * preferences for a debug view.
 */
public abstract class AbstractDisplayOptionsAction implements IViewActionDelegate, IActionDelegate2 {

	protected IViewPart fView;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		fView = view;
		applyPreferences();
	}
	
	protected void applyPreferences() {
		IDebugView view = (IDebugView)getView().getAdapter(IDebugView.class);
		if (view != null) {
			IDebugModelPresentation presentation = view.getPresentation(JDIDebugModel.getPluginIdentifier());
			if (presentation != null) {
			    String[][] preferenceInfo = getPreferenceInfo();
			    for (int i = 0; i < preferenceInfo.length; i++) {
                    applyPreference(preferenceInfo[i][0], preferenceInfo[i][1], presentation);
                }
			}
		}
	}
	
	/**
	 * Returns the preference information needed to apply preferences to a model
	 * presentation. Values are of the form:
	 * { PreferenceKey, PresentationAttribute }
     */
    protected abstract String[][] getPreferenceInfo();
    
    /**
     * Returns the dialog which should be presented to the user for
     * preference customization.
     */
    protected abstract Dialog getDialog();

	/**
	 * Sets the display attribute associated with the given preference.
	 * 
	 * @param preference preference key
	 * @param attribute attribute key
	 * @param presentation the model presentation to update
	 */
	protected abstract void applyPreference(String preference, String attribute, IDebugModelPresentation presentation);
    
    protected IViewPart getView() {
		return fView;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		// open dialog
		int res = getDialog().open();
		if (res == Window.OK) {
			final Viewer viewer = getViewer();
			BusyIndicator.showWhile(viewer.getControl().getDisplay(), new Runnable() {
				public void run() {
					applyPreferences();
					viewer.refresh();
					JDIDebugUIPlugin.getDefault().savePluginPreferences();						
				}
			});			
		}
	}
	
	protected Viewer getViewer() {
		IDebugView view = (IDebugView)getView().getAdapter(IDebugView.class);
		if (view != null) {
			return view.getViewer();
		}		
		return null;
	}
	
	/**
	 * Returns the value of this filters preference (on/off) for the given
	 * view.
	 * 
	 * @param part
	 * @return boolean
	 */
	public static boolean getBooleanPreferenceValue(String id, String preference) {
		String compositeKey = id + "." + preference; //$NON-NLS-1$
		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		boolean value = false;
		if (store.contains(compositeKey)) {
			value = store.getBoolean(compositeKey);
		} else {
			value = store.getBoolean(preference);
		}
		return value;		
	}
	
	/**
	 * Returns the value of this preference for the given view.
	 * 
	 * @param part
	 * @return String
	 */
	public static String getStringPreferenceValue(String id, String preference) {
		String compositeKey = id + "." + preference; //$NON-NLS-1$
		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		String value= ""; //$NON-NLS-1$
		if (store.contains(compositeKey)) {
			value = store.getString(compositeKey);
		} else {
			value = store.getString(preference);
		}
		return value;		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
