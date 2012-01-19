/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.heapwalking;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.core.HeapWalkingManager;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action delegate that turns on/off references being displayed as variables in the view.
 * 
 * @since 3.3
 */
public class AllReferencesInViewActionDelegate implements IPreferenceChangeListener, IActionDelegate2, IViewActionDelegate {

	private IAction fAction;
	private IDebugView fView;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		HeapWalkingManager.getDefault().setShowReferenceInVarView(action.isChecked());
		// If the current target doesn't support instance retrieval, warn the user that turning the option on will not do anything.
		if (action.isChecked() && fView.getViewer() != null){
			if (fView.getViewer().getInput() instanceof IJavaStackFrame){
				if (!HeapWalkingManager.supportsHeapWalking(fView.getViewer().getInput())){
					JDIDebugUIPlugin.statusDialog(Messages.AllReferencesInViewActionDelegate_0,new Status(IStatus.WARNING,JDIDebugUIPlugin.getUniqueIdentifier(),Messages.AllReferencesInViewActionDelegate_1));
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		fAction = action;
		action.setChecked(HeapWalkingManager.getDefault().isShowReferenceInVarView());
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
		if(prefs != null) {
			prefs.addPreferenceChangeListener(this);
		}
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		if (view instanceof IDebugView){
			fView = (IDebugView)view;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
		if(prefs != null) {
			prefs.removePreferenceChangeListener(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
	 */
	public void preferenceChange(PreferenceChangeEvent event) {
		if (JDIDebugPlugin.PREF_SHOW_REFERENCES_IN_VAR_VIEW.equals(event.getKey()) || JDIDebugPlugin.PREF_ALL_REFERENCES_MAX_COUNT.equals(event.getKey())){
			if (fAction != null){
				fAction.setChecked(HeapWalkingManager.getDefault().isShowReferenceInVarView());
				fView.getViewer().refresh();
			}
		}
	}
}
