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
import org.eclipse.jdt.debug.core.IJavaVariable;

public class PostfixPlusPlusOperator extends XfixOperator {

	public PostfixPlusPlusOperator(int variableTypeId, int start) {
		super(variableTypeId, start);
	}

	/*
	 * @see Instruction#execute()
	 */
	@Override
	public void execute() throws CoreException {
		IJavaVariable variable = (IJavaVariable) pop();
		push(variable.getValue());

		switch (fVariableTypeId) {
		case T_byte:
			variable.setValue(newValue((byte) (((IJavaPrimitiveValue) variable
					.getValue()).getByteValue() + 1)));
			break;
		case T_short:
			variable.setValue(newValue((short) (((IJavaPrimitiveValue) variable
					.getValue()).getShortValue() + 1)));
			break;
		case T_char:
			variable.setValue(newValue((char) (((IJavaPrimitiveValue) variable
					.getValue()).getCharValue() + 1)));
			break;
		case T_int:
			variable.setValue(newValue(((IJavaPrimitiveValue) variable
					.getValue()).getIntValue() + 1));
			break;
		case T_long:
			variable.setValue(newValue(((IJavaPrimitiveValue) variable
					.getValue()).getLongValue() + 1));
			break;
		case T_float:
			variable.setValue(newValue(((IJavaPrimitiveValue) variable
					.getValue()).getFloatValue() + 1));
			break;
		case T_double:
			variable.setValue(newValue(((IJavaPrimitiveValue) variable
					.getValue()).getDoubleValue() + 1));
			break;
		}
	}

	@Override
	public String toString() {
		return InstructionsEvaluationMessages.PostfixPlusPlusOperator_postfix________operator_1;
	}

}
