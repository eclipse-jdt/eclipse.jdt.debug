/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

public class MinusAssignmentOperator extends MinusOperator {

	public MinusAssignmentOperator(int variableTypeId, int valueTypeId, int start) {
		super(variableTypeId, variableTypeId, valueTypeId, true, start);
	}

	public String toString() {
		return EvalMessages.getString("MinusAssignmentOperator._-=___operator_1"); //$NON-NLS-1$
	}

}