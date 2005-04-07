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
        String signature= null;
        try {
            signature= getSignature(variable);
	    } catch (DebugException e) {
	        DebugUIPlugin.errorDialog(shell, ActionMessages.JavaVariableValueEditor_0, ActionMessages.JavaVariableValueEditor_1, e); //$NON-NLS-1$ //$NON-NLS-2$
	    }
	    if (signature == null) {
	        return false;
	    }
	    IVariableValueEditor editor;
        if (JDIModelPresentation.isObjectValue(signature)) {
            editor= new JavaObjectValueEditor();
        } else {
            // Primitive variable
            editor= new JavaPrimitiveValueEditor(signature);
        }
        return editor.editVariable(variable, shell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.actions.IVariableValueEditor#saveVariable(org.eclipse.debug.core.model.IVariable, java.lang.String, org.eclipse.swt.widgets.Shell)
     */
    public boolean saveVariable(IVariable variable, String expression, Shell shell) {
        try {
	        if (JDIModelPresentation.isObjectValue(getSignature(variable))) {
	            IVariableValueEditor editor= new JavaObjectValueEditor();
	            return editor.saveVariable(variable, expression, shell);
	        }
	    } catch (DebugException e) {
	        DebugUIPlugin.errorDialog(shell, ActionMessages.JavaVariableValueEditor_0, ActionMessages.JavaVariableValueEditor_1, e); //$NON-NLS-1$ //$NON-NLS-2$
	    }
        return false;
    }
    
    public static String getSignature(IVariable variable) throws DebugException {
        String signature= null;
        IJavaVariable javaVariable = (IJavaVariable) variable.getAdapter(IJavaVariable.class);
        if (javaVariable != null) {
                IJavaType javaType = javaVariable.getJavaType();
                signature = javaType.getSignature();
        }
        return signature;
    }

}
