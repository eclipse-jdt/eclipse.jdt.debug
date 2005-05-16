/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import java.util.ArrayList;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaValue;

import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
 
/**
 * Common functionality for variables that support value modification
 */
public abstract class JDIModificationVariable extends JDIVariable {
	
	private final static ArrayList fgValidSignatures = new ArrayList (9);
	static {
		fgValidSignatures.add("B");  // byte //$NON-NLS-1$
 		fgValidSignatures.add("C");  // char //$NON-NLS-1$
		fgValidSignatures.add("D");  // double //$NON-NLS-1$
		fgValidSignatures.add("F");  // float //$NON-NLS-1$
		fgValidSignatures.add("I");  // int //$NON-NLS-1$
		fgValidSignatures.add("J");  // long //$NON-NLS-1$
		fgValidSignatures.add("S");  // short //$NON-NLS-1$
		fgValidSignatures.add("Z");  // boolean //$NON-NLS-1$
		fgValidSignatures.add(jdiStringSignature); // String
	}

	public JDIModificationVariable(JDIDebugTarget target) {
		super(target);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		return true;
	}
	
	protected Value generateVMValue(String expression) throws DebugException {
	
		String signature= null;
		Value cValue= getCurrentValue();
		VirtualMachine vm= getVM();
		if (vm == null) {
			requestFailed(JDIDebugModelMessages.JDIModificationVariable_Unable_to_generate_value___VM_disconnected__1, null); //$NON-NLS-1$
		}
		if (cValue == null) {
			//String with null value
			signature = jdiStringSignature;
		} else {
			signature= cValue.type().signature();
		}
		if (signature.length() > 1 && !signature.equals(jdiStringSignature)) {
			return null;
		}
		Value vmValue= null;
		try {
			switch (signature.charAt(0)) {
				case 'Z' :
					String flse= Boolean.FALSE.toString();
					String tre= Boolean.TRUE.toString();
					if (expression.equals(tre) || expression.equals(flse)) {
						boolean booleanValue= Boolean.valueOf(expression).booleanValue();
						vmValue= vm.mirrorOf(booleanValue);
					}
					break;
				case 'B' :
					byte byteValue= Byte.valueOf(expression).byteValue();
					vmValue= vm.mirrorOf(byteValue);
					break;
				case 'C' :
					if (expression.length() == 1) {
						char charValue= expression.charAt(0);
						vmValue= vm.mirrorOf(charValue);
					} else if (expression.length() == 2) {
							char charValue;
							if (!(expression.charAt(0) == '\\')) {
								return null;
							}
							switch (expression.charAt(1)) {
								case 'b':
									charValue= '\b';
									break;
								case 'f':
									charValue= '\f';
									break;
								case 'n':
									charValue= '\n';
									break;
								case 'r':
									charValue= '\r';
									break;
								case 't':
									charValue= '\t';
									break;
								case '\'':
									charValue= '\'';
									break;
								case '\"':
									charValue= '\"';
									break;
								case '\\':
									charValue= '\\';
									break;
								default :
									return null;
							}
						vmValue= vm.mirrorOf(charValue);
					}
					break;
				case 'S' :
					short shortValue= Short.valueOf(expression).shortValue();
					vmValue= vm.mirrorOf(shortValue);
					break;
				case 'I' :
					int intValue= Integer.valueOf(expression).intValue();
					vmValue= vm.mirrorOf(intValue);
					break;
				case 'J' :
					long longValue= Long.valueOf(expression).longValue();
					vmValue= vm.mirrorOf(longValue);
					break;
				case 'F' :
					float floatValue= Float.valueOf(expression).floatValue();
					vmValue= vm.mirrorOf(floatValue);
					break;
				case 'D' :
					double doubleValue= Double.valueOf(expression).doubleValue();
					vmValue= vm.mirrorOf(doubleValue);
					break;
				case 'L' :
					if (expression.equals("null")) { //$NON-NLS-1$
						vmValue = null;
					} else if (expression.equals("\"null\"")) { //$NON-NLS-1$
						vmValue = vm.mirrorOf("null"); //$NON-NLS-1$
					} else {
						vmValue= vm.mirrorOf(expression);
					}
					break;

			}
		} catch (NumberFormatException nfe) {
			return null;
		}
		return vmValue;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(java.lang.String)
	 */
	public boolean verifyValue(String expression) {
		try {
			IValue value = JDIValue.createValue(getJavaDebugTarget(), generateVMValue(expression));
			return verifyValue(value);
		} catch (DebugException e) {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(org.eclipse.debug.core.model.IValue)
	 */
	public boolean verifyValue(IValue value) {
		return value instanceof IJavaValue &&
			value.getDebugTarget().equals(getDebugTarget());
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(java.lang.String)
	 */
	public final void setValue(String expression) throws DebugException {
	 	Value value= generateVMValue(expression);
		setJDIValue(value);
	}

	/**
	 * Set this variable's value to the given value
	 */
	protected abstract void setJDIValue(Value value) throws DebugException;
	
}
