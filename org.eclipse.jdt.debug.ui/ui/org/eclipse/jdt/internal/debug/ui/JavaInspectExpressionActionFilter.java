package org.eclipse.jdt.internal.debug.ui;

/* ********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
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
		return set;
	}

	/**
	 * @see org.eclipse.ui.IActionFilter#testAttribute(java.lang.Object, java.lang.String, java.lang.String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		if (target instanceof JavaInspectExpression) {
			JavaInspectExpression exp= (JavaInspectExpression) target;
			if (name.equals("PrimitiveVariableActionFilter") && value.equals("isNotPrimitive")) { //$NON-NLS-1$ //$NON-NLS-2$
				return isNotPrimitiveType(exp);
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
	
	private boolean isNotPrimitiveType(JavaInspectExpression exp) {
		try {
			String refType = OpenVariableTypeAction.removeArray(exp.getValue().getReferenceTypeName());
			return !fgPrimitiveTypes.contains(refType);
		} catch (DebugException e) {
		}
		return false;
	}

}
