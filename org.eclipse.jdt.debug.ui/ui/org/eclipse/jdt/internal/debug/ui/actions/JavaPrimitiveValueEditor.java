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

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.actions.IVariableValueEditor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

/**
 * A variable value editor that prompts the user to set a primitive's value.
 */
public class JavaPrimitiveValueEditor implements IVariableValueEditor {
    
    /**
     * The signature of the edited variable.
     */
    private String fSignature= null;

    /**
     * Creates a new editor for a variable with the given signature
     * @param signature the signature of the primitive to be edited
     */
    public JavaPrimitiveValueEditor(String signature) {
        fSignature= signature;
    }
    
    private JavaPrimitiveValueEditor() {
        // Do not call.
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.actions.IVariableValueEditor#editVariable(org.eclipse.debug.core.model.IVariable, org.eclipse.swt.widgets.Shell)
     */
    public boolean editVariable(IVariable variable, Shell shell) {
        try {
            String name= variable.getName();
            String title= ActionMessages.getString("JavaPrimitiveValueEditor.0"); //$NON-NLS-1$
            String message= MessageFormat.format(ActionMessages.getString("JavaPrimitiveValueEditor.1"), new String[] {name}); //$NON-NLS-1$
            String initialValue= variable.getValue().getValueString();
            IInputValidator validator= new PrimitiveValidator();
            InputDialog dialog= new InputDialog(shell, title, message, initialValue, validator);
            if (dialog.open() == Window.OK) {
                String stringValue = dialog.getValue();
                variable.setValue(stringValue);
            }
        } catch (DebugException e) {
            DebugUIPlugin.errorDialog(shell, ActionMessages.getString("JavaPrimitiveValueEditor.2"), ActionMessages.getString("JavaPrimitiveValueEditor.3"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return true;
    }
    
    /**
     * Input validator for primitive types
     */
    protected class PrimitiveValidator implements IInputValidator {
        /* (non-Javadoc)
         * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
         */
        public String isValid(String newText) {
            String type= null;
            switch (fSignature.charAt(0)) {
	        	case 'B':
	        	    try {
		        	    Byte.parseByte(newText);
	                } catch (NumberFormatException e) {
	                    type= "byte"; //$NON-NLS-1$
	                }
	                break;
	        	case 'C':
	        	    if (newText.length() != 1) {
	        	        type="char"; //$NON-NLS-1$
	        	    }
	                break;
	        	case 'D':
	        	    try {
	                    Double.parseDouble(newText);
	                } catch (NumberFormatException e) {
	        	        type="double"; //$NON-NLS-1$
	                }
	                break;
	        	case 'F':
	        	    try {
	                    Float.parseFloat(newText);
	                } catch (NumberFormatException e) {
	        	        type="float"; //$NON-NLS-1$
	                }
	                break;
            	case 'I':
            	    try {
                        Integer.parseInt(newText);
                    } catch (NumberFormatException e) {
	        	        type="int"; //$NON-NLS-1$
                    }
                    break;
	        	case 'J':
	        	    try {
	                    Long.parseLong(newText);
	                } catch (NumberFormatException e) {
	        	        type="long"; //$NON-NLS-1$
	                }
	                break;
	        	case 'S':
	        	    try {
	                    Short.parseShort(newText);
	                } catch (NumberFormatException e) {
	        	        type="short"; //$NON-NLS-1$
	                }
	                break;
	        	case 'Z':
	        	    try {
	                    Boolean.parseBoolean(newText);
	                } catch (NumberFormatException e) {
	        	        type="boolean"; //$NON-NLS-1$
	                }
	                break;
            }
            if (type != null) {
                return MessageFormat.format(ActionMessages.getString("JavaPrimitiveValueEditor.4"), new String[] { type }); //$NON-NLS-1$
            }
            return null;
        }
    }

}
