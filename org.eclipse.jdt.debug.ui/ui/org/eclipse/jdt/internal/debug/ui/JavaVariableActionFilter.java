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
package org.eclipse.jdt.internal.debug.ui;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JavaStructureErrorValue;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.ui.actions.EditVariableLogicalStructureAction;
import org.eclipse.jdt.internal.debug.ui.actions.OpenVariableTypeAction;
import org.eclipse.ui.IActionFilter;

public class JavaVariableActionFilter implements IActionFilter {

	private static final Set fgPrimitiveTypes = initPrimitiveTypes();

	private static Set initPrimitiveTypes() {
		HashSet set = new HashSet(8);
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
	 * @see org.eclipse.ui.IActionFilter#testAttribute(Object, String, String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		if (target instanceof IJavaVariable) {
			IJavaVariable var = (IJavaVariable) target;
			IValue varValue;
			try {
				varValue= var.getValue();
				if (name.equals("PrimitiveVariableActionFilter")) { //$NON-NLS-1$ //$NON-NLS-2$
					if (value.equals("isPrimitive")) { //$NON-NLS-1$
						return isPrimitiveType(var);
					} else if (value.equals("isValuePrimitive")) { //$NON-NLS-1$
						return isValuePrimitiveType(var);
					}
				} else if (name.equals("ConcreteVariableActionFilter") && value.equals("isConcrete")) { //$NON-NLS-1$ //$NON-NLS-2$
					return isDeclaredSameAsConcrete(var);
				} else if (name.equals("JavaVariableActionFilter") && value.equals("instanceFilter")) { //$NON-NLS-1$ //$NON-NLS-2$
					return !var.isStatic() && (varValue instanceof IJavaObject) && (((IJavaObject)varValue).getJavaType() instanceof IJavaClassType) && ((IJavaDebugTarget)var.getDebugTarget()).supportsInstanceBreakpoints();
				} else if (name.equals("DetailFormatterFilter") && value.equals("isDefined")) { //$NON-NLS-1$ //$NON-NLS-2$
					return (varValue instanceof IJavaObject) && (JavaDetailFormattersManager.getDefault().hasAssociatedDetailFormatter(((IJavaObject)varValue).getJavaType()));
				} else if (name.equals("JavaLogicalStructureFilter") && value.equals("canEditLogicalStructure")) {  //$NON-NLS-1$ //$NON-NLS-2$
                    return varValue instanceof JavaStructureErrorValue || EditVariableLogicalStructureAction.getLogicalStructure(varValue) != null;
                }
			} catch (DebugException e) {
				JDIDebugUIPlugin.log(e);
			}
		}

		return false;
	}

	protected boolean isDeclaredSameAsConcrete(IJavaVariable var) throws DebugException {
		IValue value= var.getValue();
		if (value instanceof JDINullValue) {
			return false;
		}
		return !var.getReferenceTypeName().equals(value.getReferenceTypeName());
	}
	protected String getTypeNameToOpen(String refType) {
		refType = OpenVariableTypeAction.removeArray(refType);
		if (fgPrimitiveTypes.contains(refType)) {
			return null;
		}
		return refType;
	}


	protected boolean isPrimitiveType(IJavaVariable var) {
		try {
			return getTypeNameToOpen(var.getReferenceTypeName()) != null;
		} catch (DebugException e) {
			JDIDebugUIPlugin.log(e);
			// fall through
		}
		return false;
	}
	
	protected boolean isValuePrimitiveType(IJavaVariable var) {
		try {
			return getTypeNameToOpen(var.getValue().getReferenceTypeName()) != null;
		} catch (DebugException e) {
			JDIDebugUIPlugin.log(e);
			// fall through
		}
		return false;
	}
}
