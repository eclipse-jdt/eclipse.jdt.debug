/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

public class LeftShiftAssignmentOperator extends LeftShiftOperator {

	public LeftShiftAssignmentOperator(int variableTypeId, int valueTypeId, int start) {
		super(variableTypeId, variableTypeId, valueTypeId, true, start);
	}

	public String toString() {
		return InstructionsEvaluationMessages.getString("LeftShiftAssignmentOperator._<<=___operator_1"); //$NON-NLS-1$
	}

}