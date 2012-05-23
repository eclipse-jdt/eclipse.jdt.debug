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

public class TwiddleOperator extends UnaryOperator {

	public TwiddleOperator(int expressionTypeId, int start) {
		super(expressionTypeId, start);
	}

	/*
	 * @see Instruction#execute()
	 */
	@Override
	public void execute() throws CoreException {
		IJavaPrimitiveValue value = (IJavaPrimitiveValue) popValue();
		switch (fExpressionTypeId) {
		case T_long:
			pushNewValue(~value.getLongValue());
			break;
		case T_byte:
		case T_short:
		case T_int:
		case T_char:
			pushNewValue(~value.getIntValue());
			break;
		}
	}

	@Override
	public String toString() {
		return InstructionsEvaluationMessages.TwiddleOperator______operator_1;
	}

}
