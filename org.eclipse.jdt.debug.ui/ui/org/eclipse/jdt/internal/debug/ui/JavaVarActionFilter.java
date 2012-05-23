/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JavaStructureErrorValue;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.core.model.JDIPlaceholderValue;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListVariable;
import org.eclipse.jdt.internal.debug.ui.actions.EditVariableLogicalStructureAction;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.ui.IActionFilter;

import com.sun.jdi.ClassNotLoadedException;

/**
 * Provides the action filter for Java and Inspect actions
 * 
 * @since 3.2
 */
public class JavaVarActionFilter implements IActionFilter {

	/**
	 * The set or primitive types
	 */
	private static final Set<String> fgPrimitiveTypes = initPrimitiveTypes();

	/**
	 * The predefined set of primitive types
	 * @return the set of predefined types
	 */
	private static Set<String> initPrimitiveTypes() {
		HashSet<String> set = new HashSet<String>(8);
		set.add("short"); //$NON-NLS-1$
		set.add("int"); //$NON-NLS-1$
		set.add("long"); //$NON-NLS-1$
		set.add("float"); //$NON-NLS-1$
		set.add("double"); //$NON-NLS-1$
		set.add("boolean"); //$NON-NLS-1$
		set.add("byte"); //$NON-NLS-1$
		set.add("char"); //$NON-NLS-1$
		set.add("null"); //$NON-NLS-1$
		return set;
	}
	
	/**
	 * Determines if the declared value is the same as the concrete value
	 * @param var the variable to inspect
	 * @return true if the types are the same, false otherwise
	 */
	protected boolean isDeclaredSameAsConcrete(IJavaVariable var) {
		try {
			IValue value = var.getValue();
			if (value instanceof JDINullValue) {
				return false;
			}
			return !var.getReferenceTypeName().equals(value.getReferenceTypeName());
		}
		catch(DebugException e) {JDIDebugUIPlugin.log(e);}
		return false;
	}

	/**
	 * Determines if the passed object is a primitive type or not
	 * @param obj the obj to test 
	 * @return true if the object is primitive, false otherwise
	 */
	protected boolean isPrimitiveType(Object obj) {
		if(obj instanceof IJavaVariable) {
			try {
				return !fgPrimitiveTypes.contains(removeArray(((IJavaVariable) obj).getReferenceTypeName()));
			} 
			catch (DebugException e) {
				if(!(e.getStatus().getException() instanceof ClassNotLoadedException)) {
					JDIDebugUIPlugin.log(e);
				}
				return false;
			}
		}
		else if(obj instanceof JavaInspectExpression) {
			try {
				JavaInspectExpression exp = (JavaInspectExpression)obj;
				IValue value = exp.getValue();
				if (value != null) {
					return fgPrimitiveTypes.contains(removeArray(value.getReferenceTypeName()));
				}
			} catch (DebugException e) {JDIDebugUIPlugin.log(e);}
		}
		return false;
	}
	
	/**
	 * This method returns if the specified object is an array or not
	 * @param object the object to test
	 * @return true if the specified object has the <code>IJavaType</code> of <code>JDIArrayType</code>, false otherwise
	 * @since 3.3
	 */
	protected boolean isArrayType(Object object) {
		if(object instanceof IJavaVariable) {
			try {
				IJavaType type = ((IJavaVariable)object).getJavaType();
				if(type != null) {
					return type instanceof IJavaArrayType;
				}
			} 
			catch (DebugException e) {JDIDebugUIPlugin.log(e);}
		}
		return false;
	}
	
	/**
	 * Determines if the ref type of the value is primitive
	 * 
	 * @param var the variable to inspect
	 * @return true if the the values ref type is primitive, false otherwise
	 */
	protected boolean isValuePrimitiveType(IValue value) {
		try {
			return !fgPrimitiveTypes.contains(removeArray(value.getReferenceTypeName()));
		} 
		catch (DebugException e) {JDIDebugUIPlugin.log(e);}
		return false;
	}
	
	/**
	 * Method removes the array declaration characters to return just the type
	 * 
	 * @param typeName the type name we want to strip the array delimiters from
	 * @return the altered type
	 */
	protected String removeArray(String type) {
		if (type != null) {
			int index= type.indexOf('[');
			if (index > 0) {
				return type.substring(0, index);
			}
		}
		return type;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionFilter#testAttribute(java.lang.Object, java.lang.String, java.lang.String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		if (target instanceof IJavaVariable) {
			IJavaVariable var = (IJavaVariable) target;
			IValue varValue;
			try {
				varValue = var.getValue();
				if (name.equals("PrimitiveVariableActionFilter")) { //$NON-NLS-1$ 
					if (value.equals("isPrimitive")) { //$NON-NLS-1$
						return isPrimitiveType(var);
					} 
					else if(value.equals("isArray")) { //$NON-NLS-1$
						return isArrayType(var);
					}
					else if (value.equals("isValuePrimitive")) { //$NON-NLS-1$
						return isValuePrimitiveType(varValue);
					}
				} 
				if (name.equals("JavaVariableFilter")) { //$NON-NLS-1$
					if (value.equals("isInstanceRetrievalAvailable")) { //$NON-NLS-1$
						return isInstanceRetrievalAvailable(var);
					}
					if(value.equals("isNullValue")) { //$NON-NLS-1$
						return varValue instanceof JDINullValue;
					}
					if (value.equals("isReferenceListVariable")) { //$NON-NLS-1$
						return var instanceof JDIReferenceListVariable;
					}
					if (value.equals("isPlaceholderValue")) { //$NON-NLS-1$
						return varValue instanceof JDIPlaceholderValue;
					}
				}
				else if (name.equals("ConcreteVariableActionFilter") && value.equals("isConcrete")) { //$NON-NLS-1$ //$NON-NLS-2$
					return isDeclaredSameAsConcrete(var);
				} 
				else if (name.equals("JavaVariableActionFilter")) { //$NON-NLS-1$
					if(value.equals("instanceFilter")) { //$NON-NLS-1$ 
						return !var.isStatic() && (varValue instanceof IJavaObject) && (((IJavaObject)varValue).getJavaType() instanceof IJavaClassType) && ((IJavaDebugTarget)var.getDebugTarget()).supportsInstanceBreakpoints();
					}
					if(value.equals("isValidField")) { //$NON-NLS-1$
						return !var.isFinal() & !(var.isFinal() & var.isStatic());
					}	
				} 
				else if (name.equals("DetailFormatterFilter") & (varValue instanceof IJavaObject)) { //$NON-NLS-1$
					if(value.equals("isDefined")) { //$NON-NLS-1$
						return JavaDetailFormattersManager.getDefault().hasAssociatedDetailFormatter(((IJavaObject)varValue).getJavaType());
					}
					if(value.equals("inInterface")) { //$NON-NLS-1$
						return JavaDetailFormattersManager.getDefault().hasInterfaceDetailFormatter(((IJavaObject)varValue).getJavaType());
					}
					if(value.equals("inSuperclass")) { //$NON-NLS-1$
						return JavaDetailFormattersManager.getDefault().hasSuperclassDetailFormatter(((IJavaObject)varValue).getJavaType());
					}
				} 
				else if (name.equals("JavaLogicalStructureFilter") && value.equals("canEditLogicalStructure")) {  //$NON-NLS-1$ //$NON-NLS-2$
                    return varValue instanceof JavaStructureErrorValue || EditVariableLogicalStructureAction.getLogicalStructure(varValue) != null;
                }
			} catch (DebugException e) {}	
		}
		else if (target instanceof JavaInspectExpression) {
			JavaInspectExpression exp = (JavaInspectExpression) target;
			if (name.equals("PrimitiveVariableActionFilter") && value.equals("isNotPrimitive")) { //$NON-NLS-1$ //$NON-NLS-2$
				return !isPrimitiveType(exp);
			} 
			else if (name.equals("DetailFormatterFilter")) { //$NON-NLS-1$
				try {
					IValue varValue = exp.getValue();
					if(varValue instanceof IJavaObject) {
						if(value.equals("isDefined")) { //$NON-NLS-1$
							return JavaDetailFormattersManager.getDefault().hasAssociatedDetailFormatter(((IJavaObject)varValue).getJavaType());
						}
						if(value.equals("inInterface")) { //$NON-NLS-1$
							return JavaDetailFormattersManager.getDefault().hasInterfaceDetailFormatter(((IJavaObject)varValue).getJavaType());
						}
						if(value.equals("inSuperclass")) { //$NON-NLS-1$
							return JavaDetailFormattersManager.getDefault().hasSuperclassDetailFormatter(((IJavaObject)varValue).getJavaType());
						}
					}
				} 
				catch (DebugException exception) {}
			}
		}
		return false;
	}

	/**
	 * Returns whether this variable's VM supports instance/reference information.
	 * 
	 * @param var variable
	 * @return whether this variable's VM supports instance/reference information
	 */
	protected boolean isInstanceRetrievalAvailable(IJavaVariable var) {
		return ((IJavaDebugTarget)var.getDebugTarget()).supportsInstanceRetrieval() && !(var instanceof JDIReferenceListVariable);
	}	
}