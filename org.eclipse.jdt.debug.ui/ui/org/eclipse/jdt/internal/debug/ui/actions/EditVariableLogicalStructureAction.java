/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILogicalStructureType;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JavaLogicalStructure;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JavaLogicalStructures;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JavaStructureErrorValue;
import org.eclipse.jdt.internal.debug.ui.EditLogicalStructureDialog;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Action which prompts the user to edit the logical structure that
 * is currently active on the given object.
 */
public class EditVariableLogicalStructureAction extends ActionDelegate implements IObjectActionDelegate {
    
    /**
     * The editable structure for the currently selected variable or
     * <code>null</code> if none.
     */
    private JavaLogicalStructure fStructure= null;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    /**
     * Prompt the user to edit the logical structure associated with the currently
     * selected variable.
     */
    @Override
	public void run(IAction action) {
        if (fStructure == null) {
            return;
        }
        Shell shell= JDIDebugUIPlugin.getActiveWorkbenchShell();
        if (shell != null) {
            EditLogicalStructureDialog dialog= new EditLogicalStructureDialog(shell, fStructure);
            if (dialog.open() == Window.OK) {
                JavaLogicalStructures.saveUserDefinedJavaLogicalStructures();
            }
        }
    }
    
    /**
     * @see ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
	public void selectionChanged(IAction action, ISelection selection) {
        fStructure= null;
        Object element = ((IStructuredSelection) selection).getFirstElement();
        if (element instanceof IJavaVariable) {
            try {
                IValue value= ((IJavaVariable) element).getValue();
                if (value instanceof JavaStructureErrorValue) {
                    value= ((JavaStructureErrorValue) value).getParentValue();
                }
                ILogicalStructureType type= getLogicalStructure(value);
                if (type instanceof JavaLogicalStructure) {
                    JavaLogicalStructure javaStructure= (JavaLogicalStructure) type;
                    if (!javaStructure.isContributed()) {
                        fStructure= javaStructure;
                    }
                }
            } catch (DebugException e) {
                JDIDebugUIPlugin.log(e.getStatus());
            }
        }
        action.setEnabled(fStructure != null);
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
            type= DebugPlugin.getDefaultStructureType(types);
        }
        return type;
    }
}
