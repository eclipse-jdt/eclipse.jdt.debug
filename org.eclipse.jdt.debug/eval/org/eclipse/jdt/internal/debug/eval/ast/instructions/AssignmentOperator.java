/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

public class AssignmentOperator extends CompoundInstruction {

	protected int fVariableTypeId;
	protected int fValueTypeId;

	public AssignmentOperator(int variableTypeId, int valueTypeId, int start) {
		super(start);
		fVariableTypeId = variableTypeId;
		fValueTypeId = valueTypeId;
	}

	/*
	 * @see Instruction#execute()
	 */
	@Override
	public void execute() throws CoreException {
		IJavaValue value = popValue();
		IJavaVariable variable = (IJavaVariable) pop();

		if (value instanceof IJavaPrimitiveValue) {
			IJavaPrimitiveValue primitiveValue = (IJavaPrimitiveValue) value;
			switch (fVariableTypeId) {
			case T_boolean:
				variable.setValue(newValue(primitiveValue.getBooleanValue()));
				break;
			case T_byte:
				variable.setValue(newValue(primitiveValue.getByteValue()));
				break;
			case T_short:
				variable.setValue(newValue(primitiveValue.getShortValue()));
				break;
			case T_char:
				variable.setValue(newValue(primitiveValue.getCharValue()));
				break;
			case T_int:
				variable.setValue(newValue(primitiveValue.getIntValue()));
				break;
			case T_long:
				variable.setValue(newValue(primitiveValue.getLongValue()));
				break;
			case T_float:
				variable.setValue(newValue(primitiveValue.getFloatValue()));
				break;
			case T_double:
				variable.setValue(newValue(primitiveValue.getDoubleValue()));
				break;
			}
		} else {
			variable.setValue(value);
		}
		push(variable.getValue());
	}

	@Override
	public String toString() {
		return InstructionsEvaluationMessages.AssignmentOperator_operator_1;
	}
}
