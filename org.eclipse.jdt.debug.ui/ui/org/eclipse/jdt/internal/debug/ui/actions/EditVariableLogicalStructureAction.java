/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILogicalStructureType;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JavaLogicalStructure;
import org.eclipse.jdt.internal.debug.ui.EditLogicalStructureDialog;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Action which prompts the user to edit the logical structure that
 * is currently active on the given object.
 */
public class EditVariableLogicalStructureAction extends ActionDelegate implements IObjectActionDelegate {
    
    // Copied from VariablesView
    private static final String LOGICAL_STRUCTURE_TYPE_PREFIX= "VAR_LS_"; //$NON-NLS-1$
    
    /**
     * The currently selected variable in the variable's view.
     */
    private IJavaVariable fVariable= null;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    /**
     * Prompt the user to edit the logical structure associated with the currently
     * selected variable.
     */
    public void run(IAction action) {
        if (fVariable == null) {
            return;
        }
        try {
            ILogicalStructureType structure = getLogicalStructure(fVariable.getValue());
            if (structure != null && structure instanceof JavaLogicalStructure) {
                Shell shell= JDIDebugUIPlugin.getActiveWorkbenchShell();
                if (shell != null) {
                    EditLogicalStructureDialog dialog= new EditLogicalStructureDialog(shell, (JavaLogicalStructure) structure);
                    dialog.open();
                }
            }
        } catch (DebugException e) {
        }
    }
    
    /**
     * @see ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        Object element = ((IStructuredSelection) selection).getFirstElement();
        if (element instanceof IJavaVariable) {
            fVariable= (IJavaVariable) element;
        } else {
            fVariable= null;
        }
    }
    
    /**
     * Returns the logical structure currently associated with the given
     * value or <code>null</code> if none. 
     * @param value the value
     * @return the logical structure currently associated with the given
     *  value or <code>null</code> if none.
     */
    public static ILogicalStructureType getLogicalStructure(IValue value) {
        // This code is based on VariablesViewContentProvider#getLogicalValue(IValue)
        ILogicalStructureType type = null;
        ILogicalStructureType[] types = DebugPlugin.getLogicalStructureTypes(value);
        if (types.length > 0) {
            IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
            for (int i = 0; i < types.length; i++) {
                ILogicalStructureType tempType= types[i];
                if (tempType instanceof JavaLogicalStructure) {
                    JavaLogicalStructure javaStructureType= (JavaLogicalStructure) tempType;
                    if (!javaStructureType.isContributed()) { // can't edit contributed types
                        String key = LOGICAL_STRUCTURE_TYPE_PREFIX + types[i].getId();
                        int setting = store.getInt(key);
                        // 0 = never used, 1 = on, -1 = off
                        if (setting != 0) {
                            if (setting == 1) {
                                type = javaStructureType;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return type;
    }
}
