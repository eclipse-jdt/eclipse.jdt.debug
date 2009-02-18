/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
import org.eclipse.debug.ui.actions.IVariableValueEditor;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
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
	        JDIDebugUIPlugin.errorDialog(shell, ActionMessages.JavaVariableValueEditor_0, ActionMessages.JavaVariableValueEditor_1, e); // 
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
        // set the value of chars directly if expression is a single character (not an expression to evaluate)
    	if (expression.length() == 1 && variable instanceof IJavaVariable){
    		IJavaVariable javaVariable = (IJavaVariable)variable;
    		try {
				if (javaVariable.getJavaType() != null && javaVariable.getJavaType().getSignature() == Signature.SIG_CHAR){
					javaVariable.setValue(expression);
					return true;
				}
			} catch (DebugException e) {
				JDIDebugUIPlugin.statusDialog(e.getStatus()); 
			}
    	}
    	
    	// support expressions for primitives as well as literals
        IVariableValueEditor editor= new JavaObjectValueEditor();
        return editor.saveVariable(variable, expression, shell);
    }
    
    public static String getSignature(IVariable variable) throws DebugException {
        String signature= null;
        IJavaVariable javaVariable = (IJavaVariable) variable.getAdapter(IJavaVariable.class);
        if (javaVariable != null) {
                signature = javaVariable.getSignature();
        }
        return signature;
    }

}
