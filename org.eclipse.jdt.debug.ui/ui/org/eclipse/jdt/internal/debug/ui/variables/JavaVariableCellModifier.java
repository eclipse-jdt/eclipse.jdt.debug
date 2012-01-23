/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.elements.adapters.DefaultVariableCellModifier;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * @since 3.2
 * 
 */
public class JavaVariableCellModifier extends DefaultVariableCellModifier {
	
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.elements.adapters.DefaultVariableCellModifier#canModify(java.lang.Object,
     *      java.lang.String)
     */
    @Override
	public boolean canModify(Object element, String property) {
        if (IDebugUIConstants.COLUMN_ID_VARIABLE_VALUE.equals(property)) {
            if (element instanceof IJavaVariable) {
                IJavaVariable var = (IJavaVariable) element;
                if (var.supportsValueModification()){
	                try {
	                    String signature = var.getSignature();
	                    if (signature != null) {
		                    if (signature.length() == 1) {
		                        // primitive
		                        return true;
		                    }
		                    return signature.equals("Ljava/lang/String;"); //$NON-NLS-1$
	                    }
	                } catch (DebugException e) {
	                }
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.elements.adapters.DefaultVariableCellModifier#getValue(java.lang.Object,
     *      java.lang.String)
     */
    @Override
	public Object getValue(Object element, String property) {
        if (IDebugUIConstants.COLUMN_ID_VARIABLE_VALUE.equals(property)) {
            if (element instanceof IJavaVariable) {
                IJavaVariable var = (IJavaVariable) element;
                if (isBoolean(var)) {
                    try {
                        if (var.getValue().getValueString().equals(Boolean.toString(true))) {
                            return new Integer(0);
                        }
                        return new Integer(1);
                    } catch (DebugException e) {
                    }
                }
            }
        }
        return super.getValue(element, property);
    }

    @Override
	public void modify(Object element, String property, Object value) {
        Object oldValue = getValue(element, property);
        if (!value.equals(oldValue)) {
            if (IDebugUIConstants.COLUMN_ID_VARIABLE_VALUE.equals(property)) {
                if (element instanceof IJavaVariable) {
                    IJavaVariable var = (IJavaVariable) element;
                    if (isBoolean(var)) {
                    	if (value instanceof Integer) {
	                        switch (((Integer) value).intValue()) {
	                        case 0:
	                            super.modify(element, property, Boolean.toString(true));
	                            return;
	                        case 1:
	                            super.modify(element, property, Boolean.toString(false));
	                            return;
	                        default:
	                        	// invalid value
	                        	return;
	                        }
                    	}
                		// invalid value
                		return;
                    }
                }
            }
            super.modify(element, property, value);
        }
    }

    /**
     * Returns whether the given variable is a boolean.
     * 
     * @param variable
     * @return
     */
    public static boolean isBoolean(IJavaVariable variable) {
        try {
            String signature = variable.getSignature();
            return (signature.length() == 1 && signature.charAt(0) == Signature.C_BOOLEAN);
        } catch (DebugException e) {
        }
        return false;
    }

}
