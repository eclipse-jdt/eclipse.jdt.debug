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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.actions.IVariableValueEditor;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
import org.eclipse.swt.widgets.Shell;

/**
 * Variable editor that prompts the user to edit Java variables
 */
public class JavaVariableValueEditor implements IVariableValueEditor {

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.actions.IVariableValueEditor#editVariable(org.eclipse.debug.core.model.IVariable, org.eclipse.swt.widgets.Shell)
     */
    public boolean editVariable(IVariable variable, Shell shell) {
        IJavaVariable javaVariable = (IJavaVariable) variable.getAdapter(IJavaVariable.class);
        if (javaVariable == null) {
            return false;
        }
        String signature= null;
        try {
            IJavaType javaType = javaVariable.getJavaType();
            signature = javaType.getSignature();
        } catch (DebugException e) {
            DebugUIPlugin.errorDialog(shell, ActionMessages.getString("JavaVariableValueEditor.0"), ActionMessages.getString("JavaVariableValueEditor.1"), e); //$NON-NLS-1$ //$NON-NLS-2$
            return true;
        }
        if (JDIModelPresentation.isObjectValue(signature)) {
            JavaObjectValueEditor editor= new JavaObjectValueEditor();
            return editor.editVariable(javaVariable, shell);
        }
        // Primitive variabel
        JavaPrimitiveValueEditor editor= new JavaPrimitiveValueEditor(signature);
        return editor.editVariable(javaVariable, shell);
    }

}
