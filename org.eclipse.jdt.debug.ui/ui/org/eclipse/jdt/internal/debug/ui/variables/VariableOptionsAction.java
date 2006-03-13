/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDetailFormattersPreferencePage;
import org.eclipse.jdt.internal.debug.ui.JavaLogicalStructuresPreferencePage;
import org.eclipse.jdt.internal.debug.ui.JavaPrimitivesPreferencePage;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action which opens preference settings for Java variables.
 */
public class VariableOptionsAction implements IViewActionDelegate, IPropertyChangeListener {
	
	private IViewPart fPart;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view) {
    	fPart = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        IPreferenceNode details = new PreferenceNode("org.eclipse.jdt.debug.ui.JavaDetailFormattersPreferencePage", new JavaDetailFormattersPreferencePage()); //$NON-NLS-1$
        IPreferenceNode structures = new PreferenceNode("org.eclipse.jdt.debug.ui.JavaLogicalStructuresPreferencePage", new JavaLogicalStructuresPreferencePage()); //$NON-NLS-1$
        IPreferenceNode primitives = new PreferenceNode("org.eclipse.jdt.debug.ui.JavaPrimitivesPreferencePage", new JavaPrimitivesPreferencePage()); //$NON-NLS-1$
        PreferenceManager manager = new PreferenceManager();
        manager.addToRoot(details);
        manager.addToRoot(structures);
        manager.addToRoot(primitives);
        final PreferenceDialog dialog = new PreferenceDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), manager);
        final boolean [] result = new boolean[] { false };
        Preferences pluginPreferences = JDIDebugUIPlugin.getDefault().getPluginPreferences();
		pluginPreferences.addPropertyChangeListener(this);
        BusyIndicator.showWhile(JDIDebugUIPlugin.getStandardDisplay(), new Runnable() {
            public void run() {
                dialog.create();
                result[0]= (dialog.open() == Window.OK);
            }
        }); 
        pluginPreferences.removePropertyChangeListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (fPart instanceof IDebugView) {
			IDebugView view = (IDebugView) fPart;
			view.getViewer().refresh();
		}
		
	}
    
}
