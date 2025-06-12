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
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTInstructionCompiler;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

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

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.actions.IVariableValueEditor#editVariable(org.eclipse.debug.core.model.IVariable, org.eclipse.swt.widgets.Shell)
     */
    @Override
	public boolean editVariable(IVariable variable, Shell shell) {
        try {
            String name= variable.getName();
            String title= ActionMessages.JavaPrimitiveValueEditor_0;
			String message = NLS.bind(ActionMessages.JavaPrimitiveValueEditor_1, name);
            String initialValue= variable.getValue().getValueString();
            PrimitiveValidator validator= new PrimitiveValidator();
            InputDialog dialog= new InputDialog(shell, title, message, initialValue, validator){
            	@Override
				protected Control createDialogArea(Composite parent) {
            		IWorkbench workbench = PlatformUI.getWorkbench();
            		workbench.getHelpSystem().setHelp(parent, IJavaDebugHelpContextIds.DEFAULT_INPUT_DIALOG);
            		return super.createDialogArea(parent);
            	}
            };
            if (dialog.open() == Window.OK) {
                String stringValue = dialog.getValue();
                stringValue = formatValue(stringValue);
                if (stringValue.length() > 1 && stringValue.charAt(0) == '\\') {
                	// Compute value of octal of hexadecimal escape sequence
                	int i= validator.getEscapeValue(stringValue);
                	if (i != Integer.MAX_VALUE) {
                		stringValue= new String(new char[] { (char) i });
                	}
                }
                variable.setValue(stringValue);
            }
        } catch (DebugException e) {
            JDIDebugUIPlugin.errorDialog(shell, ActionMessages.JavaPrimitiveValueEditor_2, ActionMessages.JavaPrimitiveValueEditor_3, e); //
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.actions.IVariableValueEditor#saveVariable(org.eclipse.debug.core.model.IVariable, java.lang.String, org.eclipse.swt.widgets.Shell)
     */
    @Override
	public boolean saveVariable(IVariable variable, String expression, Shell shell) {
        return false;
    }

    String formatValue(String value) {
    	try {
	    	switch (fSignature.charAt(0)) {
		    	case 'I':
	    	    	return Integer.toString(ASTInstructionCompiler.parseIntValue(value));
		    	case 'J':
	    	    	return Long.toString(ASTInstructionCompiler.parseLongValue(value));
		    	case 'S':
	                return Short.toString(ASTInstructionCompiler.parseShortValue(value));
		    	case 'F':
		    	case 'D':
		    		return ASTInstructionCompiler.removePrefixZerosAndUnderscores(value, false);
		    	case 'B':
		    		return Byte.toString(ASTInstructionCompiler.parseByteValue(value));
		    }
    	}
    	catch(NumberFormatException nfe) {}
    	return value;
    }

    /**
     * Input validator for primitive types
     */
    protected class PrimitiveValidator implements IInputValidator {
        /* (non-Javadoc)
         * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
         */
        @Override
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
	        		if (newText.length() > 1 && newText.charAt(0) == '\\') {
	        			// Possibly an escaped character
	        			if (isSpecialCharacter(newText) ||
	        					isOctalEscape(newText) ||
	        					isUnicode(newText)) {
	        				break;
	        			}
	        		}
	        		if (newText.length() != 1) {
	        	        type="char"; //$NON-NLS-1$
	        	    }
	                break;
	        	case 'D':
	        	    try {
	                    Double.parseDouble(ASTInstructionCompiler.removePrefixZerosAndUnderscores(newText, false));
	                } catch (NumberFormatException e) {
	        	        type="double"; //$NON-NLS-1$
	                }
	                break;
	        	case 'F':
	        	    try {
	                    Float.parseFloat(ASTInstructionCompiler.removePrefixZerosAndUnderscores(newText, false));
	                } catch (NumberFormatException e) {
	        	        type="float"; //$NON-NLS-1$
	                }
	                break;
            	case 'I':
            	    try {
            	    	ASTInstructionCompiler.parseIntValue(newText);
                    } catch (NumberFormatException e) {
	        	        type="int"; //$NON-NLS-1$
                    }
                    break;
	        	case 'J':
	        	    try {
	        	    	ASTInstructionCompiler.parseLongValue(newText);
	                } catch (NumberFormatException e) {
	        	        type="long"; //$NON-NLS-1$
	                }
	                break;
	        	case 'S':
	        	    try {
	                    ASTInstructionCompiler.parseShortValue(newText);
	                } catch (NumberFormatException e) {
	        	        type="short"; //$NON-NLS-1$
	                }
	                break;
	        	case 'Z':
                    if (!("true".equals(newText) || "false".equals(newText))) { //$NON-NLS-1$ //$NON-NLS-2$
						type="boolean"; //$NON-NLS-1$
                    }
	                break;
            }
            if (type != null) {
				return NLS.bind(ActionMessages.JavaPrimitiveValueEditor_4, type);
            }
            return null;
        }

		private boolean isUnicode(String newText) {
			if (newText.length() == 6) {
				if (newText.charAt(1) == 'u') {
					char[] chars = newText.toCharArray();
					for (int i = 2; i < chars.length; i++) {
						if (!isHexDigit(chars[i])) {
							return false;
						}
					}
					return true;
				}
			}
			return false;
		}

		private boolean isOctalEscape(String newText) {
			char[] chars= newText.toCharArray();
			if (chars.length < 4) {
				for (int i = 1; i < chars.length; i++) {
					if (!isOctalDigit(chars[i])) {
						return false;
					}
				}
				return true;
			} else if (chars.length == 4) {
				char ch= chars[1];
				if (ch < '0' || ch > '3') {
					return false;
				}
				for (int i = 2; i < chars.length; i++) {
					if (!isOctalDigit(chars[i])) {
						return false;
					}
				}
                return true;
			}
			return false;
		}

		private boolean isSpecialCharacter(String newText) {
			char ch= newText.charAt(1);
			return newText.length() == 2 &&
				(ch == 'b'  ||
				ch == 't'  ||
				ch == 'n'  ||
				ch == 'f'  ||
				ch == 'r'  ||
				ch == '"'  ||
				ch == '\'' ||
				ch == '\\');
		}


		private boolean isOctalDigit(char ch) {
            return Character.digit(ch, 8) != -1;
		}

		private boolean isHexDigit(char ch) {
            return Character.digit(ch, 16) != -1;
		}

		/**
		 * Returns the integer value specified by the given string, which
		 * represents an octal or hexadecimal escape sequence. Returns
		 * Integer.MAX_VALUE if the given string is not a valid octal or
		 * hexadecimal escape sequence.
		 */
		protected int getEscapeValue(String string) {
			return ASTInstructionCompiler.parseIntValue(string);
		}
    }

}
