/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTInstructionCompiler;
import org.eclipse.osgi.util.NLS;

public class Cast extends CompoundInstruction {

	public static final String IS_INSTANCE = "isInstance"; //$NON-NLS-1$
	public static final String IS_INSTANCE_SIGNATURE = "(Ljava/lang/Object;)Z"; //$NON-NLS-1$

	private final int fTypeTypeId;

	private final String fBaseTypeName;

	private final int fDimension;

	/**
	 * Cast instruction constructor.
	 *
	 * @param typeTypeId
	 *            the id of the type to cast into.
	 * @param baseTypeName
	 *            the base type name of the type (the type name if the type is
	 *            not an array type.
	 * @param dimension
	 *            the dimension of the array type, 0 if the type is not an array
	 *            type.
	 */
	public Cast(int typeTypeId, String baseTypeName, int dimension, int start) {
		super(start);
		fTypeTypeId = typeTypeId;
		fBaseTypeName = baseTypeName;
		fDimension = dimension;
	}

	/*
	 * @see Instruction#execute()
	 */
	@Override
	public void execute() throws CoreException {
		IJavaValue value = popValue();

		if (value instanceof IJavaPrimitiveValue) {
			IJavaPrimitiveValue primitiveValue = (IJavaPrimitiveValue) value;
			int newTypeId = fTypeTypeId;
			if (fTypeTypeId == T_Object) {
				newTypeId = ASTInstructionCompiler.getPrimitiveTypeId(value.getJavaType().getName());
			}
			switch (newTypeId) {
			case T_double:
				push(newValue(primitiveValue.getDoubleValue()));
				break;
			case T_float:
				push(newValue(primitiveValue.getFloatValue()));
				break;
			case T_long:
				push(newValue(primitiveValue.getLongValue()));
				break;
			case T_int:
				push(newValue(primitiveValue.getIntValue()));
				break;
			case T_short:
				push(newValue(primitiveValue.getShortValue()));
				break;
			case T_byte:
				push(newValue(primitiveValue.getByteValue()));
				break;
			case T_char:
				push(newValue(primitiveValue.getCharValue()));
				break;
			}

		} else if (value instanceof JDINullValue) {
			// null value can be cast to all non-primitive types (bug 31637).
			push(value);
		} else {
			IJavaObject classObject;
			if (fDimension == 0) {
				classObject = getClassObject(getType(fBaseTypeName));
			} else {
				classObject = getClassObject(getArrayType(
						Signature.createTypeSignature(fBaseTypeName, true),
						fDimension));
			}
			if (classObject == null) {
				throw new CoreException(
						new Status(
								IStatus.ERROR,
								JDIDebugPlugin.getUniqueIdentifier(),
								IStatus.OK,
								NLS.bind(InstructionsEvaluationMessages.Cast_No_class_object,
										typeName()),
								null));
			}
			IJavaPrimitiveValue resultValue = (IJavaPrimitiveValue) classObject
					.sendMessage(IS_INSTANCE, IS_INSTANCE_SIGNATURE,
							new IJavaValue[] { value }, getContext()
									.getThread(), false);
			if (!resultValue.getBooleanValue()) {
				throw new CoreException(
						new Status(
								IStatus.ERROR,
								JDIDebugPlugin.getUniqueIdentifier(),
								IStatus.OK,
								NLS.bind(InstructionsEvaluationMessages.Cast_ClassCastException__Cannot_cast__0__as__1___1,
														value.toString(),
										typeName()), null));
			}

			push(value);
		}
	}

	private String typeName() {
		String result = fBaseTypeName;
		for (int i = 0; i < fDimension; i++) {
			result += "[]"; //$NON-NLS-1$
		}
		return result;
	}

	/*
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return InstructionsEvaluationMessages.Cast_cast_3;
	}

}
