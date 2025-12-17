/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.actions.IVariableValueEditor;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.DebugUIMessages;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

/**
 * Variable editor that prompts the user to edit Java variables
 */
public class JavaVariableValueEditor implements IVariableValueEditor {

    @Override
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
		if (!isAllowedToModifyValue(variable)) {
			// return true to avoid further processing
			return true;
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

    @Override
	public boolean saveVariable(IVariable variable, String expression, Shell shell) {
		if (!isAllowedToModifyValue(variable)) {
			// return true to avoid further processing
			return true;
		}

        // set the value of chars directly if expression is a single character (not an expression to evaluate)
    	if (expression.length() == 1 && variable instanceof IJavaVariable javaVariable){
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

	/**
	 * @return {@code false} to prohibit editing a variable
	 */
	protected boolean isAllowedToModifyValue(IVariable variable) {
		if (variable instanceof IJavaModifiers modifiers) {
			boolean allowed = isAllowedToModifyFinalValue(modifiers);
			if (!allowed) {
				// prohibit editing a variable that is declared as final
				return false;
			}
		}
		return true;
	}

	protected boolean isAllowedToModifyFinalValue(IJavaModifiers variable) {
		IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		String key = IJDIPreferencesConstants.PREF_PROMPT_BEFORE_MODIFYING_FINAL_FIELDS;
		if (!preferenceStore.getBoolean(key)) {
			return true;
		}
		try {
			if (!variable.isFinal()) {
				return true;
			}
		} catch (DebugException e) {
			JDIDebugUIPlugin.log(e);
		}
		return promptIfAllowedToModifyFinalValue(preferenceStore, key);
	}

	protected boolean promptIfAllowedToModifyFinalValue(IPreferenceStore preferenceStore, String key) {
		boolean dontShowAgain = false;
		final MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(
				JDIDebugUIPlugin.getActiveWorkbenchShell(),
				DebugUIMessages.JavaVariableValueEditor_prompt_before_final_value_change_title,
				DebugUIMessages.JavaVariableValueEditor_prompt_before_final_value_change_message,
				DebugUIMessages.JavaVariableValueEditor_prompt_before_final_value_change_toggle_message,
				dontShowAgain,
				preferenceStore,
				key);
		return dialog.getReturnCode() == IDialogConstants.YES_ID;
	}

    public static String getSignature(IVariable variable) throws DebugException {
        String signature= null;
		IJavaVariable javaVariable = variable.getAdapter(IJavaVariable.class);
        if (javaVariable != null) {
                signature = javaVariable.getSignature();
        }
        return signature;
    }

}
