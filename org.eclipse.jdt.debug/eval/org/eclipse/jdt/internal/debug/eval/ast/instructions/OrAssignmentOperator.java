/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

public class OrAssignmentOperator extends OrOperator {

	public OrAssignmentOperator(int variableTypeId, int valueTypeId, int start) {
		super(variableTypeId, variableTypeId, valueTypeId, true, start);
	}

	public String toString() {
		return EvalMessages.getString("OrAssignmentOperator._|=___operator_1"); //$NON-NLS-1$
	}

}