/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugOptionsManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * This abstract class defines the behavior common to actions that allow the
 * user to add step filters dynamically, as they are debugging.
 * 
 * @since 2.1
 */
public abstract class AbstractAddStepFilterAction extends ObjectActionDelegate {

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		
		// Make sure there is a current selection
		IStructuredSelection selection= getCurrentSelection();
		if (selection == null) {
			return;
		}

		// For each selected stack frame, add a corresponding active step filter
		Iterator<IJavaStackFrame> itr = selection.iterator();
		while (itr.hasNext()) {
			IJavaStackFrame frame = itr.next();
			String pattern = generateStepFilterPattern(frame);
			if (pattern != null) {
				addActiveStepFilter(pattern);						
			}
		}	    
	}
	
	/**
	 * Make the specified pattern an active step filter.
	 */
	private void addActiveStepFilter(String pattern) {
		
		// Get the active & inactive filter preferences and convert them to Lists
		IPreferenceStore prefStore = getPreferenceStore();
		String[] activeArray = JavaDebugOptionsManager.parseList(prefStore.getString(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST));
		String[] inactiveArray = JavaDebugOptionsManager.parseList(prefStore.getString(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST));
		List<String> activeList = new ArrayList<String>(Arrays.asList(activeArray));
		List<String> inactiveList = new ArrayList<String>(Arrays.asList(inactiveArray));
		
		// If the pattern is already in the active list, there's nothing to do
		// (it can't/shouldn't be in the inactive list)
		if (activeList.contains(pattern)) {
			return;
		}
		
		// Add the pattern to the active list and update the preference store
		activeList.add(pattern);
		String activePref = JavaDebugOptionsManager.serializeList(activeList.toArray(new String[activeList.size()]));
		prefStore.setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, activePref);
	
		// If the pattern was present in the inactive list, remove it since we just
		// added it to the active list
		if (inactiveList.contains(pattern)) {
			inactiveList.remove(pattern);
			String inactivePref = JavaDebugOptionsManager.serializeList(inactiveList.toArray(new String[inactiveList.size()]));
			prefStore.setValue(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST, inactivePref);			
		}
	}
	
	/**
	 * Convenience method to get the preference store.
	 */
	private IPreferenceStore getPreferenceStore() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore();
	}
	
	/**
	 * Generate an appropriate String pattern for the specified Java stack
	 * frame or return null if generation failed.  For example, the pattern for
	 * a type might look like, "com. example.MyType", while the pattern for a
	 * package might look like, "com. example.*".
	 * 
	 * @param frame the Java stack frame used to generate a String pattern
	 * @return String the pattern or <code>null</code> if one could not be
	 * generated
	 */
	protected abstract String generateStepFilterPattern(IJavaStackFrame frame);

}
