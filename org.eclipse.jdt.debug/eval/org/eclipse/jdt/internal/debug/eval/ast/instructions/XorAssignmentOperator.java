/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

public class XorAssignmentOperator extends XorOperator {

	public XorAssignmentOperator(int variableTypeId, int valueTypeId, int start) {
		super(variableTypeId, variableTypeId, valueTypeId, true, start);
	}

	public String toString() {
		return InstructionsEvaluationMessages.getString("XorAssignmentOperator._^=___operator_1"); //$NON-NLS-1$
	}

}