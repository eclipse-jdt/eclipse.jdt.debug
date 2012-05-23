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
package org.eclipse.jdt.internal.debug.ui.variables;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Provides static methods for filtering the content in Java views.  By filtering in the
 * content provider rather than in the model the UI does not get updated until after the
 * filtering is done.
 * 
 * @since 3.4
 * @see JavaVariableContentProvider
 * @see JavaExpressionContentProvider
 * @see JavaStackFrameContentProvider
 */
public class JavaContentProviderFilter {

	/**
	 * Filters the given array of variables based on preference settings.  Currently
	 * removes static variables and constants if the appropriate preference is set.
	 * @param variables array of variables to filter
	 * @param context the current view's context, required because the preferences are view specific
	 * @return array of filtered variables
	 * @throws DebugException
	 */
	public static Object[] filterVariables(Object[] variables, IPresentationContext context) throws DebugException {
		boolean filterStatics = !includeStatic(context);
		boolean filterConstants = !includeConstants(context);

		if (filterStatics || filterConstants) {
			List<Object> keep = new ArrayList<Object>(variables.length);
			for (int i = 0; i < variables.length; i++) {
				boolean filter = false;
				if (variables[i] instanceof IJavaVariable){
					IJavaVariable var = (IJavaVariable)variables[i];
					if (var.isStatic()){
						if (var.isFinal()){
							filter = filterConstants;
						} else {
							filter = filterStatics;
						}
					}
				}
				if (!filter){
					keep.add(variables[i]);
				}
			}
			return keep.toArray(new Object[keep.size()]);
		}
		return variables;
	}

	/**
	 * Returns whether static variables should be displayed in the view
	 * identified by the given presentation context.  Checks for a preference
	 * that is specific to the presentation context.
	 * 
	 * @param context the context of the view being displayed
	 * @return whether static variable should be displayed
	 */
	private static boolean includeStatic(IPresentationContext context){
		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		String statics = context.getId() + "." + IJDIPreferencesConstants.PREF_SHOW_STATIC_VARIABLES; //$NON-NLS-1$
		return store.getBoolean(statics);
	}

	/**
	 * Returns whether constants should be displayed in the view
	 * identified by the given presentation context.  Checks for a preference
	 * that is specific to the presentation context.
	 * 
	 * @param context the context of the view being displayed
	 * @return whether constants should be displayed
	 */
	private static boolean includeConstants(IPresentationContext context){
		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		String constants = context.getId() + "." + IJDIPreferencesConstants.PREF_SHOW_CONSTANTS; //$NON-NLS-1$
		return store.getBoolean(constants); 
	}
}
