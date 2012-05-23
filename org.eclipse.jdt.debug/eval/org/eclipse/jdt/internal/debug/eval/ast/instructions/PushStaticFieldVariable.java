/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.osgi.util.NLS;

/**
 * Pushes the value of the static fields of the given type onto the stack.
 */
public class PushStaticFieldVariable extends CompoundInstruction {

	private String fFieldName;

	private String fQualifiedTypeName;

	public PushStaticFieldVariable(String fieldName, String qualifiedTypeName,
			int start) {
		super(start);
		fFieldName = fieldName;
		fQualifiedTypeName = qualifiedTypeName;
	}

	@Override
	public void execute() throws CoreException {
		IJavaType receiver = getType(fQualifiedTypeName);

		IJavaVariable field = null;

		if (receiver instanceof IJavaInterfaceType) {
			field = ((IJavaInterfaceType) receiver).getField(fFieldName);
		} else if (receiver instanceof IJavaClassType) {
			field = ((IJavaClassType) receiver).getField(fFieldName);
		}
		if (field == null) {
			String message = NLS.bind(InstructionsEvaluationMessages.PushStaticFieldVariable_Cannot_find_the_field__0__in__1__1,
							new String[] { fFieldName, fQualifiedTypeName });
			throw new CoreException(new Status(IStatus.ERROR,
					JDIDebugPlugin.getUniqueIdentifier(), IStatus.OK, message,
					null)); // couldn't find the field
		}
		push(field);
	}

	@Override
	public String toString() {
		return NLS.bind(InstructionsEvaluationMessages.PushStaticFieldVariable_push_static_field__0__2,
						new String[] { fFieldName });
	}

}
