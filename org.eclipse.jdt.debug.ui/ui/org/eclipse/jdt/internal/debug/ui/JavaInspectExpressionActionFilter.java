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
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.internal.debug.ui.actions.OpenVariableTypeAction;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.ui.IActionFilter;

public class JavaInspectExpressionActionFilter implements IActionFilter {

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
	 * @see org.eclipse.ui.IActionFilter#testAttribute(java.lang.Object, java.lang.String, java.lang.String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		if (target instanceof JavaInspectExpression) {
			JavaInspectExpression exp= (JavaInspectExpression) target;
			if (name.equals("PrimitiveVariableActionFilter") && value.equals("isNotPrimitive")) { //$NON-NLS-1$ //$NON-NLS-2$
				return !isPrimitiveType(exp);
			} else if (name.equals("DetailFormatterFilter") && value.equals("isDefined")) { //$NON-NLS-1$ //$NON-NLS-2$
				try {
					IValue varValue= exp.getValue();
					return (varValue instanceof IJavaObject) && (JavaDetailFormattersManager.getDefault().hasAssociatedDetailFormatter(((IJavaObject)varValue).getJavaType()));
				} catch (DebugException exception) {
					JDIDebugUIPlugin.log(exception);
				}
			}
		}
		return false;
	}
	
	private boolean isPrimitiveType(JavaInspectExpression exp) {
		if (exp == null) {
			return false;
		}
		try {
			IValue value = exp.getValue();
			if (value != null) {
				String refType = OpenVariableTypeAction.removeArray(value.getReferenceTypeName());
				return fgPrimitiveTypes.contains(refType);
			}
		} catch (DebugException e) {
		}
		return false;
	}

}
